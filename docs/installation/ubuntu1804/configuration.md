# configuration

1. Create openboxes-config.properties file

   ```text
   cd /opt/tomcat
   sudo mkdir /opt/tomcat/.grails
   sudo vi /opt/tomcat/.grails/openboxes-config.properties
   ```

2. Copy the following contents into openboxes-config.properties

**/opt/tomcat/.grails/openboxes-config.properties**

```text
# Database connection settings
dataSource.username=<username>
dataSource.password=<password>
dataSource.url=jdbc:mysql://localhost:3306/openboxes?useSSL=false

# Used primarily with g:link when absoluteUrl is true (e.g. links in emails)
grails.serverURL=http://localhost:8080/openboxes

# OpenBoxes mail settings - disabled by default (unless you set up an SMTP server)
#grails.mail.enabled=true
```

!!! note "Reminder" Change `dataSource.username` and `dataSource.password` to the `username` and `password` you set in the `grant all` command above.

!!! note "Reminder" Change `grails.serverURL` to the IP address or domain name you plan to use for your server.

!!! note Documentation for all available configuration properties is provided in the [Configuration](https://github.com/openboxes/openboxes/tree/ce29e7cd11a8a01a369e191de532c747c20c6040/configuration/README.md) section.

