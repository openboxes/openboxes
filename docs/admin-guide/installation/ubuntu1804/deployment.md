
## Stop tomcat
```
$ sudo service tomcat stop
```

## Download latest release
1. Go to the the [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub.
1. Right-click on `openboxes.war` 
1. Select **Copy link address**
1. Paste the link address in the following command

```
$ sudo wget https://github.com/openboxes/openboxes/releases/download/<version>/openboxes.war
```

## Copy WAR file to webapps
```
$ sudo cp openboxes.war /opt/tomcat/webapps/openboxes.war
```

## Change file ownership
```
$ sudo chown tomcat:tomcat /opt/tomcat/webapps/openboxes.war
```

## Restart Tomcat
```
$ sudo service tomcat start
```

## Watch Tomcat logs
The deployment could take about 10-20 minutes the first time because the application needs to perform hundreds of
database migrations. Keep an eye out for any errors/exceptions that pop up in the `catalina.out` log file and check 
the Troubleshooting section for details on how to handle these issues.
```
$ sudo tail -f /opt/tomcat/apache-tomcat-7.0.94/logs/catalina.out
```

!!! note
    If you run into an error that's not covered in the [Troubleshooting Guide](troubleshooting.md) don't hesitate to
    [email support](mailto:support@openboxes.com). Please describe the problem in as much detail as you can and attach
    the catalina.out log from your Tomcat instance. 