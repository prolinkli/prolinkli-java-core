#!/bin/bash

source ./bash/log.sh

# This script is used to start the database server for the project.

docker compose -f docker-compose.yaml up -d
