#!/bin/bash

source ./bash/log.sh

# This script is used to start the development server for the project.

log_info "Starting development server"

# Check Vault session before starting
log_info "Checking Vault authentication..."
if ! ./bash/vault/check_vault_session.sh "Spring Boot Development Server"; then
    log_error "Cannot start development server without valid Vault authentication"
    exit 1
fi

./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=local-dev \
  -Dspring-boot.run.jvmArguments="-Xmx1024m -Xms512m \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  -Dlogging.level.liquibase=DEBUG
