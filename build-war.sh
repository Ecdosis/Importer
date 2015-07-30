#!/bin/bash
if [ ! -d importer ]; then
  mkdir importer
  if [ $? -ne 0 ] ; then
    echo "couldn't create importer directory"
    exit
  fi
fi
if [ ! -d importer/WEB-INF ]; then
  mkdir importer/WEB-INF
  if [ $? -ne 0 ] ; then
    echo "couldn't create importer/WEB-INF directory"
    exit
  fi
fi
if [ ! -d importer/WEB-INF/lib ]; then
  mkdir importer/WEB-INF/lib
  if [ $? -ne 0 ] ; then
    echo "couldn't create importer/WEB-INF/lib directory"
    exit
  fi
fi
rm -f importer/WEB-INF/lib/*.jar
cp dist/Importer.jar importer/WEB-INF/lib/
cp lib/*.jar importer/WEB-INF/lib/
rm importer/WEB-INF/lib/servlet*.jar
cp web.xml importer/WEB-INF/
jar cf importer.war -C importer WEB-INF
echo "NB: you MUST copy the contents of tomcat-bin to \$tomcat_home/bin"
