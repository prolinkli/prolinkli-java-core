#!/bin/bash

VOLUME_NAME="prolinkli-java-core_prolinkli_pgdata"

if [ -z "$VOLUME_NAME" ]; then
	echo "Error: VOLUME_NAME is not set."
	exit 1
fi

if docker volume inspect "$VOLUME_NAME" &> /dev/null; then
	echo "Removing volume: $VOLUME_NAME"
	docker compose down
	docker volume rm "$VOLUME_NAME"
else
	echo "Volume $VOLUME_NAME does not exist."
fi

