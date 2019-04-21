# Data Flow

## Generate Maven Data Flow project

````bash
mvn archetype:generate \
  -DarchetypeArtifactId=google-cloud-dataflow-java-archetypes-starter \
  -DarchetypeGroupId=com.google.cloud.dataflow \
  -DgroupId=com.mscatdk.dataflow \
  -DartifactId=starter \
  -Dversion="[1.0.0,2.0.0]" \
  -DinteractiveMode=false
  ````

## Run locally

````bash
#!/bin/bash

MAIN=com.mscatdk.dataflow.starter

mvn compile -e exec:java -Dexec.mainClass=$MAIN
````

## Run Remotely

````bash
#!/bin/bash
PROJECT=$1
BUCKET=$2
MAIN=MAIN=com.mscatdk.dataflow.starter

echo "project=$PROJECT  bucket=$BUCKET  main=$MAIN"

mvn compile -e exec:java \
 -Dexec.mainClass=$MAIN \
      -Dexec.args="--project=$PROJECT \
      --stagingLocation=gs://$BUCKET/staging/ \
      --tempLocation=gs://$BUCKET/staging/ \
      --runner=DataflowRunner"
````