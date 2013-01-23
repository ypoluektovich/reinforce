#!/bin/sh

rm build -rf

mkdir -p build/compile
find src/core/java -name '*.java' >build/compile.files
javac -cp lib/snakeyaml-1.11.jar -d build/compile @build/compile.files

mkdir -p build/explode-libs
cd build/explode-libs
jar xf ../../lib/snakeyaml-1.11.jar
rm META-INF -rf
cd ../..

mkdir -p build/jar
cp -r build/compile/* build/jar
cp -r build/explode-libs/* build/jar
cp -r src/core/resources/* build/jar
cd build/jar
jar cf ../reinforce.jar *
cd ../..
