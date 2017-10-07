# Configuration

## Configuration file location
As mentioned in the installation instructions, you can override application configuration properties by creating a file called `$USER_HOME/.grails/openboxes-config.properties`.  If you are a developer, `$USER_HOME` should resolve to your home directory (e.g. `/home/jmiranda` on Ubuntu).  If you are deploying the application to Tomcat, the file is usually located under the `TOMCAT_HOME/.grails/openboxes-config.properties`.  

If you are having trouble locating where the application is looking for the file you can determine the path by deploying the application to Tomcat and checking the log file.  The first few lines of the Tomcat catalina.out file display the path(s) being used.  You can safely ignore the "Unable to load specified config location" for the locations that you are not using.  Notice below that there are 4 configuration locations and only 3 `Unable to load specified config location` lines.  That means that there was a file found at one of the locations (namely, `file:/usr/local/tomcat6/.grails/openboxes-config.properties`.

```
INFO: Deploying web application archive openboxes.war
Using configuration locations [classpath:openboxes-config.groovy, classpath:openboxes-config.properties, 
file:/usr/local/tomcat6/.grails/openboxes-config.groovy, file:/usr/local/tomcat6/.grails/openboxes-
config.properties] [staging]
Unable to load specified config location classpath:openboxes-config.groovy : class path resource 
[openboxes-config.groovy] cannot be opened because it does not exist
Unable to load specified config location classpath:openboxes-config.properties : class path resource 
[openboxes-config.properties] cannot be opened because it does not exist
Unable to load specified config location file:/usr/local/tomcat6/.grails/openboxes-config.groovy : 
/usr/local/tomcat6/.grails/openboxes-config.groovy (No such file or directory)
```