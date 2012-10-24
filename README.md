OpenBoxes
=========

### About

OpenBoxes is an Open Source Inventory and Supply Chain Management System. The initial implementation of OpenBoxes will occur at Partners In Health-supported facilities in Haiti.

### License

Copyright (c) 2012 Partners In Health.  All rights reserved.
The use and distribution terms for this software are covered by the
Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.
You must not remove this notice, or any other, from this software.

### Setup dev environment

####Dependencies

* [Eclipse IDE for Java EE Developers](http://www.eclipse.org/downloads)
* Java 1.6
  download from http://www.oracle.com/technetwork/java/javase/downloads/jdk6u37-downloads-1859587.html
* [Grails 1.3.7]
  download from (http://grails.org/download/archive/Grails)
* MySQL 5+ (https://help.ubuntu.com/8.04/serverguide/mysql.html)
  * sudo apt-get update
  * sudo apt-get install mysql-server
  * sudo netstat -tap | grep mysql
* Tomcat 6 or 7 (optional for dev envrionment)
 

####Setup Instructions

* Install dependencies above
* Add java and grails to your PATH variable
  * vim ~/.bashrc
  * Add the followings 
     export JAVA_HOME=/home/adminuser/Downloads/jdk1.6.0_37
     export PATH=$PATH:$JAVA_HOME/bin

     export GRAILS_HOME=/home/adminuser/Downloads/grails-1.3.7
     export PATH=$PATH:$GRAILS_HOME/bin 
  * Save
  * cat ~/.bashrc
* If you are a not core contributor, fork [openboxes git repository](https://github.com/PIH/openboxes)
* If you are a core contributor:

        git clone git@github.com:PIH/openboxes.git      
  otherwise, replace git url with the one of your forked reposiotry.    
* cd openboxes
* create datbabase for dev and test environments

        mysql -u root -p -e 'create database openboxes_dev default charset utf8;'

        mysql -u root -p -e 'create database openboxes_test default charset utf8;'  
        password: openboxes     
* create user for databases          
     
        mysql -u root -p -e 'grant all on openboxes_dev.* to openboxes@localhost identified by "openboxes";'
      
        mysql -u root -p -e 'grant all on openboxes_test.* to openboxes@localhost identified by "openboxes 
        password: openboxes
* create new openboxes-config.properties file under $HOME/.grails (not sure if the quickCategories will work as the default system categories might be different from ours)
  * mkdir ~/.grails
  * vim ~/.grails/openboxes-config.properties
  * Add the followings
        dataSource.username=openboxes
        dataSource.password=openboxes 
        inventoryBrowser.quickCategories=ARVs,MEDICAL SUPPLIES,FOOD,EQUIPMENT,MEDICINE

        #app.loginLocation.requiredActivities = ["MANAGE_INVENTORY"]      
* upgrade plugins for grails 
    
         grails upgrade
* start application

         grails run-app
*  open firefox/chrome http://localhost:8080/warehouse
login with username/password (manager : password)
