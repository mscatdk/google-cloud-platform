#!/bin/bash

mvn compile -e exec:java -Dexec.mainClass=com.mscatdk.dataflow.StarterPipeline -Dexec.args="--pubSubTopicPath=$TOPIC --bigQueryTable=$TABLE --project=$PROJECT"
