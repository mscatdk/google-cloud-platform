#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Provide API key as first argument"
  else
    curl -s -H 'Content-Type: application/json' -H "Authorization: Bearer $1" 'https://videointelligence.googleapis.com/v1/videos:annotate' -d @request.json | grep name | awk -F'"' '{print $4}' > id.txt
fi
