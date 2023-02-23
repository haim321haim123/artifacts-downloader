#!/bin/bash

if [ -z $1 ]
then
	echo "usage: $0 artifact [artifacts]"
	exit 2
fi

set -x

maven_central=central::default::https://repo.maven.apache.org/maven2
maven_google=google::::https://dl.google.com/dl/android/maven2
REMOTE_REPOS=$maven_central,$maven_google

MAVEN_LOCAL=~/.m2/repository
ARTIFACTS_DIR=~/artifacts

rm -rf $ARTIFACTS_DIR
mkdir -p $ARTIFACTS_DIR

for artifact in $@
do
    mvn dependency:get -Dartifact="$artifact" -DremoteRepositories="$REMOTE_REPOS" -Dmaven.repo.local="$MAVEN_LOCAL"
    TAR_PKG_NAME=$artifact
done

TAR_PKG_NAME=${TAR_PKG_NAME//:/_}.tar.xz
BASE64_ARTIFACT="$TAR_PKG_NAME".txt

tar -C `dirname $MAVEN_LOCAL` -acf "$TAR_PKG_NAME" "`basename $MAVEN_LOCAL`" 
base64 "$TAR_PKG_NAME" > $BASE64_ARTIFACT

mv $TAR_PKG_NAME $BASE64_ARTIFACT ~/artifacts




