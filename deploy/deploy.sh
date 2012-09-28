#!/bin/bash 
cd openboxes
svn update
grails clean
grails upgrade --non-interactive
grails prod war target/openboxes.war --non-interactive
line=$(jar -tvf target/openboxes.war | grep 'changelog.xml')
if [ $? -eq 1 ]
then
	echo "Cannot deploy openboxes.war because changelog.xml is missing"
else 
	#echo "Backup mysql database"
	mysql -u openboxes -p openboxes > openboxes.backup.sql

	#echo "Stop SymmetricDS"
	#sudo service sym_service stop
	
	echo "Stop Tomcat instance"
	sudo service tomcat6 stop

	echo "Undeploying existing application"
	sudo rm -rf /usr/local/tomcat6/webapps/openboxes*

	echo "Copy warehouse.war to Tomcat webapps directory"
	sudo cp target/openboxes.war /usr/local/tomcat6/webapps/openboxes.war

	echo "Start Tomcat instance"
	sudo service tomcat6 start

	#echo "Start SymmetricDS"
	#sudo service sym_service start
fi

