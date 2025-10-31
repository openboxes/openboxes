As mentioned in the installation instructions, you can override application configuration 
properties by creating a configuration file in one of the supported locations.

The first config file has the lowest priority. Its contents can be overwritten by any file 
following it that sets the same config key. In other words, the *last* config file has the 
final word (highest priority).

```yaml
grails:
  config:
    locations:
        - classpath:META-INF/grails.build.info
        - file:${catalina.base}/.grails/openboxes-config.properties
        - file:${catalina.base}/.grails/openboxes-config.groovy
        - file:${catalina.base}/.grails/openboxes.yml
        - ~/.grails/openboxes-config.properties
        - ~/.grails/openboxes-config.groovy
        - ~/.grails/openboxes.yml
```

We recommend using the following config file location for new installations.
```
$TOMCAT_HOME/.grails/openboxes.yml
```

However, we also support legacy formats (.properties, .groovy) from previous versions of OpenBoxes. 
So if you're migrating from a server running the 0.8.x application, you can simply copy the files 
from your existing implementation.
```
$TOMCAT_HOME/.grails/openboxes-config.groovy
$TOMCAT_HOME/.grails/openboxes-config.properties
```  

If you are deploying the application to a server, the config file is usually located under 
the `$TOMCAT_HOME/.grails/` directory.  This directory is not created for you so you'll need to 
run the following commands to create the file and make it accessible by the Tomcat user.
```
sudo mkdir /opt/tomcat/.grails
sudo touch /opt/tomcat/.grails/openboxes.yml
sudo chown tomcat:tocmat /opt/tomcat/.grails/openboxes.yml
```


### Troubleshooting
If you are having trouble locating where the application is looking for the file you can 
determine the path by deploying the application to Tomcat and checking the log file.  The first 
few lines of the Tomcat catalina.out file display the path(s) being used.  

```shell title="catalina.out"
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

```shell
file:/opt/tomcat/.grails/openboxes-config.properties
```

That will tell you which file you need to edit.

!!! note
    If none of the files were loaded, then you should create a new file at the location of the 
    `openboxes-config.properties` file.

## Overriding Configuration Locations
If you should need to override the default configuration locations, you can create custom 
externalized configuration locations by placing them in an external file 
and loading them via `openboxes.yml` by changing the default config locations. 

```yaml
grails:
    config:
        locations:
            - classpath:openboxes-overrides.yml
            - /etc/openboxes/openboxes-config.groovy
```

!!! danger

    Please note that overriding the default config locations will likely remove the default
    locations. 
