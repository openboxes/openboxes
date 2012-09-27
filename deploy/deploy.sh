#!/bin/bash 
cd warehouse
svn update
grails clean
grails upgrade --non-interactive
grails prod war target/warehouse.war --non-interactive
line=$(jar -tvf target/warehouse.war | grep 'changelog.xml')
if [ $? -eq 1 ]
then
	echo "Cannot deploy warehouse.war because changelog.xml is missing"
else 
	#echo "Backup mysql database"
	mysql -u warehouse -p warehouse_prod > warehouse_prod.backup.sql

	#echo "Stop SymmetricDS"
	#sudo service sym_service stop
	
	echo "Stop Tomcat instance"
	sudo service tomcat6 stop

	echo "Undeploying existing application"
	sudo rm -rf /usr/local/tomcat6/webapps/warehouse*

	echo "Copy warehouse.war to Tomcat webapps directory"
	sudo cp target/warehouse.war /usr/local/tomcat6/webapps/warehouse.war

	echo "Start Tomcat instance"
	sudo service tomcat6 start

	#echo "Start SymmetricDS"
	#sudo service sym_service start
fi

