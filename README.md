# chailmis-android

Android Tablet Application for Logistics Management in the DHIS2 Platform. Target deployment is through CHAI Nigeria.

## Issue Tracking

[Waffle.io story wall](https://waffle.io/clintonhealthaccess/chailmis-android)

[![Stories in Dev](https://badge.waffle.io/clintonhealthaccess/chailmis-android.png?label=In%20Dev&title=In%20Dev)](http://waffle.io/clintonhealthaccess/chailmis-android)  
[![Stories in Ready for QA](https://badge.waffle.io/clintonhealthaccess/chailmis-android.png?label=Ready%20for%20QA&title=Ready%20for%20QA)](http://waffle.io/clintonhealthaccess/chailmis-android)  
[![Stories in QA Complete](https://badge.waffle.io/clintonhealthaccess/chailmis-android.png?label=QA%20Complete&title=QA%20Complete)](http://waffle.io/clintonhealthaccess/chailmis-android)  

## Issue metrics

[![Throughput Graph](http://graphs.waffle.io/clintonhealthaccess/chailmis-android/throughput.svg)](https://waffle.io/clintonhealthaccess/chailmis-android/metrics)

## DHIS2 Info

[This document](docs/DHIS2 Config.md) describes the metadata needed/used by the application in DHIS2.

## Developer info

### Build status, repositories

* [![Build Status](http://104.131.225.22:8080/job/android-unit-test/badge/icon)](http://104.131.225.22:8080/job/android-unit-test/)
* [Github Repository](https://github.com/clintonhealthaccess/chailmis-android)
* [Jenkins CI](http://104.131.225.22:8080/)
* [CrashLytics repository](https://crashlytics.com/twkla/android/apps/org.clintonhealthaccess.lmis.app/)
* [App store instructions](https://github.com/clintonhealthaccess/chailmis-android/blob/master/appstore/README.md)

### Jenkins Build Info
* Uses jenkins master slave to perform all test and build jobs
* Uses jenkins slave node(fdroid-slave) running under twer user to publish development builds to the fdroid repo
  The slave node is needed because the user which owns the fdroid repo on the server MUST be the user to publish

### Tech stack

* Target OS: Android 4.2
* [Build tool : gradle](https://gradle.org/)
* [IDE: Android Studio 0.8.1](http://tools.android.com/download/studio/canary/0-8-1)
* [Emulation: Genymotion](http://www.genymotion.com/)
* [Local DHIS2 install](https://github.com/clintonhealthaccess/dhis2-dev)

### Hardware specs

* RAM: 1GB DDR3
* Chipset: [MT8382](http://www.mediatek.com/en/products/mobile-communications/tablet/mt8382/) ARM Cortex-A7 quad-core processor
* Screen: 9" screen
* Resolution: 1024X600
* Android: 4.2.2

## ThoughtWorks internal links

* [Google drive](https://drive.google.com/a/thoughtworks.com/#folders/0Bx_qXlwQO9lRb3lnMjgwZllPcDA)
* [MyThoughtworks group](https://my.thoughtworks.com/groups/chailmis)
