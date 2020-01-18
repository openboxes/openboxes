# index

## To upgrade using the command line

1. SSH into your server

   ```text
    ssh openboxes.example.com
   ```

2. Download WAR file from [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub

   ```text
    wget `curl -s https://api.github.com/repos/openboxes/openboxes/releases/latest | grep browser_download_url | cut -d '"' -f 4`
   ```

3. Shutdown tomcat

   ```text
    sudo service tomcat stop
   ```

4. Remove existing deployment

   ```text
    sudo rm -rf TOMCAT_HOME/webapps/openboxes*
   ```

5. Copy WAR file to Tomcat webapps directory \(NOTE: we need to change the name of the WAR file\)

   ```text
    sudo cp openboxes.war TOMCAT_HOME/webapps/openboxes.war
   ```

6. Start Tomcat \(NOTE: this may take awhile if there are lots of data migrations\)

   ```text
    sudo service tomcat start
   ```

7. Check the logs

   ```text
    sudo tail -f TOMCAT_HOME/logs/tomcat7/catalina.out
   ```

## To upgrade using Tomcat Manager

1. Install Tomcat manager

   ```text
    sudo apt-get install tomcat-admin
   ```

2. Edit tomcat-users.xml to add a new user \(`TOMCAT_HOME/conf/tomcat-users.xml`\)
3. Restart Tomcat

   ```text
    sudo service tomcat restart
   ```

4. Download WAR file from [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub

   ```text
    wget `curl -s https://api.github.com/repos/openboxes/openboxes/releases/latest | grep browser_download_url | cut -d '"' -f 4`
   ```

5. Log into Tomcat Manager
6. Undeploy all existing OpenBoxes applications 
7. Upload WAR file to Tomcat Manager \(under WAR file to deploy\)
8. Restart Tomcat from the command line \(optional, but hightly recommended\)

   ```text
    sudo service tomcat restart
   ```

