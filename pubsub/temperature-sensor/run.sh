#!/bin/bash

java -jar target/temperature-sensor-1.0-SNAPSHOT.jar --topic dataflow --projectID $PROJECT --room C4F7 --period 1000
