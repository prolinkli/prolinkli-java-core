#!/bin/bash

# Find the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Source log.sh from the correct location
if [[ -f "$SCRIPT_DIR/../log.sh" ]]; then
    source "$SCRIPT_DIR/../log.sh"
elif [[ -f "$PROJECT_ROOT/bash/log.sh" ]]; then
    source "$PROJECT_ROOT/bash/log.sh"
else
    # Fallback: define basic logging functions if log.sh not found
    log_info() { echo "[INFO] $*"; }
    log_success() { echo "[SUCCESS] $*"; }
    log_warn() { echo "[WARNING] $*"; }
    log_error() { echo "[ERROR] $*" >&2; }
fi

# Vault User Management Script
# This script helps manage Vault users and provides web UI access

# Configuration
VAULT_ADDR="${VAULT_ADDR:-http://10.2.2.2:8200}"
VAULT_NAMESPACE="${VAULT_NAMESPACE:-}"

# Function to check if vault CLI is installed
check_vault_cli() {
    if ! command -v vault &> /dev/null; then
        log_error "Vault CLI is not installed. Please install HashiCorp Vault CLI first."
        echo "Installation instructions: https://developer.hashicorp.com/vault/docs/install"
        exit 1
    fi
}

# Function to test vault connection
test_vault_connection() {
    if ! vault status &> /dev/null; then
        log_error "Cannot connect to Vault at $VAULT_ADDR"
        log_error "Please check your VAULT_ADDR and ensure Vault is accessible"
        return 1
    fi
    return 0
}

# Function to check if authenticated
check_auth() {
    if ! vault token lookup &> /dev/null; then
        log_error "Not authenticated to Vault. Please authenticate first:"
        log_info "Run: ./vault-auth.sh"
        return 1
    fi
    return 0
}

# Function to open Vault UI
open_vault_ui() {
    local url="$VAULT_ADDR/ui"
    
    log_info "Opening Vault UI at: $url"
    
    # Try to open the URL in the default browser
    if command -v open &> /dev/null; then
        open "$url"
    elif command -v xdg-open &> /dev/null; then
        xdg-open "$url"
    else
        log_info "Please open this URL in your browser:"
        echo "$url"
    fi
    
    log_info "Available authentication methods:"
    log_info "  - Username/Password (Method: Username)"
    log_info "  - GitHub (Method: GitHub)"
    log_info "  - Root Token (Method: Token)"
    echo
    log_info "Test credentials:"
    log_info "  Admin: username=admin, password=admin123"
    log_info "  User: username=user, password=user123"
}

# Function to create a new user
create_user() {
    local username="$1"
    local password="$2"
    local policy="${3:-github-user}"
    
    if [[ -z "$username" || -z "$password" ]]; then
        log_error "Username and password are required"
        return 1
    fi
    
    log_info "Creating user: $username with policy: $policy"
    
    if vault write auth/userpass/users/$username password="$password" policies="$policy"; then
        log_success "Successfully created user: $username"
        log_info "User can now login with:"
        log_info "  Username: $username"
        log_info "  Password: $password"
        log_info "  Policy: $policy"
        return 0
    else
        log_error "Failed to create user: $username"
        return 1
    fi
}

# Function to list users
list_users() {
    log_info "Listing all userpass users:"
    
    if vault list auth/userpass/users 2>/dev/null; then
        echo
        log_info "To see user details, run:"
        log_info "  vault read auth/userpass/users/USERNAME"
    else
        log_info "No users found or unable to list users"
    fi
}

# Function to delete a user
delete_user() {
    local username="$1"
    
    if [[ -z "$username" ]]; then
        log_error "Username is required"
        return 1
    fi
    
    log_warn "Deleting user: $username"
    read -p "Are you sure? (y/N): " confirm
    
    if [[ "$confirm" =~ ^[Yy]$ ]]; then
        if vault delete auth/userpass/users/$username; then
            log_success "Successfully deleted user: $username"
            return 0
        else
            log_error "Failed to delete user: $username"
            return 1
        fi
    else
        log_info "User deletion cancelled"
        return 0
    fi
}

# Function to update user password
update_password() {
    local username="$1"
    local new_password="$2"
    
    if [[ -z "$username" ]]; then
        log_error "Username is required"
        return 1
    fi
    
    if [[ -z "$new_password" ]]; then
        read -s -p "Enter new password for $username: " new_password
        echo
    fi
    
    # Get current user info to preserve policies
    local user_info=$(vault read -format=json auth/userpass/users/$username 2>/dev/null)
    if [[ $? -ne 0 ]]; then
        log_error "User $username not found"
        return 1
    fi
    
    local policies=$(echo "$user_info" | jq -r '.data.policies | join(",")')
    
    if vault write auth/userpass/users/$username password="$new_password" policies="$policies"; then
        log_success "Successfully updated password for user: $username"
        return 0
    else
        log_error "Failed to update password for user: $username"
        return 1
    fi
}

# Function to show user info
show_user_info() {
    local username="$1"
    
    if [[ -z "$username" ]]; then
        log_error "Username is required"
        return 1
    fi
    
    log_info "User information for: $username"
    vault read auth/userpass/users/$username
}

# Function to show help
show_help() {
    cat << EOF
Vault User Management Script

Usage: $0 [command] [options]

Commands:
    ui                          Open Vault UI in browser
    create <username> <password> [policy]  Create a new user
    list                        List all users
    delete <username>           Delete a user
    password <username> [new_password]     Update user password
    info <username>             Show user information
    help                        Show this help message

Examples:
    $0 ui                       # Open Vault UI
    $0 create john secret123    # Create user with default policy
    $0 create admin admin123 github-admin  # Create admin user
    $0 list                     # List all users
    $0 delete john              # Delete user john
    $0 password john            # Update john's password (interactive)
    $0 password john newpass123 # Update john's password
    $0 info john                # Show john's user info

Available Policies:
    github-admin    Full administrative access
    github-user     Limited read-only access

Environment Variables:
    VAULT_ADDR      Vault server address (default: http://10.2.2.2:8200)
    VAULT_TOKEN     Vault authentication token
    VAULT_NAMESPACE Vault namespace (if using Enterprise)

EOF
}

# Main script logic
main() {
    check_vault_cli
    
    if [[ $# -eq 0 ]]; then
        show_help
        exit 0
    fi
    
    local command="$1"
    shift
    
    case "$command" in
        ui)
            open_vault_ui
            ;;
        create)
            if ! test_vault_connection; then
                exit 1
            fi
            if ! check_auth; then
                exit 1
            fi
            create_user "$@"
            ;;
        list)
            if ! test_vault_connection; then
                exit 1
            fi
            if ! check_auth; then
                exit 1
            fi
            list_users
            ;;
        delete)
            if ! test_vault_connection; then
                exit 1
            fi
            if ! check_auth; then
                exit 1
            fi
            delete_user "$1"
            ;;
        password)
            if ! test_vault_connection; then
                exit 1
            fi
            if ! check_auth; then
                exit 1
            fi
            update_password "$1" "$2"
            ;;
        info)
            if ! test_vault_connection; then
                exit 1
            fi
            if ! check_auth; then
                exit 1
            fi
            show_user_info "$1"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main "$@" 