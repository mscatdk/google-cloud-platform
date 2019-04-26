#!/bin/bash

if [ $# -eq 0 ] || [ ! -f ./id.txt ]
  then
    echo "Provide API key as first argument and the file id.txt must exist"
  else
	curl -s -H 'Content-Type: application/json' -H "Authorization: Bearer $1" "https://videointelligence.googleapis.com/v1/operations/`cat id.txt`"
fi
