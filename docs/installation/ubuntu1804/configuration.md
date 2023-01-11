

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
openboxes.uploads.location=/opt/tomcat/webapps/openboxes/uploads/

# Used primarily with g:link when absoluteUrl is true (e.g. links in emails)
grails.serverURL=http://localhost:8080/openboxes

# OpenBoxes mail settings - disabled by default (unless you set up an SMTP server)
#grails.mail.enabled=true
```

!!! note "Reminder" 
    Change `dataSource.username` and `dataSource.password` to the `username` and `password` you set in the `create user... identified by` SQL command for the database in the MySQL step.

!!! note "Reminder" 
    Change `grails.serverURL` to the IP address or domain name you plan to use for your server.
    
!!! note "Reminder"
    The uploads directory must be a location the tomcat user can write to, without this line it will default to /uploads which can be created manually, but won't be created automatically as tomcat cannot create a folder here. The above path should work, but you can change this to put the uploads folder anywhere provided tomcat can write to it. If the uploads folder does not exist, tomcat will attempt to create it (which will only work if it can write to the parent location)


!!! note 
    Documentation for all available configuration properties is provided in the [Configuration](/configuration) section.
