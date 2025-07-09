#!/bin/bash

# Find the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Load vault environment configuration
source "$(dirname "${BASH_SOURCE[0]}")/vault-env.sh"

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

# Vault Session Check Script
# This script checks if the user is authenticated to Vault and if their session is valid
# Used as a pre-startup check for Spring applications

# Configuration (using vault-env.sh values)
TOKEN_FILE="$VAULT_TOKEN_FILE"
MIN_TTL_SECONDS="$VAULT_MIN_TTL_SECONDS"

# Function to check if vault CLI is installed (using vault-env.sh)
check_vault_cli() {
    vault_env_check_cli
}

# Function to test vault connection (using vault-env.sh)
test_vault_connection() {
    vault_env_test_connection
}

# Function to check if token exists and is valid
check_token_validity() {
    local token_info
    local ttl
    local display_name
    local policies
    
    # Check if token file exists
    if [[ ! -f "$TOKEN_FILE" ]]; then
        log_warn "No Vault token found at $TOKEN_FILE"
        return 1
    fi
    
    # Load token
    local token=$(cat "$TOKEN_FILE")
    if [[ -z "$token" ]]; then
        log_warn "Empty Vault token found"
        return 1
    fi
    
    # Set token for vault commands
    export VAULT_TOKEN="$token"
    
    # Check token validity
    token_info=$(vault token lookup -format=json 2>/dev/null)
    if [[ $? -ne 0 ]]; then
        log_warn "Vault token is invalid or expired"
        return 1
    fi
    
    # Extract token information
    ttl=$(echo "$token_info" | jq -r '.data.ttl // 0')
    display_name=$(echo "$token_info" | jq -r '.data.display_name // "N/A"')
    policies=$(echo "$token_info" | jq -r '.data.policies | join(", ")')
    
    # Check if token has sufficient TTL
    if [[ "$ttl" -lt "$MIN_TTL_SECONDS" ]]; then
        log_warn "Vault token expires soon (TTL: ${ttl}s, required: ${MIN_TTL_SECONDS}s)"
        return 2  # Special return code for soon-to-expire token
    fi
    
    # Token is valid
    log_success "Vault session is valid"
    log_info "  User: $display_name"
    log_info "  Policies: $policies"
    log_info "  TTL: ${ttl}s"
    
    return 0
}

# Function to get token from web UI
get_token_from_web() {
    local vault_ui_url="$VAULT_ADDR/ui"
    
    echo
    log_info "🌐 To get your token from the Vault Web UI:"
    echo
    log_info "1. Open Vault UI: $vault_ui_url"
    log_info "2. Login with your credentials:"
    log_info "   - Username: admin, Password: admin123 (for admin access)"
    log_info "   - Username: user, Password: user123 (for user access)"
    log_info "3. After login, click your profile icon (top right)"
    log_info "4. Click 'Copy token' to copy your token"
    log_info "5. Come back here and paste it"
    echo
    
    # Try to open the URL automatically (using vault-env.sh function)
    vault_env_open_ui
    
    echo
    log_info "💡 How to copy your token after logging in:"
    log_info "   1. Look for your username/profile in the top right corner"
    log_info "   2. Click on it to open the dropdown menu"
    log_info "   3. Click 'Copy token' or look for the token value"
    log_info "   4. Copy the token (it starts with 'hvs.' or 's.')"
    echo
    
    # Get token from user
    local token=""
    while [[ -z "$token" ]]; do
        read -p "📋 Paste your Vault token here: " token
        if [[ -z "$token" ]]; then
            log_warn "Token cannot be empty. Please try again."
        fi
    done
    
    # Test the token
    export VAULT_TOKEN="$token"
    if vault token lookup &>/dev/null; then
        # Save the token
        echo "$token" > "$TOKEN_FILE"
        chmod 600 "$TOKEN_FILE"
        log_success "✅ Token saved successfully!"
        return 0
    else
        log_error "❌ Invalid token. Please try again."
        return 1
    fi
}

# Function to show authentication renewal message
show_auth_renewal_message() {
    local reason="$1"
    local vault_ui_url="$VAULT_ADDR/ui"
    
    echo
    log_error "🔐 Uh Oh! Your Vault Session has expired..."
    echo
    
    case "$reason" in
        "no_token")
            log_info "Reason: No authentication token found"
            ;;
        "invalid_token")
            log_info "Reason: Authentication token is invalid"
            ;;
        "expired_token")
            log_info "Reason: Authentication token has expired"
            ;;
        "expiring_soon")
            log_info "Reason: Authentication token expires soon"
            ;;
        "no_connection")
            log_info "Reason: Cannot connect to Vault server"
            ;;
        *)
            log_info "Reason: Authentication required"
            ;;
    esac
    
    echo
    log_info "🔄 Choose how to authenticate:"
    echo
    log_info "  [1] Web UI (Recommended - Get token from browser)"
    log_info "  [2] Command Line (Use vault-auth.sh)"
    log_info "  [3] Manual Token Entry"
    log_info "  [4] Cancel"
    echo
    
    while true; do
        read -p "Enter your choice (1-4): " choice
        case "$choice" in
            1)
                if get_token_from_web; then
                    log_success "🎉 Authentication successful!"
                    return 0
                else
                    log_error "❌ Authentication failed. Please try again."
                    continue
                fi
                ;;
            2)
                log_info "🔧 Running vault authentication script..."
                if [[ -f "$SCRIPT_DIR/vault-auth.sh" ]]; then
                    if "$SCRIPT_DIR/vault-auth.sh"; then
                        log_success "🎉 Authentication successful!"
                        return 0
                    else
                        log_error "❌ Authentication failed."
                        continue
                    fi
                else
                    log_error "vault-auth.sh not found. Falling back to manual token entry."
                    choice="3"
                fi
                ;;
            3)
                log_info "📝 Manual token entry:"
                local token=""
                read -p "Enter your Vault token: " token
                if [[ -n "$token" ]]; then
                    export VAULT_TOKEN="$token"
                    if vault token lookup &>/dev/null; then
                        echo "$token" > "$TOKEN_FILE"
                        chmod 600 "$TOKEN_FILE"
                        log_success "✅ Token saved successfully!"
                        return 0
                    else
                        log_error "❌ Invalid token."
                        continue
                    fi
                else
                    log_error "❌ Token cannot be empty."
                    continue
                fi
                ;;
            4)
                log_info "🚫 Authentication cancelled."
                return 1
                ;;
            *)
                log_warn "Invalid choice. Please enter 1, 2, 3, or 4."
                continue
                ;;
        esac
    done
}

# Function to perform the session check
perform_session_check() {
    local operation="${1:-Spring application}"
    
    log_info "🔍 Checking Vault session for $operation..."
    
    # Check if Vault CLI is available
    if ! check_vault_cli; then
        log_error "Cannot proceed without Vault CLI"
        return 1
    fi
    
    # Check connection to Vault
    if ! test_vault_connection; then
        show_auth_renewal_message "no_connection"
        return 1
    fi
    
    # Check token validity
    local token_check_result
    check_token_validity
    token_check_result=$?
    
    case "$token_check_result" in
        0)
            # Token is valid
            log_success "✅ Vault session is valid for $operation"
            return 0
            ;;
        1)
            # Token is invalid or missing
            if [[ ! -f "$TOKEN_FILE" ]]; then
                show_auth_renewal_message "no_token"
            else
                show_auth_renewal_message "invalid_token"
            fi
            return $?
            ;;
        2)
            # Token expires soon
            log_warn "⚠️  Vault session expires soon"
            show_auth_renewal_message "expiring_soon"
            return $?
            ;;
        *)
            # Unknown error
            show_auth_renewal_message "unknown"
            return $?
            ;;
    esac
}

# Function to show help
show_help() {
    cat << EOF
Vault Session Check Script

Usage: $0 [operation_name]

This script checks if the user is authenticated to Vault and if their session is valid.
If the session is expired or invalid, it provides options to renew authentication.

Parameters:
    operation_name    Name of the operation being performed (optional)

Examples:
    $0                          # Check session for general use
    $0 "Spring Boot"            # Check session for Spring Boot startup
    $0 "MyBatis Generator"      # Check session for MyBatis operations

Authentication Options:
    1. Web UI - Login through browser and copy token
    2. Command Line - Use vault-auth.sh script
    3. Manual - Enter token directly

Environment Variables:
    VAULT_ADDR          Vault server address (default: http://10.2.2.2:8200)
    VAULT_TOKEN         Vault authentication token (auto-loaded from ~/.vault-token)
    VAULT_NAMESPACE     Vault namespace (if using Enterprise)

Files:
    ~/.vault-token      Vault authentication token storage

Exit Codes:
    0    Session is valid
    1    Session is invalid or expired
    2    Cannot connect to Vault
    3    Vault CLI not available

EOF
}

# Main script logic
main() {
    local operation="${1:-application}"
    
    # Handle help requests
    if [[ "$operation" == "help" || "$operation" == "--help" || "$operation" == "-h" ]]; then
        show_help
        exit 0
    fi
    
    # Perform session check
    if perform_session_check "$operation"; then
        # Session is valid, script can continue
        exit 0
    else
        # Session is invalid, exit with error
        log_error "❌ Cannot proceed without valid Vault authentication"
        exit 1
    fi
}

# Run main function
main "$@" 