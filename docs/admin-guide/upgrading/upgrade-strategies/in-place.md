# In-Place Upgrade Instructions

An in-place upgrade is when you upgrade the application and its dependencies directly on the existing server.

!!! danger

    In-place Upgrades have the potential to be destructive. If you're doing a major or minor upgrade
    (Ex: 0.8.x -> 0.9.x) or if you don't have a staging server to test the upgrade on, we strongly encourage you to
    follow the [parallel upgrade](./parallel.md) strategy instead.

    Additionally, if your version upgrade has special instructions (such as with the
    [0.8.x to 0.9.x upgrade](../version-specific-instructions/upgrading-from-08x-to-09x.md)), please read through the
    version-specific upgrade documentation in its entirety before proceeding. It is likely that in-place upgrades
    are further discouraged in those scenarios.

!!! tip

    If you would like advice or support, please feel free to reach out to us on our
    [Community discussion forum](https://community.openboxes.com).


## Upgrade Overview

In-place upgrades require you to complete the following steps:

* [ ] 1. Assess your current setup to see if an in-place migration makes sense for you
* [ ] 2. Bring down the app server
* [ ] 3. Backup your database
* [ ] 4. Backup your custom configuration
* [ ] 5. Upgrade dependencies
* [ ] 6. Upgrade app
* [ ] 7. Start the server
* [ ] 8. Test the upgrade
* [ ] 9. Remove the old dependencies


## Step-by-step Instructions


### 1. Environment Assessment

Before you even begin the upgrade, we want to encourage you once again to review any upgrade instructions that are
specific to your release. Verify that the dependency versions that you have installed are the same as those defined
in the upgrade instructions. If the versions differ, know that you're entering into an unpredictable area. In that
situation, we again urge you to switch to a parallel upgrade if at all possible.


### 2. Bring down the app server

Because this is an in-place upgrade, the first thing we have to do is stop your application server.

!!! note
    This is where your downtime begins.

{% include 'admin-guide/common/_stop_server.md' %}


### 3.Backup Database

Before starting the upgrade process, make sure to take a full backup of your existing databases. This backup will serve
as a safety net in case anything goes wrong during the upgrade process.

{% include 'admin-guide/common/_backup_database.md' %}


### 4. Backup Custom Config

Similarly, if you've ever made configuration changes to the app or its dependencies over the years, you'll want to
take a backup of those changes in case you need to revert back to them if anything goes wrong during the upgrade.

{% include 'admin-guide/common/_determining_custom_config.md' %}


### 5. Upgrade Dependencies

Our major and minor releases may require dependency upgrades, such as a new Java or Tomcat version. This is the time
to perform those upgrades.

!!! warning
    This is the point of no return. Make sure you have your system properly backed up in case of errors.


Make sure to check if there are special instructions for the version you're upgrading to (for example, the
[0.8.x to 0.9.x upgrade](../version-specific-instructions/upgrading-from-08x-to-09x.md)). Those instructions will
typically inform you of what dependencies need to be upgraded and how.


### 6. Upgrade The App

Now that your dependencies are up-to-date, it's time to update the application itself.

=== "Using command line"

    #### 1. SSH into your server

        ssh app.openboxes.com

    #### 2. Download WAR file from [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub

        wget `curl -s https://api.github.com/repos/openboxes/openboxes/releases/latest | grep browser_download_url | cut -d '"' -f 4`

    #### 3. Remove existing deployment

        sudo rm -rf /var/lib/tomcat9/webapps/openboxes*

    #### 4. Copy WAR file to Tomcat webapps directory (NOTE: we need to change the name of the WAR file)

        sudo cp openboxes.war /var/lib/tomcat9/webapps/openboxes.war


=== "Using Tomcat Manager"

    #### 1. Install Tomcat manager
    
        sudo apt-get install tomcat9-admin
            
    #### 2. Add a new Tomcat user
        
    Edit `TOMCAT_HOME/conf/tomcat-users.xml`

        <user username="<username>" password="<password>" roles="manager-gui"/>
    
    For example:

        <user username="myname" password="mypassword" roles="manager-gui"/>

    #### 3. Restart Tomcat
    
        sudo service tomcat9 restart
    
    #### 4. Download WAR file from [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub
    
        wget `curl -s https://api.github.com/repos/openboxes/openboxes/releases/latest | grep browser_download_url | cut -d '"' -f 4`
    
    #### 5. Log into Tomcat Manager 

    #### 6. Undeploy all existing OpenBoxes applications 

    #### 7. Upload WAR file to Tomcat Manager (under WAR file to deploy)


### 7. Start The Server

With the app and its dependencies properly upgraded, all we need to do now is start the server.

!!! note
    This is where your downtime should end.

{% include 'admin-guide/common/_start_server.md' %}


### 8. Test The Upgrade

With the server started, it's time to verify that the installation worked.

{% include 'admin-guide/common/_verifying_installation.md' %}


### 9. Remove Old Dependencies (optional)

Once enough time has past that you feel confident the upgrade has succeeded, you can optionally remove the old
dependency versions that you just upgraded from (Java, Apache, Tomcat...). This will keep your machine
in a cleaner state, which will help reduce risk when performing future upgrades.
