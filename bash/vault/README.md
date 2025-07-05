# Vault Scripts

This directory contains scripts for managing HashiCorp Vault operations and group membership for the Java Core project.

## Scripts

### 1. `vault-auth.sh`

Authenticates to Vault using various methods and saves the token for other scripts to use.

**Usage:**
```bash
./vault-auth.sh [method]
```

**Authentication Methods:**
- `auto` - Auto-detect and try available methods (default)
- `userpass` - Username/password authentication
- `oidc` - OpenID Connect (web-based authentication)
- `github` - GitHub personal access token
- `ldap` - LDAP authentication
- `token` - Manual token entry
- `ui` - Open Vault UI in browser

**Examples:**
```bash
# Auto-detect authentication method
./vault-auth.sh

# Use web-based OIDC authentication
./vault-auth.sh oidc

# Use username/password
./vault-auth.sh userpass

# Open Vault UI in browser
./vault-auth.sh ui

# Logout and remove saved token
./vault-auth.sh logout
```

### 2. `vault-copy-secrets.sh`

Copies secrets from one Vault path to another.

**Usage:**
```bash
./vault-copy-secrets.sh <source_path> <destination_path>
```

**Examples:**
```bash
# First authenticate (if not already done)
./vault-auth.sh

# Copy production secrets to staging
./vault-copy-secrets.sh app/prod/config app/staging/config

# Copy database secrets
./vault-copy-secrets.sh database/prod database/staging
```

**Required:**
- Authentication via `vault-auth.sh` or `VAULT_TOKEN` environment variable

**Optional Environment Variables:**
- `VAULT_NAMESPACE`: Vault namespace (if using Vault Enterprise)
- `VAULT_MOUNT`: Vault mount point (default: kv)
- `VAULT_VERSION`: KV engine version - v1 or v2 (default: v2)

### 3. `add-java-core-member.sh`

Adds users to the java-core group for project access. Handles both system-level group membership and Vault identity group membership.

**Usage:**
```bash
./add-java-core-member.sh <username> [<username2> ...]
```

**Examples:**
```bash
# First authenticate (if not already done)
./vault-auth.sh

# Add single user
./add-java-core-member.sh john.doe

# Add multiple users
./add-java-core-member.sh alice bob charlie
```

**Required:**
- Authentication via `vault-auth.sh` or `VAULT_TOKEN` environment variable

**Optional Environment Variables:**
- `VAULT_ADDR`: Vault server address (default: http://10.2.2.2:8200)

## Setup

1. **Install Vault CLI**:
   ```bash
   # macOS
   brew install vault
   
   # Linux
   curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add -
   sudo apt-add-repository "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main"
   sudo apt-get update && sudo apt-get install vault
   ```

2. **Set Environment Variables** (Optional):
   ```bash
   export VAULT_ADDR=http://10.2.2.2:8200
   ```

3. **Authenticate to Vault**:
   Use the authentication script to get a token:
   ```bash
   cd bash/vault
   ./vault-auth.sh
   ```
   This will automatically detect available authentication methods and guide you through the process.

4. **Verify Connection**:
   ```bash
   vault status
   vault token lookup
   ```

## Security Notes

- Never commit Vault tokens to version control
- Use `.env` files for local development (already in `.gitignore`)
- In production, use proper authentication methods (AppRole, Kubernetes auth, etc.)
- Keep Vault tokens secure and rotate them regularly

## Troubleshooting

**Common Issues:**

1. **"vault command not found"**
   - Install the Vault CLI (see Setup section)

2. **"Authentication failed"**
   - Check your `VAULT_TOKEN` environment variable
   - Verify the token is valid: `vault token lookup`

3. **"Cannot connect to Vault"**
   - Check your `VAULT_ADDR` environment variable
   - Ensure Vault server is running and accessible

4. **"Permission denied" for group operations**
   - Ensure you have sudo privileges
   - Run the script with appropriate permissions

**Getting Help:**
- Use `--help` or run scripts without arguments to see usage information
- Check the script logs for detailed error messages
- Consult your DevOps team for Vault configuration details 