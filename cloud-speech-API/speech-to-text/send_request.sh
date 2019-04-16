#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Provide API key as first argument"
  else
	# Convert file to flac
    ffmpeg -i $2 -f flac -ac 1 -ar 8000 fileout.flac
	echo { \"config\": { \"encoding\":\"FLAC\", \"sampleRateHertz\": 8000, \"languageCode\": \"en-US\", \"enableWordTimeOffsets\": false}, \"audio\": { \"content\":\"`base64 -w 0 fileout.flac`\" } } > request.json
    curl -s -H "Content-Type: application/json" -H "Authorization: Bearer "$(gcloud auth application-default print-access-token) https://speech.googleapis.com/v1/speech:recognize -d @request.json
	rm request.json
	rm fileout.flac
fi