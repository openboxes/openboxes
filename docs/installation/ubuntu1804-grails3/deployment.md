
## Stop openboxes service
```
sudo service openboxes stop
```

## Download the latest release
Currently the OpenBoxes with Grails v3 WAR is being built in the Bamboo pipeline.
You can get the latest build version with wget

```
sudo wget http://bamboo.pih-emr.org:8085/browse/OPENBOXES-SDOD2/latest/artifact/shared/Latest-WAR/openboxes.war
```

## Copy WAR file to webapps
```
sudo cp openboxes.war /opt/openboxes/openboxes.war
```

## Change file ownership
```
sudo chown openboxes:openboxes /opt/openboxes/openboxes.war
```

## Restart openboxes service
```
sudo service openboxes start
```

## Watch Tomcat logs
The deployment could take about 10-20 minutes the first time because the application needs to perform hundreds of
database migrations. Keep an eye out for any errors/exceptions that pop up in the in the log and check 
the Troubleshooting section for details on how to handle these issues.
```
journalctl -u openboxes.service -f
```

!!! note
    If you run into an error that's not covered in the [Troubleshooting Guide](troubleshooting.md) don't hesitate to
    [email support](mailto:support@openboxes.com). Please describe the problem in as much detail as you can and attach
    the catalina.out log from your Tomcat instance. 