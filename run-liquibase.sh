#!/bin/bash

# Source logging functions
source ./bash/log.sh

# Check Vault session before starting
log_info "Checking Vault authentication..."
if ! ./bash/vault/check_vault_session.sh "Liquibase"; then
    log_error "Cannot run Liquibase without valid Vault authentication"
    exit 1
fi

# Primitive for now
mvn liquibase:clearCheckSums  \
  -Dliquibase.url=jdbc:postgresql://localhost:6543/postgres \
  -Dliquibase.username=postgres \
  -Dliquibase.password=docker \
  -Dliquibase.driver=org.postgresql.Driver
