#!/bin/bash

# Vault Environment Configuration
# This file contains common environment variables and functions for all Vault scripts
# Source this file in other scripts: source "$(dirname "${BASH_SOURCE[0]}")/vault-env.sh"

# Vault Configuration
export VAULT_ADDR="${VAULT_ADDR:-https://mg2vdmvxdh4s8i6w9hsyuxvg.prolinkli.com}"
export VAULT_NAMESPACE="${VAULT_NAMESPACE:-}"
export VAULT_FORMAT="${VAULT_FORMAT:-json}"

# KV Engine Configuration
export VAULT_KV_VERSION="${VAULT_KV_VERSION:-v2}"
export VAULT_KV_MOUNT="${VAULT_KV_MOUNT:-secret}"

# Token and Authentication
export VAULT_TOKEN_FILE="${VAULT_TOKEN_FILE:-$HOME/.vault-token}"
export VAULT_MIN_TTL_SECONDS="${VAULT_MIN_TTL_SECONDS:-300}"  # 5 minutes

# Vault Paths for different secret types
export VAULT_DATABASE_PATH="${VAULT_DATABASE_PATH:-secret/spring/database}"
export VAULT_JWT_PATH="${VAULT_JWT_PATH:-secret/spring/jwt}"
export VAULT_SECURITY_PATH="${VAULT_SECURITY_PATH:-secret/spring/security}"
export VAULT_OAUTH_PATH="${VAULT_OAUTH_PATH:-secret/spring/oauth}"
export VAULT_APP_PATH="${VAULT_APP_PATH:-secret/spring/app}"
export VAULT_CONFIG_PATH="${VAULT_CONFIG_PATH:-secret/spring/config}"

# Default policies
export VAULT_ADMIN_POLICY="${VAULT_ADMIN_POLICY:-github-admin}"
export VAULT_USER_POLICY="${VAULT_USER_POLICY:-github-user}"

# Script paths (calculated relative to this file)
VAULT_SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$VAULT_SCRIPTS_DIR/../.." && pwd)"

# Common functions for all vault scripts
vault_env_log_info() { echo "[INFO] $*"; }
vault_env_log_success() { echo "[SUCCESS] $*"; }
vault_env_log_warn() { echo "[WARNING] $*"; }
vault_env_log_error() { echo "[ERROR] $*" >&2; }

# Function to check if vault CLI is installed
vault_env_check_cli() {
    if ! command -v vault &> /dev/null; then
        vault_env_log_error "Vault CLI is not installed. Please install HashiCorp Vault CLI first."
        echo "Installation instructions: https://developer.hashicorp.com/vault/docs/install"
        return 1
    fi
    return 0
}

# Function to test vault connection
vault_env_test_connection() {
    if ! vault status &> /dev/null; then
        vault_env_log_error "Cannot connect to Vault at $VAULT_ADDR"
        vault_env_log_error "Please check your VAULT_ADDR and ensure Vault is accessible"
        return 1
    fi
    return 0
}

# Function to load vault token from file
vault_env_load_token() {
    if [[ -f "$VAULT_TOKEN_FILE" ]]; then
        local token=$(cat "$VAULT_TOKEN_FILE" 2>/dev/null)
        if [[ -n "$token" ]]; then
            export VAULT_TOKEN="$token"
            return 0
        fi
    fi
    return 1
}

# Function to check if currently authenticated
vault_env_check_auth() {
    vault_env_load_token
    if ! vault token lookup &> /dev/null; then
        vault_env_log_error "Not authenticated to Vault. Please authenticate first:"
        vault_env_log_info "Run: ./vault-auth.sh"
        return 1
    fi
    return 0
}

# Function to get vault server info
vault_env_info() {
    vault_env_log_info "Vault Environment Configuration:"
    vault_env_log_info "  Server: $VAULT_ADDR"
    vault_env_log_info "  KV Version: $VAULT_KV_VERSION"
    vault_env_log_info "  KV Mount: $VAULT_KV_MOUNT"
    vault_env_log_info "  Token File: $VAULT_TOKEN_FILE"
    vault_env_log_info "  Min TTL: ${VAULT_MIN_TTL_SECONDS}s"
    
    if [[ -n "$VAULT_NAMESPACE" ]]; then
        vault_env_log_info "  Namespace: $VAULT_NAMESPACE"
    fi
    
    echo
    vault_env_log_info "Secret Paths:"
    vault_env_log_info "  Database: $VAULT_DATABASE_PATH"
    vault_env_log_info "  JWT: $VAULT_JWT_PATH"
    vault_env_log_info "  Security: $VAULT_SECURITY_PATH"
    vault_env_log_info "  OAuth: $VAULT_OAUTH_PATH"
    vault_env_log_info "  Application: $VAULT_APP_PATH"
    vault_env_log_info "  Config: $VAULT_CONFIG_PATH"
}

# Function to open Vault UI
vault_env_open_ui() {
    local vault_ui_url="$VAULT_ADDR/ui"
    
    vault_env_log_info "Opening Vault UI at: $vault_ui_url"
    
    # Try to open the URL in the default browser
    if command -v open &> /dev/null; then
        open "$vault_ui_url"
    elif command -v xdg-open &> /dev/null; then
        xdg-open "$vault_ui_url"
    else
        vault_env_log_info "Please open this URL in your browser:"
        echo "$vault_ui_url"
    fi
    
    vault_env_log_info "Available authentication methods:"
    vault_env_log_info "  - Username/Password (Method: Username)"
    vault_env_log_info "  - GitHub (Method: GitHub)"
    vault_env_log_info "  - Root Token (Method: Token)"
    echo
    vault_env_log_info "Test credentials:"
    vault_env_log_info "  Admin: username=admin, password=admin123"
    vault_env_log_info "  User: username=user, password=user123"
}

# Function to construct KV path based on version
vault_env_kv_path() {
    local path="$1"
    case "$VAULT_KV_VERSION" in
        "v2")
            echo "${VAULT_KV_MOUNT}/data/${path}"
            ;;
        "v1"|*)
            echo "${VAULT_KV_MOUNT}/${path}"
            ;;
    esac
}

# Function to construct KV metadata path (v2 only)
vault_env_kv_metadata_path() {
    local path="$1"
    if [[ "$VAULT_KV_VERSION" == "v2" ]]; then
        echo "${VAULT_KV_MOUNT}/metadata/${path}"
    else
        echo "${VAULT_KV_MOUNT}/${path}"
    fi
}

# Function to read a secret using the correct KV path
vault_env_read_secret() {
    local path="$1"
    local kv_path=$(vault_env_kv_path "$path")
    vault read -format=json "$kv_path" 2>/dev/null
}

# Function to write a secret using the correct KV path
vault_env_write_secret() {
    local path="$1"
    shift  # Remove path from arguments, rest are key=value pairs
    local kv_path=$(vault_env_kv_path "$path")
    vault write "$kv_path" "$@"
}

# Function to list secrets using the correct KV path
vault_env_list_secrets() {
    local path="$1"
    local metadata_path=$(vault_env_kv_metadata_path "$path")
    vault list "$metadata_path" 2>/dev/null
}

# Initialization - auto-load token if available
vault_env_load_token &>/dev/null

# Show environment info if run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    vault_env_info
fi 