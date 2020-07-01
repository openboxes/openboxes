[![Build Status](https://travis-ci.org/openboxes/openboxes.svg?branch=develop)](https://travis-ci.org/openboxes/openboxes)
[![Documentation Status](https://readthedocs.org/projects/openboxes/badge/?version=develop)](https://readthedocs.org/projects/openboxes/?badge=develop)
[![Financial Contributors on Open Collective](https://opencollective.com/openboxes/all/badge.svg?label=financial+contributors)](https://opencollective.com/openboxes) 
[![Slack Signup](http://slack-signup.openboxes.com/badge.svg)](http://slack-signup.openboxes.com)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com) [![Join the chat at https://gitter.im/openboxes/openboxes](https://badges.gitter.im/openboxes/openboxes.svg)](https://gitter.im/openboxes/openboxes?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

OpenBoxes
=========

## About

OpenBoxes is an Open Source Inventory and Supply Chain Management System. The initial implementation of OpenBoxes will occur at Partners In Health-supported facilities in Haiti.

## Contributors

### Code Contributors

This project exists thanks to all the people who contribute. [[Contribute](CONTRIBUTING.md)].
<a href="https://github.com/openboxes/openboxes/graphs/contributors"><img src="https://opencollective.com/openboxes/contributors.svg?width=890&button=false" /></a>

### Financial Contributors

Become a financial contributor and help us sustain our community. [[Contribute](https://opencollective.com/openboxes/contribute)]

#### Individuals

<a href="https://opencollective.com/openboxes"><img src="https://opencollective.com/openboxes/individuals.svg?width=890"></a>

#### Organizations

Support this project with your organization. Your logo will show up here with a link to your website. [[Contribute](https://opencollective.com/openboxes/contribute)]

<a href="https://opencollective.com/openboxes/organization/0/website"><img src="https://opencollective.com/openboxes/organization/0/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/1/website"><img src="https://opencollective.com/openboxes/organization/1/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/2/website"><img src="https://opencollective.com/openboxes/organization/2/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/3/website"><img src="https://opencollective.com/openboxes/organization/3/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/4/website"><img src="https://opencollective.com/openboxes/organization/4/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/5/website"><img src="https://opencollective.com/openboxes/organization/5/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/6/website"><img src="https://opencollective.com/openboxes/organization/6/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/7/website"><img src="https://opencollective.com/openboxes/organization/7/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/8/website"><img src="https://opencollective.com/openboxes/organization/8/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/9/website"><img src="https://opencollective.com/openboxes/organization/9/avatar.svg"></a>

## License

Copyright (c) 2012 Partners In Health.  All rights reserved.
The use and distribution terms for this software are covered by the
Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.
You must not remove this notice, or any other, from this software.

## Setup development environment

### Install Dependencies

#### Required
* Java 7 (must install Java 7)
* MySQL 5.7
* [SDK Man](http://sdkman.io/install.html)
* [Grails 1.3.9](http://grails.org/download/archive/Grails)
* NPM 6.13.7
* Node 13.11.0

#### Optional
* [IntelliJ IDEA 14.1](https://www.jetbrains.com/idea/download/)
* Chrome

### Basic setup instructions for developers

These instructions are for developers only.  If you are a user/implementer, please check out our 
[Installation](http://docs.openboxes.com/en/latest/installation/) documentation.

#### 1. Install Dependencies
Install required dependencies above

#### 2. Install Grails
Check that you have SDK Man installed properly (otherwise follow instructions on the skdman install page).
```
$ sdk version
SDKMAN 3.1.0
```

Install Grails 1.3.9
```
$ sdk install grails 1.3.9
```

#### 3. Clone repository 
If you are a core contributor:
```
git clone git@github.com:openboxes/openboxes.git      
```
If you are a not core contributor, fork [openboxes git repository](https://github.com/openboxes/openboxes)
and replace git url with the one of your forked repository
```
git clone git@github.com:<gitusername>/openboxes.git      
```

#### 4. Create database 
Create openboxes database
```
mysql -u root -p -e 'create database openboxes default charset utf8;'
```

Create openboxes user 
```
mysql -u root -p -e 'grant all on openboxes.* to "openboxes"@"localhost" identified by "openboxes";'
```

#### 5. Create Openboxes configuration file 
Edit `$HOME/.grails/openboxes-config.properties`

```
# Database connection settings
# You can use dataSource.url when you are using a non-dev/non-test database (test-app may not run properly).
# If you want to run $ grails test-app you should comment out the dataSource.url below and create a new 
# openboxes_test database.  Eventually, we will move to an in-memory H2 database for testing, but we're 
# currently stuck with MySQL because I'm using some MySQL-specific stuff in the Liquibase changesets.  My bad.

dataSource.url=jdbc:mysql://localhost:3306/openboxes
dataSource.username=openboxes
dataSource.password=openboxes

# OpenBoxes mail settings (disabled by default)
grails.mail.enabled=false
```
NOTE: If you are running in development mode with a copy of an existing production database, you will need to
instruct the application to not setup test fixtures automatically by uncommenting the above property:
```
openboxes.fixtures.enabled=false
```

#### 6. Install NPM dependencies
```    
npm config set engine-strict true
npm install
```

#### 7. Build React frontend
You can build React frontend with this command, but it will be automatically build when starting the application.
```    
npm run bundle
```

#### 8. React frontend Hot-Reload
When using this command React fronted will be rebuild automatically after any change, you just need to refresh the 
browser to see the effect.
```    
npm run watch
```

#### 9. Upgrade the project to the currently installed grails version 
Either of the following actions (upgrade, compile, run-app) should generate the all important Spring configuration 
(`/WEB-INF/applicationContext.xml`) and start the dependency resolution process.  

```    
grails upgrade
```
OR

```    
grails compile
```

The `grails compile` step is not necessary since `grails run-app` will invoke the compilation step, but it doesn't 
hurt anything.

If you see any errors, run the command again.  

**IMPORTANT** That last line is important.  Because of some quirkiness with the way older versions of Grails resolve 
dependencies and generates config files, you may need to run either of these commands multiple times in order to 
resolve all dependencies and generate the config files.

Once the dependency resolution phase has completed, all dependencies will be stored in a local ivy cache (usually 
under `$USER_HOME/.grails/ivy-cache`).  You do not have to worry about this, just know that the dependencies are now 
on your machine and Grails will attempt to find them there before it tries to resolve them in a remote repository. 

#### 10. Start application in development mode
The application can be run in development mode.  This starts the application running in an instance of Tomcat within 
the Grails console.
You may need to run 'grails run-app' several times in order to download all dependencies.
```
grails run-app
```

#### 11. Open application in Google Chrome 
```
http://localhost:8080/openboxes
```

#### 12. Log into OpenBoxes 
You can use the default accounts (manager:password OR admin:password). Once you are logged in as an admin, you can 
create own account. Or you can use the signup form to create a new account.

#### 13. React tests
To run new frontend (React) tests type:
```
npm test
```
#### 14. React documentation
Start a style guide dev server:
```
npm run styleguide
```
View your style guide in the browser:
```
http://localhost:6060
```

## Troubleshooting

### How to Debug 
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


### Problem
```
Caused by: java.io.FileNotFoundException: Could not open ServletContext resource [/WEB-INF/applicationContext.xml]
```
### Solution
Execute the grails upgrade command in order to generate the files nece
```
$ grails upgrade
```
See the following stackoverflow article:
http://stackoverflow.com/questions/24243027/grails-spring-security-sample-application-not-working
