# Reconfiguration of OpenBoxes settings
Although the basic configuration was already set in [Application](/installation/ubuntu1804-grails3/application) you can change it. If you'd like to edit the settings for the OpenBoxes you can edit the openboxes.yml with nano or vim.
```
nano /opt/openboxes/.grails/openboxes.yml
```

Once you do - remember to restart the openboxes service
```
sudo service openboxes restart
```


!!! note "Reminder" 
    Change `dataSource.username` and `dataSource.password` to the `username` and `password` you set in the `grant all` command above.

!!! note "Reminder" 
    Change `grails.serverURL` to the IP address or domain name you plan to use for your server.

!!! note 
    Documentation for all available configuration properties is provided in the [Configuration](/configuration) section.