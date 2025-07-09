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

### 2. `manage-users.sh` 🆕

Manages Vault users and provides easy web UI access for authentication.

**Usage:**
```bash
./manage-users.sh [command] [options]
```

**Commands:**
- `ui` - Open Vault UI in browser
- `create <username> <password> [policy]` - Create a new user
- `list` - List all users
- `delete <username>` - Delete a user
- `password <username> [new_password]` - Update user password
- `info <username>` - Show user information

**Examples:**
```bash
# Open Vault UI for easy login
./manage-users.sh ui

# Create a new user
./manage-users.sh create developer dev123

# Create an admin user
./manage-users.sh create admin admin123 github-admin

# List all users
./manage-users.sh list

# Update password
./manage-users.sh password developer newpass123

# Delete a user
./manage-users.sh delete developer
```

### 3. `check_vault_session.sh` 🆕

Checks if the user is authenticated to Vault and if their session is valid. Used as a pre-startup check for Spring applications.

**Usage:**
```bash
./check_vault_session.sh [operation_name]
```

**Features:**
- Validates Vault token and checks expiration
- Automatically opens Vault UI if session is expired
- Provides helpful renewal instructions
- Integrates with Spring Boot startup scripts

**Examples:**
```bash
# Check session for general use
./check_vault_session.sh

# Check session for specific operation
./check_vault_session.sh "Spring Boot Development"
```

**Integration:**
This script is automatically called by:
- `start-dev.sh` - Spring Boot development server
- `run-mybatis.sh` - MyBatis generator
- `run-liquibase.sh` - Liquibase operations

### 4. `vault-copy-secrets.sh`

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

### 5. `add-java-core-member.sh`

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

## Quick Start 🚀

### For New Users

1. **Open Vault UI and Login:**
   ```bash
   cd bash/vault
   ./manage-users.sh ui
   ```
   
   Use these credentials:
   - **Admin**: username=`admin`, password=`admin123`
   - **User**: username=`user`, password=`user123`

2. **Or authenticate via CLI:**
   ```bash
   ./vault-auth.sh
   ```

3. **Start development server:**
   ```bash
   cd ../..
   ./start-dev.sh
   ```

### For Admins

1. **Create new users:**
   ```bash
   cd bash/vault
   ./manage-users.sh create newuser password123
   ```

2. **Manage existing users:**
   ```bash
   ./manage-users.sh list
   ./manage-users.sh info username
   ./manage-users.sh password username
   ```

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

## Automatic Session Management 🔄

The system now automatically checks your Vault session before running Spring operations:

- **Spring Boot Development**: `./start-dev.sh` checks session before startup
- **MyBatis Generator**: `./run-mybatis.sh` checks session before generation
- **Liquibase**: `./run-liquibase.sh` checks session before database operations

If your session is expired, you'll see:
```
🔐 Uh Oh! Your Vault Session has expired...

🔄 To renew your session, you can:
  Option 1: Web UI (Recommended)
    Click this link: http://10.2.2.2:8200/ui
```

The system will automatically open the Vault UI in your browser for easy re-authentication.

## Available User Policies

- **github-admin**: Full administrative access to all Vault operations
- **github-user**: Limited read-only access to specific secret paths

## Security Notes

- Never commit Vault tokens to version control
- Use `.env` files for local development (already in `.gitignore`)
- In production, use proper authentication methods (AppRole, Kubernetes auth, etc.)
- Keep Vault tokens secure and rotate them regularly
- Session checks ensure expired tokens are renewed automatically

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

5. **"Session expired" during Spring operations**
   - Follow the renewal instructions displayed
   - Use `./manage-users.sh ui` for quick web login

**Getting Help:**
- Use `--help` or run scripts without arguments to see usage information
- Check the script logs for detailed error messages
- Consult your DevOps team for Vault configuration details 