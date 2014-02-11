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
* [Grails 1.3.7](http://grails.org/download/archive/Grails)
* MySQL 5.5+
* Tomcat 6 or 7 (optional for dev envrionment)
 

#### Basic setup instructions for developers

If you are a user/implementer, please check out our [Installation](wiki/Installation) page.

##### Install dependencies

* Install dependencies above
* Install latest version of GVM (http://gvmtool.net/)
* Install Grails 1.3.7
```
gvm install grails 1.3.7
```

##### Clone repository 
* If you are a not core contributor, fork [openboxes git repository](https://github.com/PIH/openboxes)
* If you are a core contributor:
```
        git clone git@github.com:PIH/openboxes.git      
```
Otherwise, replace git url with the one of your forked repository

##### Create openboxes database 
```
mysql -u root -p -e 'create database openboxes default charset utf8;'
```
##### Create openboxes user 
```
mysql -u root -p -e 'grant all on openboxes.* to "openboxes"@"localhost" identified by "openboxes";'
```

##### Create Openboxes configuration file 
Add `$HOME/.grails/openboxes-config.properties`

```
# Database connection settings
# You can use dataSource.url when you are using a non-dev/non-test database (test-app may not run properly).
# If you want to run $ grails test-app you should comment out the dataSource.url below and create a new 
# openboxes_test database.  Eventually, we will move to an in-memory H2 database for testing, but we're 
# currently stuck with MySQL because I'm using some MySQL-specific stuff in the Liquibase changesets.  My bad.

dataSource.url=jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB
dataSource.username=openboxes
dataSource.password=openboxes

# OpenBoxes mail settings - disabled by default
grails.mail.enabled=false

# Application settings
#inventoryBrowser.quickCategories=ARVs,MEDICAL SUPPLIES,FOOD,EQUIPMENT,MEDICINE
#openboxes.loginLocation.requiredActivities = ["MANAGE_INVENTORY"]

# If you wish to not set up any test data, you can indicate this per the below 
# (eg. if you are running from a copy of a production db)
# openboxes.fixtures.enabled=false

# Google Product Search
#google.api.key=<Google API key>

# Hipaaspace.com API (NDC Lookup)
#hipaaspace.api.key=<hipaaspace API key>

# RXNorm API
#rxnorm.api.key=<RxNorm API key>

# Google analytics
#google.analytics.enabled = false
#google.analytics.webPropertyID = <Google Analytics Key>
```

NOTE: If you are running in development mode with a copy of an existing production database, you will need to
instruct the application to not setup test fixtures automatically by uncommenting the above property:
openboxes.fixtures.enabled=false

##### Compile or "upgrade" grails version and plugins for grails
Either of these actions should start the dependency resolution process.  

**IMPORTANT** You may need to run either of these commands multiple times in order to resolve all dependencies.

```    
grails compile
```

OR

```    
grails upgrade
```
##### Start application 
The application can be run in development mode.  This starts the application running in an instance of Tomcat within the Grails console.
You may need to run 'grails run-app' several times in order to download all dependencies.
```
grails run-app
```

##### Open application in Google Chrome 
```
http://localhost:8080/openboxes
```

##### Log into OpenBoxes 
You can use the default accounts (manager:password OR admin:password) and create your own accounts.
