### To upgrade using the command line
1. SSH into your server

        ssh openboxes.example.com

1. Download WAR file from [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub

        wget `curl -s https://api.github.com/repos/openboxes/openboxes/releases/latest | grep browser_download_url | cut -d '"' -f 4`

1. Shutdown tomcat 

        sudo service tomcat stop

1. Remove existing deployment

        sudo rm -rf TOMCAT_HOME/webapps/openboxes*

1. Copy WAR file to Tomcat webapps directory (NOTE: we need to change the name of the WAR file)

        sudo cp openboxes.war TOMCAT_HOME/webapps/openboxes.war

1. Start Tomcat (NOTE: this may take awhile if there are lots of data migrations)
    
        sudo service tomcat start

1. Check the logs 

        sudo tail -f TOMCAT_HOME/logs/tomcat7/catalina.out

### To upgrade using Tomcat Manager 
1. Install Tomcat manager

        sudo apt-get install tomcat-admin
        
1. Edit tomcat-users.xml to add a new user (`TOMCAT_HOME/conf/tomcat-users.xml`)
    
        <user username="<username>" password="<password>" roles="manager-gui"/>

1. Restart Tomcat

        sudo service tomcat restart

1. Download WAR file from [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub

        wget `curl -s https://api.github.com/repos/openboxes/openboxes/releases/latest | grep browser_download_url | cut -d '"' -f 4`

1. Log into Tomcat Manager 
1. Undeploy all existing OpenBoxes applications 
1. Upload WAR file to Tomcat Manager (under WAR file to deploy)
1. Restart Tomcat from the command line (optional, but hightly recommended)

        sudo service tomcat restart
