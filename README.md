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

* Install dependencies above
* Download/install Grails 1.3.7
* Add grails to your PATH variable
* If you are a not core contributor, fork [openboxes git repository](https://github.com/PIH/openboxes)
* If you are a core contributor:

        git clone git@github.com:PIH/openboxes.git      
  otherwise, replace git url with the one of your forked repository
* Create openboxes database 
```
mysql -u root -p -e 'create database openboxes default charset utf8;'
```
* Create openboxes user 
```
mysql -u root -p -e 'grant all on openboxes.* to "openboxes"@"localhost" identified by "openboxes";'
```
* Create new `$HOME/.grails/openboxes-config.properties` -- see http://pastebin.com/i4gDemnu for an example
* Upgrade grails version and plugins for grails
```    
grails upgrade
```
* Start application in development mode (you may need to run 'grails run-app' several times in order to download all dependencies).
```
grails run-app
```
* Open Chrome 
```
http://localhost:8080/openboxes
```
* Login with username/password (manager:password OR admin:password)
