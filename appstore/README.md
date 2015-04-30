Setup Your Own FDroid Repository
================================

To setup a device
-------
* First install F-Droid from https://f-droid.org/
* Once in the F-Droid app, click on the menu to navigate to the "Repositories" section
* In the "Repositories" section, click the + sign to add a new repository
* There are two repositories that can be added. NOTE: Both are http, not https. Therefore, you may have to delete the "s" in the default field on F-Droid
1) For development use: http://lmisapp.dhis2nigeria.org.ng/fdroid/development/repo. 
2) For production use: http://lmisapp.dhis2nigeria.org.ng/fdroid/production/repo
* Once the repository is added, navigate back to your available apps to install DHIS2 LMIS.
* Install and Update LMIS app



---------
Repositories are curently stored on the CI server, at the following URL: [http://lmisapp.dhis2nigeria.org.ng](http://lmisapp.dhis2nigeria.org.ng).

---------

- `vagrant up`
- `vagrant ssh`
- Now Nginx should be running. Check [http://localhost:8888](http://localhost:8888) to see Nginx welcome page.
- Install Android SDK as described in [this document](http://bernaerts.dyndns.org/linux/74-ubuntu/245-ubuntu-precise-install-android-sdk)
    - _(NOTE: Don't install Oracle JDK. OpenJDK works just fine.)_
    - `sudo aptitude install ia32-libs libz1:i386`
    - `sudo apt-get install lib32stdc++6 lib32z1`
    - `mkdir ~/downloads && cd ~/downloads`
    - `wget http://dl.google.com/android/android-sdk_r23.0.2-linux.tgz`
    - `tar zxvf android-sdk_r23.0.2-linux.tgz`
    - `sudo mv android-sdk-linux/ /var/lib/android-sdk`
    - `export ANDROID_HOME=/var/lib/android-sdk && export PATH=$ANDROID_HOME/tools:$PATH`
    - (List all sdk packages) 
    - `android list sdk --all`
    - (We need Android API 19)
    - `android update sdk -u -a -t 1,2,12,90`
    - `sudo ln -sf /var/lib/android-sdk/build-tools/21.1.0/aapt /var/lib/android-sdk/platform-tools/aapt`
- Build new apk and update it to FDroid repository following [this blog](https://guardianproject.info/2013/11/05/setting-up-your-own-app-store-with-f-droid/)
    - (in host machine) `./gradlew clean assembleDebug crashlyticsUploadDistributionDevelopmentDebug`
    - (in host machine) `cp app/build/outputs/apk/app-development-debug.apk appstore/LMIS.apk`
    - (in Vagrant vm) `cd /usr/share/nginx/www/fdroid && mv /vagrant/LMIS.apk repo/`
    - (in Vagrant vm) `fdroid update --create-metadata --clean --verbose`
- Install apk from Android phone
    - Install [F-Droid](https://f-droid.org/) first
    - Remove existing repository, and add your own repository URL (such as: http://10.111.125.58:8888/fdroid/repo)
    - Now refresh, you should be able to see CHAI LMIS application.



To publish a development package
------

- `cd /var/www/html/fdroid/development/`
- `wget http://lmisapp.dhis2nigeria.org.ng:8080/job/generate-apk/lastSuccessfulBuild/artifact/app/build/outputs/apk/app-development-debug.apk -O repo/LMIS.apk`
- `fdroid update --create-metadata --clean --verbose`

To publish a production package
-------

- `cd /var/www/html/fdroid/production/`
- `wget http://lmisapp.dhis2nigeria.org.ng:8080/job/generate-apk/lastSuccessfulBuild/artifact/app/build/outputs/apk/app-production-debug.apk -O repo/LMIS.apk`
- `fdroid update --create-metadata --clean --verbose`
