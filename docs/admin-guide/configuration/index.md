## Configuration file location
As mentioned in the installation instructions, you can override application configuration properties by creating a file 
at the following location:
```
$TOMCAT_HOME/.grails/openboxes-config.properties
```  

If you are deploying the application to a server, the config file is usually located under the `TOMCAT_HOME/.grails/openboxes-config.properties`.  

```
sudo mkdir /opt/tomcat/.grails
sudo touch /opt/tomcat/.grails/openboxes-config.properties
```

If you are having trouble locating where the application is looking for the file you can determine the path by 
deploying the application to Tomcat and checking the log file.  The first few lines of the Tomcat catalina.out file 
display the path(s) being used.  

**Example log file**
```
INFO: Deploying web application archive openboxes.war
Using configuration locations [classpath:openboxes-config.groovy, classpath:openboxes-config.properties, 
file:/opt/tomcat/.grails/openboxes-config.groovy, file:/opt/tomcat/.grails/openboxes-
config.properties] [staging]
Unable to load specified config location classpath:openboxes-config.groovy : class path resource 
[openboxes-config.groovy] cannot be opened because it does not exist
Unable to load specified config location classpath:openboxes-config.properties : class path resource 
[openboxes-config.properties] cannot be opened because it does not exist
Unable to load specified config location file:/opt/tomcat/.grails/openboxes-config.groovy : 
/opt/tomcat/.grails/openboxes-config.groovy (No such file or directory)
```
You can safely ignore the **Unable to load specified config location** errors for the file locations 
that you are not using.  Notice in the example log file that there are four (4) configuration locations, but 
only three (3) config locations that could not be loaded. That means that there was one (1) file found and loaded at 
one of the config file locations. Namely,
```
file:/opt/tomcat/.grails/openboxes-config.properties
```
That will tell you which file you need to edit.

!!! note
    If none of the files were loaded, then you should create a new file at the location of the 
    `openboxes-config.properties` file.

