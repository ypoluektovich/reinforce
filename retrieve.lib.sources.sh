#!/bin/sh
#mkdir -p lib-src
#curl https://oss.sonatype.org/service/local/repositories/releases/content/org/yaml/snakeyaml/1.11/snakeyaml-1.11-sources.jar -o lib-src/snakeyaml-1.11-sources.jar
#curl https://oss.sonatype.org/service/local/repositories/central/content/org/apache/ivy/ivy/2.2.0/ivy-2.2.0-sources.jar -o lib-src/ivy-2.2.0-sources.jar
ivy -settings ivysettings.xml -confs sources -retrieve 'lib-src/[artifact]-[revision](-[classifier]).[ext]'
