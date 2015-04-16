Setup Your Own FDroid Repository
================================

- vagrant up
- vagrant ssh
- Now Nginx should be running. Check [http://localhost:8888](http://localhost:8888) to see Nginx welcome page.
- Install Android SDK as described in [this document](http://bernaerts.dyndns.org/linux/74-ubuntu/245-ubuntu-precise-install-android-sdk)
    - _(NOTE: Don't install Oracle JDK. OpenJDK works just fine.)_
    - sudo aptitude install ia32-libs libz1:i386
    - mkdir ~/downloads && cd ~/downloads
    - wget http://dl.google.com/android/android-sdk_r23.0.2-linux.tgz
    - tar zxvf android-sdk_r23.0.2-linux.tgz
    - sudo mv android-sdk-linux/ /var/lib/android-sdk
    - export ANDROID_HOME=/var/lib/android-sdk && export PATH=$ANDROID_HOME/tools:$PATH
    - android update sdk -u -a -t 1,2,3,20,107,108
    - sudo ln -sf /var/lib/android-sdk/build-tools/21.1.0/aapt /var/lib/android-sdk/platform-tools/aapt
- Build new apk and update it to FDroid repository following [this blog](https://guardianproject.info/2013/11/05/setting-up-your-own-app-store-with-f-droid/)
    - (in host machine) ./script/rebuild-android-app && ./script/sign-apk
        - (every TWer should know the password of the keystore...)
    - (in host machine) cp platforms/android/ant-build/BulamuApp.apk appstore/
    - (in Vagrant vm) cd /usr/share/nginx/www/fdroid && mv /vagrant/BulamuApp.apk repo/
    - (in Vagrant vm) fdroid update
- Install apk from Android phone
    - Install [F-Droid](https://f-droid.org/) first
    - Remove existing repository, and add your own repository URL (such as: http://10.111.125.58:8888/fdroid/repo)
    - Now refresh, you should be able to see Bulamu application.