#!/bin/sh

./retrieve.lib.sh

rm build -rf

mkdir -p build/compile-core
find src/core/java -name '*.java' >build/compile-core.files
javac -cp lib/snakeyaml-1.11.jar -d build/compile-core @build/compile-core.files

mkdir -p build/explode-libs
cd build/explode-libs
jar xf ../../lib/snakeyaml-1.11.jar
rm META-INF -rf
cd ../..

mkdir -p build/jar.tmp
cp -r build/compile-core/* build/jar.tmp
cp -r build/explode-libs/* build/jar.tmp
cp -r src/core/resources/* build/jar.tmp
cd build/jar.tmp
jar cf ../reinforce-core.jar *
cd ../..
