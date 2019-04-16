#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Provide API key as first argument"
  else
    echo { \"requests\": [ { \"image\": { \"content\": \"`base64 -w 0 faces.png`\" }, \"features\": [ { \"type\": \"FACE_DETECTION\" }, { \"type\": \"LANDMARK_DETECTION\" } ] } ] } > request.json
    curl -s -X POST -H "Content-Type: application/json" --data-binary @request.json https://vision.googleapis.com/v1/images:annotate?key=${1}
fi
