# 2. Upgrade The App

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
