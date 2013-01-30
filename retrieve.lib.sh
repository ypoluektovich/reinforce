#!/bin/sh
mkdir -p lib
curl https://oss.sonatype.org/service/local/repositories/releases/content/org/yaml/snakeyaml/1.11/snakeyaml-1.11.jar -o lib/snakeyaml-1.11.jar
curl https://oss.sonatype.org/service/local/repositories/central/content/org/apache/ivy/ivy/2.2.0/ivy-2.2.0.jar -o lib/ivy-2.2.0.jar
