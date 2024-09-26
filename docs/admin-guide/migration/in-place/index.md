
# Migrating In-Place 
This approach requires you to upgrade the application and its dependencies directly on the existing server.

!!! danger
    We strongly encourage you to use the Parallel Migration strategy. The In-Place Migration 
    strategy is experimental 

## Assumptions
* Ubuntu 18.04 is the operating system 
* Java 7 is installed
* Tomcat 7 is installed under /opt/tomcat
* tomcat user has been created
* MySQL 5.7 is installed
* Apache 2.2 is installed 
* OpenBoxes v0.8.x is installed and running
* OpenBoxes v0.8.x configuration is stored under /opt/tomcat/.grails

!!! note
    If any of these assumptions are not valid, it is important to spend time 
    reviewing your environment (Step 2) to make sure you understand the migration steps you will 
    need to execute in order to upgrade the application. In addition, you should plan for 
    contingencies and document your own rollback plan as you are about to embark on a migration
    that is not officially supported. 

!!! tip 
    If you would like advice or support, please feel free to reach out to us on our 
    [Community discussion forum](https://community.openboxes.com).

## Considerations
* :white_check_mark: Minimal changes to the infrastructure
* :white_check_mark: Usually faster than setting up a new server
* :white_check_mark: Retains current configurations and settings
* :warning: Once you start, it's hard to turn back
* :warning: Risk of downtime and service disruption
* :warning: Potential for incompatibility issues
* :warning: Harder to rollback if something goes wrong

## Basic Procedure

* [ ] Backup Database: Ensure you have a complete backup of the application data and configuration.
* [ ] Environment Assessment: Check compatibility of new dependencies with the current server environment.
* [ ] Perform Migration: Perform the upgrade of dependencies and the application.
* [ ] Testing: Test the application thoroughly in the upgraded environment.
* [ ] Rollback Plan: Have a rollback plan in case issues arise during or after the upgrade.
* [ ] Monitoring: Keep an eye on the server to ensure there's no funny business.
* [ ] Remove Old Dependencies [optional]: Once you're beyond an acceptable rollback timeframe you can remove old dependencies.   


## Step-by-step Instructions

### Backup Database

{% include 'admin-guide/migration/backup_database.md' %}

### Environment Assessment

There's a chance you have made configuration changes to dependencies over the years. You will need
to ensure that you compile a list of all changes and determine whether those need to be applied 
when you install the latest dependencies.

* [ ] MySQL 5.7
    * [ ] /etc/mysql/mysql.conf.d/mysql.cnf
    * [ ] /etc/mysql/mysql.conf.d/mysqld.cnf
* [ ] Tomcat 7
    * [ ] /opt/tomcat/bin/setenv.sh 
    * [ ] /opt/tomcat/conf/server.xml
    * [ ] /etc/systemd/system/tomcat.service
* [ ] Java 7
* [ ] Apache 2.2
    *  [ ] /etc/apache2/sites-enabled/000-default-le-ssl.conf
* [ ] OpenBoxes v0.8.x 
    * [ ] /opt/tomcat/.grails/openboxes-config.properties
    * [ ] /opt/tomcat/.grails.openboxes-config.groovy

!!! tip 
    One easy way to remind yourself what you've changed on a particular server would be to go 
    through your Bash History and see what files have been changed. 

    The following command will show you all of the files you've opened with vi, which could lead
    to some 

        $ history | grep vi

## Perform Migration

### Operating System (optional)
If you are currently on an older distribution of Ubuntu (14.04 or 16.04) we would highly recommend
aborting the in-place upgrade and 

If you are running a more recent LTS distribution like Ubuntu 18.04 or 20.04, it might be possible 
to perform a dist-upgrade to migrate to upgrade your dependencies.  



```
sudo dist-upgrade
```

### Java 

=== "Zulu"

        sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9
        sudo apt-add-repository 'deb http://repos.azulsystems.com/ubuntu stable main' -y
        sudo apt-get update
        sudo apt-get install zulu-8 -y


### Application Server

!!! todo

    I would prefer if we install Tomcat under /opt and then create a symbolic link /opt/tomcat to 
    the version you want to use. 

=== "Tomcat 9 (APT)"

        sudo apt install tomcat9 

=== "Tomcat 9 (Manual)"

        wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.91/bin/apache-tomcat-9.0.91.tar.gz
        tar xfv apache-tomcat-9.0.91.tar.gz
        sudo mv apache-tomcat-9.0.91 /opt/tomcat9
        sudo chown -R tomcat:tomcat /opt/tomcat9
        sudo chmod +x /opt/tomcat9/bin/*
        rm apache-tomcat-9.0.91.tar.gz

=== "Tomcat 8.5 (Manual)"

        wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.89/bin/apache-tomcat-8.5.89.tar.gz
        tar xfv apache-tomcat-8.5.89.tar.gz
        sudo mv apache-tomcat-8.5.89 /opt/tomcat85
        sudo chown -R tomcat:tomcat /opt/tomcat85
        sudo chmod +x /opt/tomcat85/bin/*
        rm apache-tomcat-8.5.89.tar.gz

### Web Server / HTTPS / SSL (optional)
This step is optional since you can stay with Apache 2, if that's what was installed previously. We
have started to migrate some of our production environments to use nginx, so that's why these
instructions are included.

=== "Apache"

    If you've configured SSL/HTTPS on Apache 2 then you can stay with that configuration. You might 
    need to re-configure Apache 2 to delegate requests to Tomcat 8.5 or Tomcat 9 through mod_jk or 
    whatever plugin was used. We'll try to upgrade the docs to include those instructions soon.
    
    !!! todo
    
            Include configuration changes for mod_jk or mod_http or whatever. 

=== "nginx"
    
    #### Install nginx apt
    
        sudo apt-get install nginx -y
    
    #### Configure SSL
    
        sudo touch /etc/nginx/conf.d/default.conf
        cat 9_nginx_config.template > /etc/nginx/conf.d/default.conf
        echo "Bruteforce creating missing files in the /etc/letsencrypt"
        curl https://raw.githubusercontent.com/certbot/certbot/master/certbot-nginx/certbot_nginx/_internal/tls_configs/options-ssl-nginx.conf
        curl https://raw.githubusercontent.com/certbot/certbot/master/certbot/certbot/ssl-dhparams.pem > /etc/letsencrypt/ssl-dhparams.pem
    
    #### Disable Apache2 
    
        service apache2 stop
        systemctl daemon-reload
        systemctl disable apache2
    
    #### Enable Nginx
    
        systemctl enable nginx
        service nginx start

=== "Tomcat"
    
    This documentation does not officially support enabling HTTPS/SSL on Tomcat, but there's 
    nothing preventing you from using this feature.  

## Upgrade Application

### Backup Database 

### Configuration

### Swap Tomcat

## Validation 

* Check logs to make sure application has started that all database migrations have been executed without errors.
* Check the application 
* Check Sentry (if applicable)





