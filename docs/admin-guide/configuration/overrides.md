OpenBoxes configuration properties can be overridden using multiple methods, depending on the 
deployment environment and preference.

To modify settings, update the respective configuration file and restart OpenBoxes. 
If using environment variables, restart the application for changes to take effect.

!!! important
    We currently recommend using the External Configuration File option 
    (particularly openboxes.yml), since we have not throroughly tested all of the 
    override options yet (environment variables, system properties)

[TOC]



## External Configuration File 

### openboxes.yml
Modify the openboxes.yml file to override default properties.

```yaml title="/opt/tomcat/.grails/openboxes.yml"
dataSource:
  url: jdbc:mysql://localhost:3306/changeme?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
  username: changeme
  password: changeme
grails:
  serverURL: http://localhost:8080/changeme
```

### openboxes-config.groovy
```shell title="/opt/tomcat/.grails/openboxes-config.groovy"
dataSource {
    url = "jdbc:mysql://localhost:3306/changeme?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC"
    username = "changeme"
    password = "changeme"
}
grails { 
  serverURL = "http://localhost:8080/changeme"
}
```

### openboxes-config.properties
```shell title="/opt/tomcat/.grails/openboxes-config.properties"
dataSource.url=jdbc:mysql://localhost:3306/changeme?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
dataSource.username=openboxes
dataSource.password=0p3nB0x35
grails.serverURL=http://localhost:8080/openboxes
```
!!! caution
    The .properties files don't handle boolean (true/false) values properly, so we do not 
    recommend using this option unless you are migrating from OpenBoxes 0.8.x and don't want to 
    put in the effort to covert to YAML before testing your deployment.

## Environment Variables
Some configuration properties can be overridden via environment variables. Unfortunately, we 
haven't had a chance to validate that all properties can be passed via environment variables
so your miles may vary. 

```title="/opt/tomcat/bin/setenv.sh"
export OPENBOXES_UPLOADS_DIRECTORY=/tmp/uploads
```

## Java System Properties
Some configuration properties can be passed at runtime through System Properties.
```title="/opt/tomcat/bin/setenv.sh"
export CATALINA_OPTS="$CATALINA_OPTS -Dopenboxes.uploads.location=/tmp/uploads"
```
