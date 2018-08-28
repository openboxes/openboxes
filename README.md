[![Stories in Ready](https://badge.waffle.io/openboxes/openboxes.png?label=ready&title=Ready)](https://waffle.io/openboxes/openboxes)
[![Build Status](https://travis-ci.org/openboxes/openboxes.svg?branch=master)](https://travis-ci.org/openboxes/openboxes)
[![Documentation Status](https://readthedocs.org/projects/openboxes/badge/?version=latest)](https://readthedocs.org/projects/openboxes/?badge=latest)
[![Slack Signup](http://slack-signup.openboxes.com/badge.svg)](http://slack-signup.openboxes.com)

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

### Setup development environment

#### Install Dependencies

Required
* Java 7
* MySQL 5.5+
* [SDK Man] (http://sdkman.io/install.html)
* [Grails 1.3.9](http://grails.org/download/archive/Grails)
* NPM

Optional
* [IntelliJ IDEA 14.1] (https://www.jetbrains.com/idea/download/)

#### Basic setup instructions for developers

These instructions are for developers only.  If you are a user/implementer, please check out our [Installation](http://docs.openboxes.com/en/latest/installation/) documentation.

##### 1. Install Dependencies
Install dependencies above

##### 2. Install Grails
Check that you have SDK Man installed properly (otherwise follow instructions on the skdman install page).
```
$ sdk version
SDKMAN 3.1.0
```

Install Grails 1.3.9
```
$ sdk install grails 1.3.9
```

##### 3. Clone repository 
* If you are a not core contributor, fork [openboxes git repository](https://github.com/openboxes/openboxes)
* If you are a core contributor:
```
git clone git@github.com:openboxes/openboxes.git      
```
Otherwise, replace git url with the one of your forked repository
```
git clone git@github.com:<gitusername>/openboxes.git      
```

##### 4. Create database 
Create openboxes database
```
mysql -u root -p -e 'create database openboxes default charset utf8;'
```

Create openboxes user 
```
mysql -u root -p -e 'grant all on openboxes.* to "openboxes"@"localhost" identified by "openboxes";'
```

##### 5. Create Openboxes configuration file 
Edit `$HOME/.grails/openboxes-config.properties`

```
# Database connection settings
# You can use dataSource.url when you are using a non-dev/non-test database (test-app may not run properly).
# If you want to run $ grails test-app you should comment out the dataSource.url below and create a new 
# openboxes_test database.  Eventually, we will move to an in-memory H2 database for testing, but we're 
# currently stuck with MySQL because I'm using some MySQL-specific stuff in the Liquibase changesets.  My bad.
# 
# MySQL 5.5 required a few extra parameters but these are no longer needed if using MySQL 5.7
# ?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB

dataSource.url=jdbc:mysql://localhost:3306/openboxes
dataSource.username=openboxes
dataSource.password=openboxes

# OpenBoxes mail settings (disabled by default)
grails.mail.enabled=false

# OpenBoxes > Inventory Browser > Quick categories
#openboxes.inventoryBrowser.quickCategories=ARVs,MEDICAL SUPPLIES,FOOD,EQUIPMENT,MEDICINE

# OpenBoxes > Choose Location > Required Activities
# The supported activities required in order for a location a location to show up on Choose Location page.
# Possible values: MANAGE_INVENTORY, PLACE_ORDER, PLACE_REQUEST, FULFILL_ORDER, FULFILL_REQUEST, SEND_STOCK, RECEIVE_STOCK, EXTERNAL
#
#openboxes.chooseLocation.requiredActivities = "MANAGE_INVENTORY"

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
```
openboxes.fixtures.enabled=false
```

##### 6. Install NPM dependencies
```    
npm install
```

##### 7. Build React frontend
You can build React frontend with this command, but it will be automatically build when starting the application.
```    
npm run bundle
```

##### 8. React frontend Hot-Reload
When using this command React fronted will be rebuild automatically after any change, you just need to refresh the browser to see the effect.
```    
npm run watch
```

##### 9. Upgrade the project to the currently installed grails version 
Either of the following actions (upgrade, compile, run-app) should generate the all important Spring configuration (`/WEB-INF/applicationContext.xml`) and start the dependency resolution process.  

```    
grails upgrade
```
OR

```    
grails compile
```

The `grails compile` step is not necessary since `grails run-app` will invoke the compilation step, but it doesn't hurt anything.

If you see any errors, run the command again.  

**IMPORTANT** That last line is important.  Because of some quirkiness with the way older versions of Grails resolve dependencies and generates config files, you may need to run either of these commands multiple times in order to resolve all dependencies and generate the config files.

Once the dependency resolution phase has completed, all dependencies will be stored in a local ivy cache (usually under `$USER_HOME/.grails/ivy-cache`).  You do not have to worry about this, just know that the dependencies are now on your machine and Grails will attempt to find them there before it tries to resolve them in a remote repository. 

##### 10. Start application in development mode
The application can be run in development mode.  This starts the application running in an instance of Tomcat within the Grails console.
You may need to run 'grails run-app' several times in order to download all dependencies.
```
grails run-app
```

##### 11. Open application in Google Chrome 
```
http://localhost:8080/openboxes
```

##### 12. Log into OpenBoxes 
You can use the default accounts (manager:password OR admin:password).  Once you are logged in as an admin, you can create own account.  Or you can use the signup form to create a new account.

##### 13. React tests
To run new frontend (React) tests type:
```
npm test
```
##### 13. React documentation
Start a style guide dev server:
```
npm run styleguide
```
View your style guide in the browser:
```
http://localhost:6060
```

### Troubleshooting

#### How to Debug 
* Run Grails in debug mode
    ```
    grails-debug run-app
    ```
* In Intellij navigate to Run > Edit Configurations
* Create a new Remote Debug Configuration
    * Name: openboxes-debug
    * Transport: Socket
    * Debugger mode: Attach
    * Host: localhost
    * Port: 5005
* Command line arguments should look something like this: 
    ```
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
    ```


#### Problem
```
Caused by: java.io.FileNotFoundException: Could not open ServletContext resource [/WEB-INF/applicationContext.xml]
```
#### Solution
Execute the grails upgrade command in order to generate the files nece
```
$ grails upgrade
```
See the following stackoverflow article:
http://stackoverflow.com/questions/24243027/grails-spring-security-sample-application-not-working
