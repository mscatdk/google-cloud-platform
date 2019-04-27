#!/bin/bash


mvn compile -e exec:java \
 -Dexec.mainClass=$MAIN \
      -Dexec.args="--project=$PROJECT \
      --pubSubTopicPath=$TOPIC \
      --bigQueryTable=$TABLE \
      --stagingLocation=gs://$BUCKET/staging/ \
      --tempLocation=gs://$BUCKET/staging/ \
      --defaultWorkerLogLevel=DEBUG \
      --runner=DataflowRunner"
