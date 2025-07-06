#!/bin/bash

# Load vault environment configuration
source "$(dirname "${BASH_SOURCE[0]}")/vault-env.sh"

source ../log.sh

# Vault Authentication Script
# This script authenticates to Vault using various methods
# Usage: ./vault-auth.sh [method]

# Configuration (using vault-env.sh values)
TOKEN_FILE="$VAULT_TOKEN_FILE"
AUTH_METHOD="${1:-auto}"

# Function to check if vault CLI is installed (using vault-env.sh)
check_vault_cli() {
    if ! vault_env_check_cli; then
        exit 1
    fi
}

# Function to test vault connection (using vault-env.sh)
test_vault_connection() {
    vault_env_test_connection
}

# Function to check if already authenticated
check_existing_auth() {
    if [[ -f "$TOKEN_FILE" ]]; then
        local token=$(cat "$TOKEN_FILE")
        if [[ -n "$token" ]]; then
            export VAULT_TOKEN="$token"
            if vault token lookup &> /dev/null; then
                log_success "Already authenticated with existing token"
                
                # Show token info
                local token_info=$(vault token lookup -format=json 2>/dev/null)
                if [[ -n "$token_info" ]]; then
                    local display_name=$(echo "$token_info" | jq -r '.data.display_name // "N/A"')
                    local policies=$(echo "$token_info" | jq -r '.data.policies | join(", ")')
                    local ttl=$(echo "$token_info" | jq -r '.data.ttl // "N/A"')
                    
                    log_info "Token Display Name: $display_name"
                    log_info "Policies: $policies"
                    log_info "TTL: $ttl seconds"
                fi
                
                return 0
            else
                log_info "Existing token is invalid or expired"
                rm -f "$TOKEN_FILE"
            fi
        fi
    fi
    return 1
}

# Function to save token
save_token() {
    local token="$1"
    echo "$token" > "$TOKEN_FILE"
    chmod 600 "$TOKEN_FILE"
    export VAULT_TOKEN="$token"
    log_success "Token saved to $TOKEN_FILE"
}

# Function to get available auth methods
get_auth_methods() {
    vault auth list -format=json 2>/dev/null | jq -r 'keys[]' | sed 's/\/$//'
}

# Function to authenticate with userpass
auth_userpass() {
    log_info "Authenticating with userpass method"
    
    read -p "Username: " username
    read -s -p "Password: " password
    echo
    
    local auth_response
    auth_response=$(vault write -format=json auth/userpass/login/$username password="$password" 2>/dev/null)
    
    if [[ $? -eq 0 ]]; then
        local token=$(echo "$auth_response" | jq -r '.auth.client_token')
        if [[ -n "$token" && "$token" != "null" ]]; then
            save_token "$token"
            log_success "Successfully authenticated with userpass"
            return 0
        fi
    fi
    
    log_error "Userpass authentication failed"
    return 1
}

# Function to authenticate with OIDC (web-based)
auth_oidc() {
    log_info "Authenticating with OIDC method"
    
    # Check if we can detect the role
    local role="${VAULT_OIDC_ROLE:-}"
    if [[ -z "$role" ]]; then
        read -p "OIDC Role (press Enter for default): " role
        if [[ -z "$role" ]]; then
            role="default"
        fi
    fi
    
    log_info "Starting OIDC authentication flow..."
    log_info "This will open a browser window for authentication"
    
    # Start OIDC auth and capture the response
    local auth_response
    auth_response=$(vault write -format=json auth/oidc/login role="$role" 2>/dev/null)
    
    if [[ $? -eq 0 ]]; then
        local auth_url=$(echo "$auth_response" | jq -r '.data.auth_url // empty')
        
        if [[ -n "$auth_url" ]]; then
            log_info "Opening browser for authentication..."
            
            # Try to open the URL in the default browser
            if command -v open &> /dev/null; then
                open "$auth_url"
            elif command -v xdg-open &> /dev/null; then
                xdg-open "$auth_url"
            else
                log_info "Please open this URL in your browser:"
                echo "$auth_url"
            fi
            
            # Wait for the user to complete authentication
            log_info "Please complete authentication in your browser..."
            read -p "Press Enter after completing authentication in the browser..."
            
            # Try to complete the auth flow
            local complete_response
            complete_response=$(vault write -format=json auth/oidc/callback 2>/dev/null)
            
            if [[ $? -eq 0 ]]; then
                local token=$(echo "$complete_response" | jq -r '.auth.client_token // empty')
                if [[ -n "$token" && "$token" != "null" ]]; then
                    save_token "$token"
                    log_success "Successfully authenticated with OIDC"
                    return 0
                fi
            fi
        fi
    fi
    
    log_error "OIDC authentication failed"
    return 1
}

# Function to authenticate with GitHub
auth_github() {
    log_info "Authenticating with GitHub method"
    
    read -p "GitHub Personal Access Token: " -s github_token
    echo
    
    local auth_response
    auth_response=$(vault write -format=json auth/github/login token="$github_token" 2>/dev/null)
    
    if [[ $? -eq 0 ]]; then
        local token=$(echo "$auth_response" | jq -r '.auth.client_token')
        if [[ -n "$token" && "$token" != "null" ]]; then
            save_token "$token"
            log_success "Successfully authenticated with GitHub"
            return 0
        fi
    fi
    
    log_error "GitHub authentication failed"
    return 1
}

# Function to authenticate with LDAP
auth_ldap() {
    log_info "Authenticating with LDAP method"
    
    read -p "LDAP Username: " username
    read -s -p "LDAP Password: " password
    echo
    
    local auth_response
    auth_response=$(vault write -format=json auth/ldap/login/$username password="$password" 2>/dev/null)
    
    if [[ $? -eq 0 ]]; then
        local token=$(echo "$auth_response" | jq -r '.auth.client_token')
        if [[ -n "$token" && "$token" != "null" ]]; then
            save_token "$token"
            log_success "Successfully authenticated with LDAP"
            return 0
        fi
    fi
    
    log_error "LDAP authentication failed"
    return 1
}

# Function to authenticate with manual token
auth_token() {
    log_info "Manual token authentication"
    
    read -p "Enter your Vault token: " -s token
    echo
    
    if [[ -n "$token" ]]; then
        export VAULT_TOKEN="$token"
        if vault token lookup &> /dev/null; then
            save_token "$token"
            log_success "Successfully authenticated with token"
            return 0
        fi
    fi
    
    log_error "Token authentication failed"
    return 1
}

# Function to auto-detect and authenticate
auth_auto() {
    log_info "Auto-detecting available authentication methods..."
    
    local auth_methods
    auth_methods=$(get_auth_methods)
    
    if [[ -z "$auth_methods" ]]; then
        log_error "Could not retrieve authentication methods"
        return 1
    fi
    
    log_info "Available authentication methods:"
    echo "$auth_methods" | sed 's/^/  - /'
    
    # Try methods in order of preference
    if echo "$auth_methods" | grep -q "oidc"; then
        log_info "Trying OIDC authentication..."
        if auth_oidc; then
            return 0
        fi
    fi
    
    if echo "$auth_methods" | grep -q "userpass"; then
        log_info "Trying userpass authentication..."
        if auth_userpass; then
            return 0
        fi
    fi
    
    if echo "$auth_methods" | grep -q "github"; then
        log_info "Trying GitHub authentication..."
        if auth_github; then
            return 0
        fi
    fi
    
    if echo "$auth_methods" | grep -q "ldap"; then
        log_info "Trying LDAP authentication..."
        if auth_ldap; then
            return 0
        fi
    fi
    
    # Fallback to manual token
    log_info "Fallback to manual token entry..."
    auth_token
}

# Function to open Vault UI
open_vault_ui() {
    local vault_ui_url="$VAULT_ADDR/ui"
    
    log_info "Opening Vault UI at $vault_ui_url"
    
    if command -v open &> /dev/null; then
        open "$vault_ui_url"
    elif command -v xdg-open &> /dev/null; then
        xdg-open "$vault_ui_url"
    else
        log_info "Please open this URL in your browser:"
        echo "$vault_ui_url"
    fi
}

# Function to display usage
usage() {
    echo "Usage: $0 [method]"
    echo ""
    echo "Authentication Methods:"
    echo "  auto       - Auto-detect and try available methods (default)"
    echo "  userpass   - Username/password authentication"
    echo "  oidc       - OpenID Connect (web-based)"
    echo "  github     - GitHub personal access token"
    echo "  ldap       - LDAP authentication"
    echo "  token      - Manual token entry"
    echo "  ui         - Open Vault UI in browser"
    echo ""
    echo "Environment Variables:"
    echo "  VAULT_ADDR      - Vault server address (default: http://10.2.2.2:8200)"
    echo "  VAULT_NAMESPACE - Vault namespace (if using Vault Enterprise)"
    echo "  VAULT_OIDC_ROLE - OIDC role to use (default: default)"
    echo ""
    echo "Examples:"
    echo "  ./vault-auth.sh                    # Auto-detect method"
    echo "  ./vault-auth.sh userpass           # Use userpass"
    echo "  ./vault-auth.sh oidc               # Use OIDC/web auth"
    echo "  ./vault-auth.sh ui                 # Open Vault UI"
    echo ""
    echo "Token will be saved to: $TOKEN_FILE"
}

# Function to logout
logout() {
    if [[ -f "$TOKEN_FILE" ]]; then
        rm -f "$TOKEN_FILE"
        log_success "Logged out and removed token file"
    fi
    unset VAULT_TOKEN
}

# Main script execution
main() {
    log_info "Vault Authentication Script"
    log_info "Vault Address: $VAULT_ADDR"
    
    # Check prerequisites
    check_vault_cli
    
    # Set Vault address
    export VAULT_ADDR="$VAULT_ADDR"
    if [[ -n "$VAULT_NAMESPACE" ]]; then
        export VAULT_NAMESPACE="$VAULT_NAMESPACE"
    fi
    
    # Test connection
    if ! test_vault_connection; then
        exit 1
    fi
    
    # Handle special cases
    case "$AUTH_METHOD" in
        "logout")
            logout
            exit 0
            ;;
        "ui")
            open_vault_ui
            exit 0
            ;;
        "help" | "--help" | "-h")
            usage
            exit 0
            ;;
    esac
    
    # Check if already authenticated
    if check_existing_auth; then
        read -p "Already authenticated. Re-authenticate? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 0
        fi
    fi
    
    # Authenticate based on method
    case "$AUTH_METHOD" in
        "auto")
            auth_auto
            ;;
        "userpass")
            auth_userpass
            ;;
        "oidc")
            auth_oidc
            ;;
        "github")
            auth_github
            ;;
        "ldap")
            auth_ldap
            ;;
        "token")
            auth_token
            ;;
        *)
            log_error "Unknown authentication method: $AUTH_METHOD"
            usage
            exit 1
            ;;
    esac
    
    if [[ $? -eq 0 ]]; then
        log_success "Authentication completed successfully!"
        log_info "You can now use other Vault scripts"
        log_info "Token saved to: $TOKEN_FILE"
    else
        log_error "Authentication failed"
        exit 1
    fi
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 