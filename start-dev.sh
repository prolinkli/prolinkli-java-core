#!/bin/bash

source ./bash/log.sh

# This script is used to start the development server for the project.

log "Starting development server"

mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Xmx1024m -Xms512m" -Dspring-boot.run.profiles=dev -Dlogging.level.liquibase=DEBUG 
