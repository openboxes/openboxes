# Installing Apache 2 (optional)
The best way to enable HTTPS is to add a load balancer using your hosting provider. One of your next best options is 
to install Apache or Nginx locally. The nice thing about this option is that it's fairly straightforward and works
very well. However, it's not really a load balancer since it's only fronting a single Tomcat instance.
```
sudo apt-get install apache2
```
!!! note
    To enable request delegation from Apache to Tomcat, please see [Configuration Guide > Tomcat AJP](/configuration/ajp).
    
!!! note 
    To enable HTTPS, please see [Configuration Guide > Apache HTTPS](/configuration/https).