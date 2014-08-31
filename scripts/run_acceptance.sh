#! /bin/bash
./gradlew assembleDevelopmentDebug
export RESET_BETWEEN_SCENARIOS=1
calabash-android run ./app/build/outputs/apk/app-development-debug.apk $1
