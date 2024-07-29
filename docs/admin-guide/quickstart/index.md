
# Quickstart

!!! warning 

    These quickstart instructions are not recommended for production 
    environments. Please use the installation guide.

## Step 0: Verify Prerequisites 
* Operating System: Ubuntu 22.04+ 
* Memory: 8GB

## Step 1: Create Database

### Install dependencies
```shell
sudo apt install mysql-server
```

### Create Database

```shell
mysql -e 'create database openboxes default charset utf8'
```

### Create Database User
```shell
mysql -e 'grant all on openboxes.* to openboxes@localhost identified by "openboxes"'
```

## Step 2: Setup Environment 

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



## Step 3: Start Application 

### Download WAR file

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


### Install Java 8
```shell 
sudo apt-get install openjdk-8-jdk
```

### Execute WAR file 
```shell
java -jar openboxes.war

```

