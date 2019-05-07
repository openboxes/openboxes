# Configuration

## Configure Environment
Determine the path to Java 
```
$ sudo update-java-alternatives --list
java-1.7.0-openjdk-amd64       1071       /usr/lib/jvm/java-1.7.0-openjdk-amd64
``` 

### Add environment variables to `~/.bashrc`
```
export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64
export CATALINA_HOME=/opt/tomcat/apache-tomcat-7.0.91
```

### Refresh environment
```
. ~/.bashrc
```

---
## Configure Application 


## Create openboxes-config.properties file
```
cd /opt/tomcat
sudo mkdir /opt/tomcat/.grails
sudo vi /opt/tomcat/.grails/openboxes-config.properties
```

## Copy the following contents into opneboxes-config.properties
**/opt/tomcat/.grails/openboxes-config.properties**
```
# Database connection settings
dataSource.username=<username>
dataSource.password=<password>
dataSource.url=jdbc:mysql://localhost:3306/openboxes?useSSL=false

# Used primarily with g:link when absoluteUrl is true (e.g. links in emails)
grails.serverURL=http://localhost:8080/openboxes

# OpenBoxes mail settings - disabled by default (unless you set up an SMTP server)
#grails.mail.enabled=true
```

!!! note "Reminder" 
    Change `dataSource.username` and `dataSource.password` to the `username` and `password` you set in the `grant all` command above.

!!! note "Reminder" 
    Change `grails.serverURL` to the IP address or domain name you plan to use for your server.


!!! note 
    Documentation for all available configuration properties is provided in the Configuration section.