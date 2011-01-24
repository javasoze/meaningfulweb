#!/bin/bash
echo "installing og4j.jar..."
mvn install:install-file -Dfile=lib/og4j-0.0.1-SNAPSHOT.jar -DgroupId=proj.og4j -DartifactId=og4j -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
echo "installing tika core snapshot..."
mvn install:install-file -Dfile=lib/tika-core-0.9-SNAPSHOT.jar -DgroupId=org.apache.tika -DartifactId=tika-core -Dversion=0.9-SNAPSHOT -Dpackaging=jar
echo "installing tika parser snapshot..."
mvn install:install-file -Dfile=lib/tika-parsers-0.9-SNAPSHOT.jar -DgroupId=org.apache.tika -DartifactId=tika-parsers -Dversion=0.9-SNAPSHOT -Dpackaging=jar
