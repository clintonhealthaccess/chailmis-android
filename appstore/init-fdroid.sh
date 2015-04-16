#!/bin/sh

sudo rm -rf /usr/share/nginx/www/fdroid && \
sudo mkdir /usr/share/nginx/www/fdroid && \
sudo chown -R $USER /usr/share/nginx/www/fdroid && \
cd /usr/share/nginx/www/fdroid && \
fdroid init && \
fdroid update --create-metadata