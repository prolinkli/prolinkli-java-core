#!/bin/bash

source ./bash/log.sh

# Vault Secret Copy Script
# This script copies secrets from one Vault path to another
# Usage: ./vault-copy-secrets.sh <source_path> <destination_path>

# Configuration - these can be set as environment variables
VAULT_ADDR="${VAULT_ADDR:-http://10.2.2.2:8200}"
VAULT_TOKEN="${VAULT_TOKEN:-}"
VAULT_NAMESPACE="${VAULT_NAMESPACE:-}"
VAULT_MOUNT="${VAULT_MOUNT:-kv}"
VAULT_VERSION="${VAULT_VERSION:-v2}"
TOKEN_FILE="$HOME/.vault-token"

# Function to load token from file
load_token_from_file() {
    if [[ -z "$VAULT_TOKEN" && -f "$TOKEN_FILE" ]]; then
        local token=$(cat "$TOKEN_FILE")
        if [[ -n "$token" ]]; then
            export VAULT_TOKEN="$token"
            log_info "Loaded token from $TOKEN_FILE"
        fi
    fi
}

# Function to check if vault CLI is installed
check_vault_cli() {
    if ! command -v vault &> /dev/null; then
        log_error "Vault CLI is not installed. Please install HashiCorp Vault CLI first."
        echo "Installation instructions: https://developer.hashicorp.com/vault/docs/install"
        exit 1
    fi
}

# Function to validate vault connection
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
    if ! vault status &> /dev/null; then
        log_error "Cannot connect to Vault at $VAULT_ADDR"
        log_error "Please check your VAULT_ADDR and ensure Vault is accessible"
        exit 1
    fi

    # Test authentication
    if ! vault token lookup &> /dev/null; then
        log_error "Authentication failed. Please check your VAULT_TOKEN or run ./vault-auth.sh"
        exit 1
    fi

    log_success "Vault connection validated"
}

# Function to check if a secret exists
secret_exists() {
    local path="$1"
    local full_path
    
    if [[ "$VAULT_VERSION" == "v2" ]]; then
        full_path="$VAULT_MOUNT/metadata/$path"
    else
        full_path="$VAULT_MOUNT/$path"
    fi
    
    vault read "$full_path" &> /dev/null
}

# Function to read secret from vault
read_secret() {
    local path="$1"
    local full_path
    
    if [[ "$VAULT_VERSION" == "v2" ]]; then
        full_path="$VAULT_MOUNT/data/$path"
    else
        full_path="$VAULT_MOUNT/$path"
    fi
    
    vault read -format=json "$full_path"
}

# Function to write secret to vault
write_secret() {
    local path="$1"
    local data="$2"
    local full_path
    
    if [[ "$VAULT_VERSION" == "v2" ]]; then
        full_path="$VAULT_MOUNT/data/$path"
        # For KV v2, we need to wrap the data in a "data" field
        echo "$data" | vault write "$full_path" -
    else
        full_path="$VAULT_MOUNT/$path"
        echo "$data" | vault write "$full_path" -
    fi
}

# Function to copy secret
copy_secret() {
    local source_path="$1"
    local dest_path="$2"
    
    log_info "Copying secret from '$source_path' to '$dest_path'"
    
    # Check if source exists
    if ! secret_exists "$source_path"; then
        log_error "Source secret '$source_path' does not exist"
        exit 1
    fi
    
    # Read the source secret
    log_info "Reading source secret..."
    local secret_data
    secret_data=$(read_secret "$source_path")
    
    if [[ $? -ne 0 ]]; then
        log_error "Failed to read source secret '$source_path'"
        exit 1
    fi
    
    # Extract the actual secret data
    local extracted_data
    if [[ "$VAULT_VERSION" == "v2" ]]; then
        # For KV v2, extract the data field
        extracted_data=$(echo "$secret_data" | jq -r '.data.data')
    else
        # For KV v1, use the data field directly
        extracted_data=$(echo "$secret_data" | jq -r '.data')
    fi
    
    # Check if destination exists and prompt for confirmation
    if secret_exists "$dest_path"; then
        log_info "Destination secret '$dest_path' already exists"
        read -p "Do you want to overwrite it? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Operation cancelled"
            exit 0
        fi
    fi
    
    # Write to destination
    log_info "Writing to destination secret..."
    
    if [[ "$VAULT_VERSION" == "v2" ]]; then
        # For KV v2, we need to format the data properly
        echo "$extracted_data" | vault kv put "$VAULT_MOUNT/$dest_path" -
    else
        # For KV v1, write the data directly
        echo "$extracted_data" | vault write "$VAULT_MOUNT/$dest_path" -
    fi
    
    if [[ $? -eq 0 ]]; then
        log_success "Secret successfully copied from '$source_path' to '$dest_path'"
    else
        log_error "Failed to write secret to '$dest_path'"
        exit 1
    fi
}

# Function to display usage
usage() {
    echo "Usage: $0 <source_path> <destination_path>"
    echo ""
    echo "Environment Variables:"
    echo "  VAULT_ADDR      - Vault server address (default: http://10.2.2.2:8200)"
    echo "  VAULT_TOKEN     - Vault authentication token (required)"
    echo "  VAULT_NAMESPACE - Vault namespace (optional)"
    echo "  VAULT_MOUNT     - Vault mount point (default: kv)"
    echo "  VAULT_VERSION   - KV engine version: v1 or v2 (default: v2)"
    echo ""
    echo "Example:"
    echo "  export VAULT_ADDR=http://10.2.2.2:8200"
    echo "  export VAULT_TOKEN=s.xxxxxxxxxxxxxxxxxxxxxxxx"
    echo "  ./vault-copy-secrets.sh app/prod/config app/staging/config"
    echo ""
    echo "This will copy all secrets from 'app/prod/config' to 'app/staging/config'"
}

# Main script execution
main() {
    # Check arguments
    if [[ $# -ne 2 ]]; then
        log_error "Invalid number of arguments"
        usage
        exit 1
    fi
    
    local source_path="$1"
    local dest_path="$2"
    
    # Validate inputs
    if [[ -z "$source_path" || -z "$dest_path" ]]; then
        log_error "Source and destination paths cannot be empty"
        usage
        exit 1
    fi
    
    log_info "Starting Vault secret copy operation"
    log_info "Source: $source_path"
    log_info "Destination: $dest_path"
    log_info "Vault Address: $VAULT_ADDR"
    log_info "Vault Mount: $VAULT_MOUNT"
    log_info "KV Version: $VAULT_VERSION"
    
    # Check prerequisites
    check_vault_cli
    validate_vault_connection
    
    # Perform the copy
    copy_secret "$source_path" "$dest_path"
    
    log_success "Vault secret copy operation completed successfully"
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 