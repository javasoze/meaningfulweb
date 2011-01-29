#!/bin/bash
echo "installing tika core snapshot..."
mvn install:install-file -Dfile=lib/tika-core-0.9-SNAPSHOT.jar -DgroupId=org.apache.tika -DartifactId=tika-core -Dversion=0.9-SNAPSHOT -Dpackaging=jar
echo "installing tika parser snapshot..."
mvn install:install-file -Dfile=lib/tika-parsers-0.9-SNAPSHOT.jar -DgroupId=org.apache.tika -DartifactId=tika-parsers -Dversion=0.9-SNAPSHOT -Dpackaging=jar
echo "installing htmlcleaner 2.2"
mvn install:install-file -Dfile=lib/htmlcleaner-2.2.jar -DgroupId=org.htmlcleaner -DartifactId=htmlcleaner -Dversion=2.2 -Dpackaging=jar
