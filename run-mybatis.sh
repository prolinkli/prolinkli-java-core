#!/bin/bash

# Source logging functions
source ./bash/log.sh

# Check Vault session before starting
log_info "Checking Vault authentication..."
if ! ./bash/vault/check_vault_session.sh "MyBatis Generator"; then
    log_error "Cannot run MyBatis Generator without valid Vault authentication"
    exit 1
fi

# JDBC Configuration
JDBC_DRIVER="org.postgresql.Driver"
JDBC_URL="jdbc:postgresql://localhost:6543/postgres"
JDBC_USER="postgres"
JDBC_PASSWORD="docker"

JDBC_PARAMS="-Djdbc.driverClass=${JDBC_DRIVER} -Djdbc.url=${JDBC_URL} -Djdbc.userId=${JDBC_USER} -Djdbc.password=${JDBC_PASSWORD}"

log_info "Starting MyBatis Generator Process..."
log_info "Using database: ${JDBC_URL}"

# Step 1: Clean and compile the project
log_info "Step 1: Cleaning and compiling project..."
if mvn clean compile ${JDBC_PARAMS}; then
    log_success "Project compiled successfully"
else
    log_error "Failed to compile project"
    exit 1
fi

# Step 2: Package the project (creates original JAR)
log_info "Step 2: Packaging project to create JAR files..."
if mvn package -DskipTests ${JDBC_PARAMS}; then
    log_success "Project packaged successfully"
else
    log_error "Failed to package project"
    exit 1
fi

# Step 3: Install original JAR to local repository (required for MyBatis generator plugin)
log_info "Step 3: Installing original JAR to local Maven repository..."
if mvn install:install-file \
    -Dfile=target/framework-0.0.1-SNAPSHOT.jar.original \
    -DgroupId=com.prolinkli \
    -DartifactId=framework \
    -Dversion=0.0.1-SNAPSHOT \
    -Dclassifier=original \
    -Dpackaging=jar \
    ${JDBC_PARAMS}; then
    log_success "Original JAR installed to local repository"
else
    log_error "Failed to install original JAR"
    exit 1
fi

# Step 4: Run MyBatis Generator
log_info "Step 4: Running MyBatis Generator..."
if mvn mybatis-generator:generate ${JDBC_PARAMS}; then
    log_success "MyBatis Generator completed successfully!"
    log_info "Generated files:"
    log_info "  - Model classes in: src/main/java/com/prolinkli/core/app/db/model/generated/"
    log_info "  - Mapper classes in: src/main/java/com/prolinkli/core/app/db/mapper/generated/"
    log_info "  - XML mappers in: src/main/resources/mapper/generated/"
else
    log_error "MyBatis Generator failed"
    exit 1
fi

log_success "MyBatis generation process completed successfully!"
log_info "You can now use the generated classes in your application."
