??? warning

    Please do NOT use this page if you are upgrading from 0.8.x to 0.9.x. 
    Review the [Migration Guide](../migration) if you need more information.


# Upgrading


Once you have migrated from 0.8.x to 0.9.x, the upgrade process for subsequent releases will
go back to being straightforward.


## Assumptions
These upgrade instructions make the following assumptions

* [x] You are NOT upgrading from v0.8.x to v0.9.x release 
* [x] You have deployed the application to Tomcat 9  

    ??? note
    
          You can still use Tomcat 8.5, but the commands below assume that you are using Tomcat 9.


## Instructions

### 1. Backup Database

!!! important

    Whether you're upgrading or migrating ALWAYS backup your database just in case something 
    goes awry.

=== "SSH/SCP"

    ??? note "Assumptions"
        * Credentials are configured in ~/.my.cnf (otherwise include credentials as arguments)

    1. SSH to your application server (or database server if you're using distributed deployment)
    ```shell
    ssh <database-server-ip>
    ```
    2. Backup the database
    ```shell
    mysqldump openboxes > openboxes.sql 
    ```
    3. Copy database backup to safe place
    ```shell
    scp openboxes.sql <backup-server-ip>:<backup-directory>
    ```
=== "Remote Backup"
    
    ??? note "Assumptions"
        * MySQL/MariaDB is configured to listen for external connections
        * Credentials are configured in ~/.my.cnf (otherwise include credentials as arguments)

    1. Execute mysqldump from your local machine
        ```
        mysqldump -h <database-server-ip> openboxes > openboxes.sql
        ```
    2. Copy database backup to long-term storage
        ```
        scp openboxes.sql <backup-server-ip>
        ```


### 2. Upgrade Application

=== "Using command line"

    1. SSH into your server

            ssh app.openboxes.com

    1. Download WAR file from [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub

            wget `curl -s https://api.github.com/repos/openboxes/openboxes/releases/latest | grep browser_download_url | cut -d '"' -f 4`

    1. Shutdown tomcat 

            sudo service tomcat9 stop

    1. Remove existing deployment

            sudo rm -rf /var/lib/tomcat9/webapps/openboxes*

    1. Copy WAR file to Tomcat webapps directory (NOTE: we need to change the name of the WAR file)

            sudo cp openboxes.war /var/lib/tomcat9/webapps/openboxes.war

    1. Start Tomcat (NOTE: this may take a while if there are lots of data migrations)
    
            sudo service tomcat9 start

    1. Check the logs 

            sudo tail -f /var/lib/tomcat9/logs/catalina.out

=== "Using Tomcat Manager"

    2. Install Tomcat manager
    
            sudo apt-get install tomcat9-admin
            
    1. Edit tomcat-users.xml to add a new user (`TOMCAT_HOME/conf/tomcat-users.xml`)
        
            <user username="<username>" password="<password>" roles="manager-gui"/>
    
    1. Restart Tomcat
    
            sudo service tomcat9 restart
    
    1. Download WAR file from [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub
    
            wget `curl -s https://api.github.com/repos/openboxes/openboxes/releases/latest | grep browser_download_url | cut -d '"' -f 4`
    
    1. Log into Tomcat Manager 
    1. Undeploy all existing OpenBoxes applications 
    1. Upload WAR file to Tomcat Manager (under WAR file to deploy)
    1. Restart Tomcat from the command line (optional, but hightly recommended)
    
            sudo service tomcat9 restart
