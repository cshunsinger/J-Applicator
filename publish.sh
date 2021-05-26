#!/bin/bash

if [ "$1" = "" ]; then
  echo '**'
  echo '****'
  echo '******'
  echo '********'
  echo '**********'
  echo 'Usage: ./publish.sh <version>'
  echo '**********'
  echo '********'
  echo '******'
  echo '****'
  echo '**'
  exit 1
fi

#Import secret key into GPG
gpg --fast-import --no-tty --batch --yes secret-keys.gpg

#Publish and provide necessary details
gradle clean test publish \
-Pversion=$1 \
-PsonatypeUsername=${sonatype_username} \
-PsonatypePassword=${sonatype_password} \
-Psigning.gnupg.keyName=${sonatype_gpg_keyid} \
-Psigning.gnupg.passphrase=${sonatype_gpg_password}
