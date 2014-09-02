#!/bin/sh
adb shell pm uninstall org.clintonhealthaccess.lmis.app
./gradlew installDevelopmentDebug
adb shell am start -n org.clintonhealthaccess.lmis.app/org.clintonhealthaccess.lmis.app.activities.HomeActivity