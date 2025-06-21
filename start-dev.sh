#!/bin/bash

source ./bash/log.sh

# This script is used to start the development server for the project.

log_info "Starting development server"


./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.jvmArguments="-Xmx1024m -Xms512m \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  -Dlogging.level.liquibase=DEBUG
