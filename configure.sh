#!/bin/sh

if [ "$#" -lt 1 ]; then
    echo "Usage: ./configure [dev|staging|prod] task"
    exit -1
fi
./gradlew -q :dhis2-config-client:jar
java -jar dhis2-config-client/build/libs/dhis2-config-client.jar $1 $2
