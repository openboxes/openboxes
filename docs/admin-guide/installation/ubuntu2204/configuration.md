# Configuration

## Configuration Locations

If you installed Tomcat using APT (as instructed in the [Install Dependencies](dependencies.md) step), then there will 
be five (5) configuration file locations that can be used to store OpenBoxes configuration properties. We recommend
using the YML file, but it's entirely up to you.

* [ ] `/var/lib/tomcat9/.grails/openboxes.yml` **Recommended**
* [ ] `/var/lib/tomcat9/.grails/openboxes-config.groovy` 
* [ ] `/var/lib/tomcat9/.grails/openboxes-config.properties` 
* [ ] `/var/lib/tomcat/.grails/openboxes-config.groovy`
* [ ] `/var/lib/tomcat/.grails/openboxes-config.properties`

!!!note
    If you installed Tomcat manually or through another dependency , you might need to deploy the application once in 
    order to see where it's looking for configuration files. 

    Here's a sample log file from a recent 
    ```log
    [2024-08-09 03:55:48] [info] 2024-08-09 03:55:48,159  INFO [-utility-2] .p.w.m.SentryServletContainerInitializer: Initializing sentry-servlet
    [2024-08-09 03:55:51] [info] 2024-08-09 03:55:51,499  INFO [-utility-2] g.plugin.externalconfig.ExternalConfig  : Loading properties config file file:/var/lib/tomcat9/webapps/openboxes/WEB-INF/classes/META-INF/g
    rails.build.info
    [2024-08-09 03:55:51] [info] 2024-08-09 03:55:51,559 DEBUG [-utility-2] g.plugin.externalconfig.ExternalConfig  : Config file file:/var/lib/tomcat9/.grails/openboxes-config.properties not found
    [2024-08-09 03:55:51] [info] 2024-08-09 03:55:51,634 DEBUG [-utility-2] g.plugin.externalconfig.ExternalConfig  : Config file file:/var/lib/tomcat9/.grails/openboxes-config.groovy not found
    [2024-08-09 03:55:51] [info] 2024-08-09 03:55:51,700 DEBUG [-utility-2] g.plugin.externalconfig.ExternalConfig  : Config file file:/var/lib/tomcat9/.grails/openboxes.yml not found
    [2024-08-09 03:55:51] [info] 2024-08-09 03:55:51,767 DEBUG [-utility-2] g.plugin.externalconfig.ExternalConfig  : Config file file:/var/lib/tomcat/.grails/openboxes-config.properties not found
    [2024-08-09 03:55:51] [info] 2024-08-09 03:55:51,864 DEBUG [-utility-2] g.plugin.externalconfig.ExternalConfig  : Config file file:/var/lib/tomcat/.grails/openboxes-config.groovy not found 
    ```

## Instructions

### Step 1. Create a new configuration file
```shell
mkdir /var/lib/tomcat9/.grails
touch /var/lib/tomcat9/.grails/openboxes.yml
```

### Step 2. Add configuration properties to file 

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

### Step 3. Replace placeholder values

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
    sudo service openboxes restart
    ```

## Advanced Configuration 

Documentation for all available configuration properties will be provided in the 
[Configuration Guide](../../configuration/index.md). However, it is recommended that you 
[deploy the application](deployment.md) before proceeding to the configuration guide.
