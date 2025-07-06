#!/bin/bash

# Load vault environment configuration
source "$(dirname "${BASH_SOURCE[0]}")/vault-env.sh"

source ../log.sh

# Vault Secret Copy Script
# This script copies secrets from one Vault path to another (supports recursive copying)
# Usage: ./vault-copy-secrets.sh <source_path> <destination_path> [--recursive]

# Configuration (using vault-env.sh values)
VAULT_MOUNT="${VAULT_KV_MOUNT}"
VAULT_VERSION="${VAULT_KV_VERSION}"
TOKEN_FILE="$VAULT_TOKEN_FILE"

# Global variables
RECURSIVE=false
COPIED_COUNT=0
FAILED_COUNT=0

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

# Function to check if a secret exists (using vault-env.sh)
secret_exists() {
    local path="$1"
    local full_path
    
    full_path=$(vault_env_kv_metadata_path "$path")
    
    vault read "$full_path" &> /dev/null
}

# Function to list secrets at a path (using vault-env.sh)
list_secrets() {
    local path="$1"
    local full_path
    
    full_path=$(vault_env_kv_metadata_path "$path")
    
    vault list -format=json "$full_path" 2>/dev/null | jq -r '.[]? // empty'
}

# Function to check if a path is a directory (ends with /)
is_directory() {
    local path="$1"
    [[ "$path" == */ ]]
}

# Function to recursively get all secret paths under a given path
get_all_secret_paths() {
    local base_path="$1"
    local current_path="$2"
    local paths=()
    
    # Remove trailing slash for consistency
    current_path="${current_path%/}"
    
    # List items at current path
    local items
    items=$(list_secrets "$current_path")
    
    if [[ -z "$items" ]]; then
        # No items found, check if this is a secret itself
        if secret_exists "$current_path"; then
            echo "$current_path"
        fi
        return
    fi
    
    # Process each item
    while IFS= read -r item; do
        if [[ -z "$item" ]]; then
            continue
        fi
        
        local full_item_path="$current_path/$item"
        
        if is_directory "$item"; then
            # Recursively get paths from subdirectory
            local subdir_path="${full_item_path%/}"
            get_all_secret_paths "$base_path" "$subdir_path"
        else
            # This is a secret
            echo "$full_item_path"
        fi
    done <<< "$items"
}

# Function to read secret from vault (using vault-env.sh)
read_secret() {
    local path="$1"
    vault_env_read_secret "$path"
}

# Function to write secret to vault (using vault-env.sh)
write_secret() {
    local path="$1"
    local data="$2"
    local full_path
    
    full_path=$(vault_env_kv_path "$path")
    
    if [[ "$VAULT_KV_VERSION" == "v2" ]]; then
        # For KV v2, we need to wrap the data in a "data" field
        echo "$data" | vault write "$full_path" -
    else
        echo "$data" | vault write "$full_path" -
    fi
}

# Function to copy a single secret
copy_single_secret() {
    local source_path="$1"
    local dest_path="$2"
    local prompt_overwrite="$3"
    
    log_info "Copying secret: '$source_path' → '$dest_path'"
    
    # Check if source exists
    if ! secret_exists "$source_path"; then
        log_error "Source secret '$source_path' does not exist"
        ((FAILED_COUNT++))
        return 1
    fi
    
    # Read the source secret
    local secret_data
    secret_data=$(read_secret "$source_path")
    
    if [[ $? -ne 0 ]]; then
        log_error "Failed to read source secret '$source_path'"
        ((FAILED_COUNT++))
        return 1
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
    
    # Check if destination exists and prompt for confirmation if requested
    if [[ "$prompt_overwrite" == "true" ]] && secret_exists "$dest_path"; then
        log_info "Destination secret '$dest_path' already exists"
        read -p "Do you want to overwrite it? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "Skipped '$dest_path'"
            return 0
        fi
    fi
    
    # Write to destination
    if [[ "$VAULT_VERSION" == "v2" ]]; then
        # For KV v2, we need to format the data properly
        echo "$extracted_data" | vault kv put "$VAULT_MOUNT/$dest_path" -
    else
        # For KV v1, write the data directly
        echo "$extracted_data" | vault write "$VAULT_MOUNT/$dest_path" -
    fi
    
    if [[ $? -eq 0 ]]; then
        log_success "✓ Copied '$source_path' to '$dest_path'"
        ((COPIED_COUNT++))
        return 0
    else
        log_error "✗ Failed to write secret to '$dest_path'"
        ((FAILED_COUNT++))
        return 1
    fi
}

# Function to copy secret(s) - handles both single and recursive
copy_secrets() {
    local source_path="$1"
    local dest_path="$2"
    
    if [[ "$RECURSIVE" == "true" ]]; then
        log_info "Starting recursive copy from '$source_path' to '$dest_path'"
        
        # Get all secret paths under the source
        local secret_paths
        secret_paths=$(get_all_secret_paths "$source_path" "$source_path")
        
        if [[ -z "$secret_paths" ]]; then
            log_error "No secrets found under '$source_path'"
            exit 1
        fi
        
        local total_secrets
        total_secrets=$(echo "$secret_paths" | wc -l)
        log_info "Found $total_secrets secret(s) to copy"
        
        # Ask for batch overwrite confirmation
        local overwrite_all=false
        if [[ "$total_secrets" -gt 1 ]]; then
            read -p "Do you want to overwrite existing secrets without prompting for each one? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                overwrite_all=true
            fi
        fi
        
        # Copy each secret
        while IFS= read -r secret_path; do
            if [[ -z "$secret_path" ]]; then
                continue
            fi
            
            # Calculate relative path from source
            local relative_path="${secret_path#$source_path}"
            relative_path="${relative_path#/}"  # Remove leading slash
            
            # Calculate destination path
            local target_path="$dest_path"
            if [[ -n "$relative_path" ]]; then
                target_path="$dest_path/$relative_path"
            fi
            
            # Copy the secret
            if [[ "$overwrite_all" == "true" ]]; then
                copy_single_secret "$secret_path" "$target_path" "false"
            else
                copy_single_secret "$secret_path" "$target_path" "true"
            fi
        done <<< "$secret_paths"
        
    else
        # Single secret copy
        copy_single_secret "$source_path" "$dest_path" "true"
    fi
}

# Function to display usage
usage() {
    echo "Usage: $0 <source_path> <destination_path> [--recursive]"
    echo ""
    echo "Options:"
    echo "  --recursive, -r  Copy all secrets recursively from source path"
    echo ""
    echo "Environment Variables:"
    echo "  VAULT_ADDR      - Vault server address (default: http://10.2.2.2:8200)"
    echo "  VAULT_TOKEN     - Vault authentication token (required)"
    echo "  VAULT_NAMESPACE - Vault namespace (optional)"
    echo "  VAULT_MOUNT     - Vault mount point (default: kv)"
    echo "  VAULT_VERSION   - KV engine version: v1 or v2 (default: v2)"
    echo ""
    echo "Examples:"
    echo "  # Copy a single secret"
    echo "  ./vault-copy-secrets.sh app/prod/config app/staging/config"
    echo ""
    echo "  # Copy all secrets recursively"
    echo "  ./vault-copy-secrets.sh secret/dev secret/staging --recursive"
    echo ""
    echo "  # This will copy all secrets under 'secret/dev' to 'secret/staging'"
    echo "  # preserving the directory structure"
}

# Main script execution
main() {
    # Parse arguments
    local source_path=""
    local dest_path=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --recursive|-r)
                RECURSIVE=true
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            *)
                if [[ -z "$source_path" ]]; then
                    source_path="$1"
                elif [[ -z "$dest_path" ]]; then
                    dest_path="$1"
                else
                    log_error "Too many arguments"
                    usage
                    exit 1
                fi
                shift
                ;;
        esac
    done
    
    # Validate inputs
    if [[ -z "$source_path" || -z "$dest_path" ]]; then
        log_error "Source and destination paths are required"
        usage
        exit 1
    fi
    
    log_info "Starting Vault secret copy operation"
    log_info "Source: $source_path"
    log_info "Destination: $dest_path"
    log_info "Recursive: $RECURSIVE"
    log_info "Vault Address: $VAULT_ADDR"
    log_info "Vault Mount: $VAULT_MOUNT"
    log_info "KV Version: $VAULT_VERSION"
    
    # Check prerequisites
    check_vault_cli
    validate_vault_connection
    
    # Perform the copy
    copy_secrets "$source_path" "$dest_path"
    
    # Print summary
    log_info "=== Copy Summary ==="
    log_success "Successfully copied: $COPIED_COUNT secret(s)"
    if [[ $FAILED_COUNT -gt 0 ]]; then
        log_error "Failed to copy: $FAILED_COUNT secret(s)"
    fi
    log_success "Vault secret copy operation completed"
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 
