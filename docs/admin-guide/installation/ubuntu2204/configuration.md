
!!! note "Assumptions"
    This document assume you installed Tomcat using the manual installation method. If you 
    installed Tomcat using the tomcat9 APT repository package, then you just need to change
    all Tomcat directory paths from `/opt/tomcat` to `/var/lib/tomcat9`.

## Configuration Locations

If you installed Tomcat manually (as recommended in the [Install Tomcat](tomcat.md) step), then 
there will be at least three (3) configuration file locations that can be used to store OpenBoxes 
configuration properties.

* /opt/tomcat/.grails/openboxes.yml **[Recommended]**
* /opt/tomcat/.grails/openboxes-config.groovy
* /opt/tomcat/.grails/openboxes-config.properties 

As indicated above, we recommend using the YML file, particularly for new installations. However, 
it's entirely up to you. If you are installing from scratch but have existing configuration 
files (i.e. you're migrating to a new server) it's much easier to copy those configuration file(s) 
to get started and use openboxes.yml for all additional configuration overrides.

!!!note
    Whether you installed Tomcat manually or through the APT repository, you might need to deploy 
    the application as a dry run to enumerate all of the configuration file locations and what order
    they are being read and loaded. The order will likely impact 

    Here's the log from a manually installed version of Tomcat
    ```
    2025-01-18 06:27:07,566 INFO  [main] g.plugin.externalconfig.ExternalConfig: Loading properties config file file:/opt/apache-tomcat-9.0.98/webapps/openboxes/WEB-INF/classes/META-INF/grails.build.info
    2025-01-18 06:27:08,157 DEBUG [main] g.plugin.externalconfig.ExternalConfig: Config file file:/opt/apache-tomcat-9.0.98/.grails/openboxes-config.properties not found
    2025-01-18 06:27:08,259 DEBUG [main] g.plugin.externalconfig.ExternalConfig: Config file file:/opt/apache-tomcat-9.0.98/.grails/openboxes-config.groovy not found
    2025-01-18 06:27:08,386 DEBUG [main] g.plugin.externalconfig.ExternalConfig: Config file file:/opt/apache-tomcat-9.0.98/.grails/openboxes.yml not found
    2025-01-18 06:27:08,483 DEBUG [main] g.plugin.externalconfig.ExternalConfig: Config file file:/opt/tomcat/.grails/openboxes-config.properties not found
    2025-01-18 06:27:08,572 DEBUG [main] g.plugin.externalconfig.ExternalConfig: Config file file:/opt/tomcat/.grails/openboxes-config.groovy not found
    2025-01-18 06:27:08,685 DEBUG [main] g.plugin.externalconfig.ExternalConfig: Config file file:/opt/tomcat/.grails/openboxes.yml not found
    ```

## Create a new configuration file
```shell
mkdir /opt/tomcat/.grails
touch /opt/tomcat/.grails/openboxes.yml
```

## Add configuration properties to file 

Copy the configuration below into the newly created configuration file.

=== "openboxes.yml"
```yml

dataSource:
    url: jdbc:mysql://localhost:3306/<database>?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
    username: <username>
    password: <password>

grails:
    serverURL: http://<server-ip-or-hostname>:8080/openboxes
```

## Replace placeholder values

* Replace the `<database>` placeholder in the `dataSource.url` 
* Replace the placeholder values in `dataSource.username` and `dataSource.password` with the `username` and `password` 
used in the `create user` command when [creating the database](database.md) in the previous step.
* Replace the `<server-ip-or-hostname>` placeholder in the `grails.serverURL` to include the IP address or domain name you plan to use for your server. 

!!! note  
    Later in this guide (Post-Installation steps) we're going to configure the server to accept only HTTPS requests
    so we'll need to eventually edit the configuration file again to change the protocol in the `grails.serverURL` 
    from `http://` to `https://`.

!!! info
    At this point, we have not deployed the application so there's no reason to restart Tomcat.
    However, if you ever make changes to this file in the future, you'll need to execute the following command
    in order for the changes to be recognized.
    ```
    sudo service tomcat restart
    ```

## Advanced Configuration 

Documentation for all available configuration properties will be provided in the 
[Configuration Guide](../../configuration/index.md). However, it is recommended that you 
[deploy the application](deployment.md) before proceeding to the configuration guide.
