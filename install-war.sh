#!/bin/bash
service tomcat6 stop
cp importer.war /var/lib/tomcat6/webapps/
rm -rf /var/lib/tomcat6/webapps/importer
rm -rf /var/lib/tomcat6/work/Catalina/localhost/
service tomcat6 start
