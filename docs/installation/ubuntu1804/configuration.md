

1. Create openboxes-config.properties file
```
cd /opt/tomcat
sudo mkdir /opt/tomcat/.grails
sudo vi /opt/tomcat/.grails/openboxes-config.properties
```

1. Copy the following contents into openboxes-config.properties

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
    Documentation for all available configuration properties is provided in the [Configuration](/configuration) section.