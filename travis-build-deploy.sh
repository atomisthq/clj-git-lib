#!/bin/bash

die(){
  echo $1
  exit 1
}


export PV=$(head -n 1 project.clj | cut -d ' ' -f3 | cut -d '"' -f2 | cut -d '-' -f1)-$(date -u '+%Y%m%d%H%M%S')
echo "Version is: $PV"

lein set-version $PV :no-snapshot true || die "Error setting version"
lein do clean, midje, jar, deploy || die "Error building/deploying jar"
echo "Branch is ${TRAVIS_BRANCH}"

if [ "${TRAVIS_BRANCH}" == "master" ]; then
  git config --global user.email "travis-ci@atomist.com"
  git config --global user.name "Travis CI"
  git tag $PV -m "Generated tag from TravisCI build $TRAVIS_BUILD_NUMBER"
  git push origin $PV --follow-tags
fi