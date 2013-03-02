#!/bin/sh

BASEDIR=`pwd`
LIB_DIR="`pwd`/lib"
rm -rf "$LIB_DIR"
./retrieve-libs.sh core

SANDBOX="`pwd`/build/core-bootstrap"
rm -rf "$SANDBOX"

mkdir -p "$SANDBOX/compile"
find src/core/java -name '*.java' >"$SANDBOX/compile.files"
CP="$( find lib -name '*.jar' -type f | awk '{ if (NR > 1) printf ":"; printf "%s", $0 }' )"
javac -cp "$CP" -d "$SANDBOX/compile" "@$SANDBOX/compile.files"

mkdir -p "$SANDBOX/explode-libs"
cd "$SANDBOX/explode-libs"
find "$LIB_DIR" -name '*.jar' -type f -exec jar xf "{}" \; -exec rm -rf META-INF \;
cd "$BASEDIR"

mkdir -p "$SANDBOX/jar.tmp"
cp -r "$SANDBOX"/compile/* "$SANDBOX/jar.tmp"
cp -r "$SANDBOX"/explode-libs/* "$SANDBOX/jar.tmp"
cp -r src/core/resources/* "$SANDBOX/jar.tmp"
cd "$SANDBOX/jar.tmp"
jar cf ../reinforce-bundle.jar *
cd "$BASEDIR"
