# Getting Started

## Dependencies

### Required
* Java 7
* MySQL 5.5+
* [SDK Man] (http://sdkman.io/install.html)
* [Grails 1.3.9](http://grails.org/download/archive/Grails)
* NPM

NOTE: We are in the process of upgrading to Grails 2.5.5 [(see this feature branch).](https://github.com/openboxes/openboxes/tree/feature/94-upgrade-to-grails-2.5.x) Once that is complete you will be able to use Java 8. 

### Optional, but recommended
* [IntelliJ IDEA] (https://www.jetbrains.com/idea/download/)

## Instructions
These instructions are for developers only.  If you are a system administrator looking to install OpenBoxes on your
own server, please check out our [Installation](installation/index) page.

### 1. Install Dependencies
Install dependencies above
```
sudo apt-get install openjdk7
sudo apt-get install mysql-server
```

### 2. Install Grails
Check that you have SDK Man installed properly (otherwise follow instructions on the skdman install page).
```
$ sdk version
SDKMAN 3.1.0
```

To install Grails 1.3.9
```
$ sdk install grails 1.3.9
```

### 3. Clone repository 
If you are a core contributor:
```
git clone git@github.com:openboxes/openboxes.git      
```

If you are a not core contributor, fork the [openboxes GitHub repository](https://github.com/openboxes/openboxes), then replace git url with the one of your forked repository
```
git clone git@github.com:<username>/openboxes.git      
```

### 4. Create database 
Create openboxes database
```
mysql -u root -p -e 'create database openboxes default charset utf8;'
```

Create openboxes user 
```
mysql -u root -p -e 'grant all on openboxes.* to "openboxes"@"localhost" identified by "openboxes";'
```

### 5. Create configuration file(s)

Create or edit a file called `$HOME/.grails/openboxes-config.properties`.

```
# Database connection settings
# You can use dataSource.url when you are using a non-dev/non-test database (test-app may not run properly).
# If you want to run $ grails test-app you should comment out the dataSource.url below and create a new 
# openboxes_test database.  Eventually, we will move to an in-memory H2 database for testing, but we're 
# currently stuck with MySQL because I'm using some MySQL-specific statements in the Liquibase changesets.  My bad.
dataSource.url=jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB
dataSource.username=openboxes
dataSource.password=openboxes
 
# OpenBoxes mail settings - disabled by default
grails.mail.enabled=false
 
# OpenBoxes > Inventory Browser > Quick categories
#openboxes.inventoryBrowser.quickCategories=ARVs,MEDICAL SUPPLIES,FOOD,EQUIPMENT,MEDICINE
 
# OpenBoxes > Choose Location > Required Activities
# The supported activities required in order for a location a location to show up on Choose Location page.
# Possible values: MANAGE_INVENTORY, PLACE_ORDER, PLACE_REQUEST, FULFILL_ORDER, FULFILL_REQUEST, SEND_STOCK, RECEIVE_STOCK, EXTERNAL
#openboxes.chooseLocation.requiredActivities = MANAGE_INVENTORY
 
# If you wish to not set up any test data, you can indicate this per the below 
# (eg. if you are running from a copy of a production db)
# openboxes.fixtures.enabled=false
 
# If you want to track users via Google analytics
#google.analytics.enabled = false
#google.analytics.webPropertyID = <Google Analytics Key>
```

**NOTE:** If you are running in development mode with a copy of an existing database, you may want to
instruct the application to bypass the test data fixtures automatically. You can achieve this by commenting 
out the `openboxes.fixtures.enabled` property. Unfortunately, 
the .properties files DO NOT deal well with boolean values so commenting out is the only way to set this property. 
If you want a more elegant approach, you can add all boolean properties to `openboxes-config.groovy`.
```
#openboxes.fixtures.enabled=true
```

### 6. Install NPM dependencies
```    
npm install
```

### 7. Build React frontend
You can build React frontend with this command, but it will be automatically build when starting the application.
```    
npm run bundle
```

### 8. React frontend Hot-Reload
When using this command React fronted will be rebuild automatically after any change, you just need to refresh the browser to see the effect.
```    
npm run watch
```

### 9. Grails Upgrade or Grails Compile 
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

### 10. Start application in development mode
The application can be run in development mode.  This starts the application running in an instance of Tomcat within the Grails console.
You may need to run 'grails run-app' several times in order to download all dependencies.
```
grails run-app
```

### 11. Open application in Google Chrome 
```
http://localhost:8080/openboxes
```

### 12. Log into OpenBoxes 
You can use the default accounts (manager:password OR admin:password).  Once you are logged in as an admin, you can create own account.  Or you can use the signup form to create a new account.

##### 13. React tests
To run new frontend (React) tests type:
```
npm test
```
