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
* MySQL 5+
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
Add `$HOME/.grails/openboxes-config.properties` -- see http://pastebin.com/i4gDemnu for an example.

##### Upgrade grails version and plugins for grails -- does some basic cleanup / dependency resolution.
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
