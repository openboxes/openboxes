
# Quickstart on Ubuntu 22.04

This guide provides a step-by-step introduction to get you started quickly.

## Requirements
* Operating System: You are running Ubuntu 22.04 or higher
* Memory: You have at least 4 GB of RAM, preferably 8 GB
* Disk Space: You have at least 25 GB of disk available

!!! warning 

    These quickstart instructions are not recommended for production 
    environments. Please use the installation guide. 

## Instructions

### Step 1: Install dependencies

#### Update System Packages
First, ensure that your system is up to date by running:
```shell
sudo apt update
sudo apt upgrade -y
```

#### Install MySQL 8
```shell
sudo apt install mysql-server-8
```

#### Install Java 8
```shell
sudo apt-get install openjdk-8-jdk
```

### Step 2: Create Database


#### Create Database

```shell
mysql -e 'create database openboxes default charset utf8'
```

#### Create Database User
```shell
mysql -e 'grant all on openboxes.* to openboxes@localhost identified by "openboxes"'
```

### Step 3: Setup Environment 

=== "Use Defaults"
    If you don't touch anything, OpenBoxes will use the defaults provided within 
    application.yml. So if you follow the instructions above while creating
    the database you shouldn't need to touch anything. 
    ```yaml
    dataSource:
      username: "openboxes"
      password: "openboxes"
      url: jdbc:mysql://localhost:3306/openboxes?serverTimezone=UTC&useSSL=false
    ```
=== "Use External Config File"
    If you need to override the default values (e.g. you want to use a more secure
    password), you can set your own config property values in an external config file.
    See our Configuration Guide for more details. 

    ```
    mkdir ~/.grails
    vi ~/.grails/openboxes.yml
    ```
    ... and then copy the default properties into `~/.grails/openboxes.yml` and
    edit the properties to meet your needs.

    ```
    dataSource:
      username: openboxes
      password: openboxes
      url: jdbc:mysql://localhost:3306/openboxes?serverTimezone=UTC&useSSL=false
    ```
=== "Use Environment Variables"
    We haven't confirmed that the environment variables work for all 
    ```shell
        export DATASOURCE_URL=jdbc:mysql://localhost:3306/openboxes?serverTimezone=UTC&useSSL=false
        export DATASOURCE_USERNAME=openboxes
        export DATASOURCE_PASSWORD=openboxes
    ```



### Step 4: Start Application 

#### Download WAR file

Download the stable release (production) or the daily snapshot (development).

=== "Stable" 
    ```
    wget https://bamboo-ci.pih-emr.org/browse/OPENBOXES-OBNR/latestSuccessful/artifact/G3JOB/Latest-WAR/openboxes.war
    ```
    [Download](https://bamboo-ci.pih-emr.org/browse/OPENBOXES-OBNR/latestSuccessful/artifact/G3JOB/Latest-WAR/openboxes.war)


=== "Snapshot"
    ```
    wget https://bamboo-ci.pih-emr.org/browse/OPENBOXES-DS/latestSuccessful/artifact/G3JOB/Latest-WAR/openboxes.war
    ```
    [Download](https://bamboo-ci.pih-emr.org/browse/OPENBOXES-DS/latestSuccessful/artifact/G3JOB/Latest-WAR/openboxes.war)


#### Execute WAR file 
```shell
java -jar openboxes.war
```
You should see the application starting to boot up. 
```shell
2024-07-30 00:24:13,661  INFO [main      ] g.plugin.externalconfig.ExternalConfig  : Loading properties config file jar:file:/home/jmiranda/Downloads/openboxes.war!/WEB-INF/classes!/META-INF/grails.build.info
2024-07-30 00:24:13,679 DEBUG [main      ] g.plugin.externalconfig.ExternalConfig  : Config file file:${catalina.base}/.grails/openboxes-config.properties not found
2024-07-30 00:24:13,687 DEBUG [main      ] g.plugin.externalconfig.ExternalConfig  : Config file file:${catalina.base}/.grails/openboxes-config.groovy not found
2024-07-30 00:24:13,695 DEBUG [main      ] g.plugin.externalconfig.ExternalConfig  : Config file file:${catalina.base}/.grails/openboxes.yml not found
2024-07-30 00:24:13,704 DEBUG [main      ] g.plugin.externalconfig.ExternalConfig  : Config file file:/home/jmiranda/.grails/openboxes-config.properties not found
2024-07-30 00:24:13,713 DEBUG [main      ] g.plugin.externalconfig.ExternalConfig  : Config file file:/home/jmiranda/.grails/openboxes-config.groovy not found
2024-07-30 00:24:13,722 DEBUG [main      ] g.plugin.externalconfig.ExternalConfig  : Config file file:/home/jmiranda/.grails/openboxes.yml not found
2024-07-30 00:24:13,798  INFO [main      ] org.pih.warehouse.Application           : Starting Application on ThinkPad-P17 with PID 773976 (/home/jmiranda/Downloads/openboxes.war started by jmiranda in /home/jmiranda/Downloads)
2024-07-30 00:24:13,798 DEBUG [main      ] org.pih.warehouse.Application           : Running with Spring Boot v1.5.22.RELEASE, Spring v4.3.30.RELEASE
2024-07-30 00:24:13,798  INFO [main      ] org.pih.warehouse.Application           : The following profiles are active: production
2024-07-30 00:24:13,798 DEBUG [main      ] o.springframework.boot.SpringApplication: Loading source class org.pih.warehouse.Application
2024-07-30 00:24:14,009 DEBUG [nd-preinit] org.jboss.logging                       : Logging Provider: org.jboss.logging.Slf4jLoggerProvider found via system property
2024-07-30 00:24:14,010  INFO [nd-preinit] o.h.validator.internal.util.Version     : HV000001: Hibernate Validator 5.3.6.Final
```

Once it's finished booting up you should see the following log messages
```
2024-07-30 00:26:46,809  INFO [main      ] o.g.w.s.mvc.GrailsDispatcherServlet     : FrameworkServlet 'grailsDispatcherServlet': initialization completed in 5907 ms
2024-07-30 00:26:46,821  INFO [main      ] org.pih.warehouse.Application           : Started Application in 32.235 seconds (JVM running for 33.316)
2024-07-30 00:26:46,821  INFO [main      ] grails.boot.GrailsApp                   : Application starting in environment: production
Grails application running at http://localhost:8080/openboxes in environment: production
```

At this point, it's safe to authenticate into the server and start the [Onboarding](../../onboarding/index.md) process.


