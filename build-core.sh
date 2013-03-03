#!/bin/sh

BASEDIR=`pwd`
LIB_DIR="`pwd`/lib"
rm -rf "$LIB_DIR"
./retrieve-libs.sh core
(( $? == 0 )) || exit 1

SANDBOX="`pwd`/build/core-bootstrap"
rm -rf "$SANDBOX"

mkdir -p "$SANDBOX/compile"
find src/core/java -name '*.java' >"$SANDBOX/compile.files"
CP="$( find lib -name '*.jar' -type f | awk '{ if (NR > 1) printf ":"; printf "%s", $0 }' )"
javac -cp "$CP" -d "$SANDBOX/compile" "@$SANDBOX/compile.files"
(( $? == 0 )) || exit 1

mkdir -p "$SANDBOX/jar.tmp"
cp -r "$SANDBOX"/compile/* "$SANDBOX/jar.tmp" || exit 1
cp -r src/core/resources/* "$SANDBOX/jar.tmp" || exit 1
cd "$SANDBOX/jar.tmp"
jar cf ../reinforce-core.jar * || exit 1
cd "$BASEDIR"
