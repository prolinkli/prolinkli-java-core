#!/bin/bash

# Load vault environment configuration
source "$(dirname "${BASH_SOURCE[0]}")/vault-env.sh"

source ../log.sh

# Java Core Vault Group Member Addition Script
# This script adds users to the java-core Vault identity group
# Usage: ./add-java-core-member.sh <username> [<username2> ...]

# Configuration (using vault-env.sh values)
JAVA_CORE_GROUP="java-core"
VAULT_MOUNT="${VAULT_KV_MOUNT}"
VAULT_VERSION="${VAULT_KV_VERSION}"
TOKEN_FILE="$VAULT_TOKEN_FILE"

# Function to load token from file (using vault-env.sh)
load_token_from_file() {
    vault_env_load_token
}

# Function to check if vault CLI is installed (using vault-env.sh)
check_vault_cli() {
    if ! vault_env_check_cli; then
        exit 1
    fi
}

# Function to validate vault connection (using vault-env.sh)
validate_vault_connection() {
    log_info "Validating Vault connection..."
    
    # Try to load token from file first
    load_token_from_file
    
    if [[ -z "$VAULT_TOKEN" ]]; then
        log_error "VAULT_TOKEN environment variable is not set and no token file found"
        log_error "Please run ./vault-auth.sh first to authenticate"
        exit 1
    fi

    # Test vault connection
    if ! vault_env_test_connection; then
        exit 1
    fi

    # Test authentication
    if ! vault_env_check_auth; then
        exit 1
    fi

    log_success "Vault connection validated"
}

# Function to create vault group policy if needed
create_vault_group_policy() {
    local group_name="$1"
    local policy_name="$group_name-policy"
    
    log_info "Creating Vault policy for group '$group_name'"
    
    # Create a basic policy for the java-core group
    cat > "/tmp/${policy_name}.hcl" << EOF
# Java Core Group Policy
# Allows access to java-core specific secrets

# Allow access to java-core secrets
path "${VAULT_KV_MOUNT}/data/java-core/*" {
  capabilities = ["read", "list"]
}

path "${VAULT_KV_MOUNT}/metadata/java-core/*" {
  capabilities = ["read", "list"]
}

# Allow access to shared development secrets
path "${VAULT_KV_MOUNT}/data/shared/dev/*" {
  capabilities = ["read", "list"]
}

path "${VAULT_KV_MOUNT}/metadata/shared/dev/*" {
  capabilities = ["read", "list"]
}

# Allow reading own user secrets
path "${VAULT_KV_MOUNT}/data/users/{{identity.entity.name}}/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}

path "${VAULT_KV_MOUNT}/metadata/users/{{identity.entity.name}}/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
EOF

    # Upload policy to Vault
    vault policy write "$policy_name" "/tmp/${policy_name}.hcl"
    
    if [[ $? -eq 0 ]]; then
        log_success "Policy '$policy_name' created successfully"
        rm -f "/tmp/${policy_name}.hcl"
    else
        log_error "Failed to create policy '$policy_name'"
        rm -f "/tmp/${policy_name}.hcl"
        return 1
    fi
}

# Function to add user to vault group
add_user_to_vault_group() {
    local username="$1"
    local group_name="$2"
    
    log_info "Adding user '$username' to Vault group '$group_name'"
    
    # Check if the group exists in Vault
    if ! vault read "identity/group/name/$group_name" &> /dev/null; then
        log_info "Vault group '$group_name' does not exist. Creating it..."
        
        # Create the group policy first
        create_vault_group_policy "$group_name"
        
        # Create the group in Vault
        vault write "identity/group/name/$group_name" \
            policies="$group_name-policy,default" \
            type="internal" \
            metadata="description=Java Core Development Group"
        
        if [[ $? -eq 0 ]]; then
            log_success "Vault group '$group_name' created"
        else
            log_error "Failed to create Vault group '$group_name'"
            return 1
        fi
    fi
    
    # Check if user entity exists
    if ! vault read "identity/entity/name/$username" &> /dev/null; then
        log_info "User entity '$username' does not exist in Vault. Creating it..."
        
        vault write "identity/entity/name/$username" \
            policies="default" \
            metadata="username=$username"
        
        if [[ $? -eq 0 ]]; then
            log_success "User entity '$username' created in Vault"
        else
            log_error "Failed to create user entity '$username' in Vault"
            return 1
        fi
    fi
    
    # Get the user entity ID
    local entity_id
    entity_id=$(vault read -field=id "identity/entity/name/$username")
    
    if [[ -z "$entity_id" ]]; then
        log_error "Failed to get entity ID for user '$username'"
        return 1
    fi
    
    # Get current group members
    local current_members
    current_members=$(vault read -field=member_entity_ids "identity/group/name/$group_name" 2>/dev/null || echo "")
    
    # Check if user is already a member
    if [[ "$current_members" == *"$entity_id"* ]]; then
        log_info "User '$username' is already a member of Vault group '$group_name'"
        return 0
    fi
    
    # Add user to group (append to existing members)
    if [[ -n "$current_members" ]]; then
        vault write "identity/group/name/$group_name" \
            member_entity_ids="$current_members,$entity_id"
    else
        vault write "identity/group/name/$group_name" \
            member_entity_ids="$entity_id"
    fi
    
    if [[ $? -eq 0 ]]; then
        log_success "User '$username' added to Vault group '$group_name'"
        return 0
    else
        log_error "Failed to add user '$username' to Vault group '$group_name'"
        return 1
    fi
}

# Function to list group members
list_group_members() {
    local group_name="$1"
    
    log_info "Current members of Vault group '$group_name':"
    
    # Get member entity IDs
    local member_ids
    member_ids=$(vault read -field=member_entity_ids "identity/group/name/$group_name" 2>/dev/null)
    
    if [[ -z "$member_ids" ]]; then
        echo "  No members found"
        return
    fi
    
    # Convert comma-separated IDs to array
    IFS=',' read -ra ID_ARRAY <<< "$member_ids"
    
    for entity_id in "${ID_ARRAY[@]}"; do
        # Get entity name from ID
        local entity_name
        entity_name=$(vault read -field=name "identity/entity/id/$entity_id" 2>/dev/null)
        
        if [[ -n "$entity_name" ]]; then
            echo "  - $entity_name ($entity_id)"
        else
            echo "  - Unknown entity ($entity_id)"
        fi
    done
}

# Function to display usage
usage() {
    echo "Usage: $0 <username> [<username2> ...]"
    echo ""
    echo "This script adds users to the java-core Vault identity group."
    echo ""
    echo "Environment Variables:"
    echo "  VAULT_ADDR      - Vault server address (default: http://10.2.2.2:8200)"
    echo "  VAULT_TOKEN     - Vault authentication token (required)"
    echo "  VAULT_MOUNT     - Vault mount point (default: kv)"
    echo "  VAULT_VERSION   - KV engine version: v1 or v2 (default: v2)"
    echo ""
    echo "Examples:"
    echo "  ./add-java-core-member.sh john.doe"
    echo "  ./add-java-core-member.sh alice bob charlie"
    echo ""
    echo "Before running, set your Vault token:"
    echo "  export VAULT_TOKEN=your-vault-token-here"
}

# Main script execution
main() {
    # Check arguments
    if [[ $# -eq 0 ]]; then
        log_error "No usernames provided"
        usage
        exit 1
    fi
    
    log_info "Starting Vault java-core group member addition process"
    log_info "Group: $JAVA_CORE_GROUP"
    log_info "Users to add: $*"
    log_info "Vault Address: $VAULT_ADDR"
    
    # Check prerequisites
    check_vault_cli
    validate_vault_connection
    
    # Process each username
    local success_count=0
    local total_count=$#
    
    for username in "$@"; do
        log_info "Processing user: $username"
        
        if add_user_to_vault_group "$username" "$JAVA_CORE_GROUP"; then
            ((success_count++))
        else
            log_error "Failed to add user '$username' to Vault group"
        fi
        
        echo "" # Add spacing between users
    done
    
    # Summary
    log_info "Summary: $success_count out of $total_count users processed successfully"
    
    if [[ $success_count -eq $total_count ]]; then
        log_success "All users added to java-core Vault group successfully"
        
        # Display group members
        list_group_members "$JAVA_CORE_GROUP"
        
        exit 0
    else
        log_error "Some users could not be added to the Vault group"
        exit 1
    fi
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 