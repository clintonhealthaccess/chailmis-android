#!/bin/sh
adb shell pm uninstall org.clintonhealthaccess.lmis.app
./gradlew installDevelopmentDebug