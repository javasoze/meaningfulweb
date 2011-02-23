#!/bin/bash
echo "installing htmlcleaner 2.2"
mvn install:install-file -Dfile=lib/htmlcleaner-2.2.jar -DgroupId=org.htmlcleaner -DartifactId=htmlcleaner -Dversion=2.2 -Dpackaging=jar
echo "installing nekohtml 1.9.13"
mvn install:install-file -Dfile=lib/nekohtml-1.9.13.jar -DgroupId=nekohtml -DartifactId=nekohtml -Dversion=1.9.13 -Dpackaging=jar
