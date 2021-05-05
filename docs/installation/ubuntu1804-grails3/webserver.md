# Installing Apache 2/Nginx (optional)
You could install either install Nginx or Apache to serve as a webserver in front of Tomcat

## Installing Nginx

```
sudo apt-get install nginx
```

1.) Create Nginx reverse proxy configuration using heredoc
```
sudo bash -c 'cat <<-EOT > /etc/nginx/sites-available/reverse-proxy.conf
server {
    listen 80;

    access_log /var/log/nginx/reverse-access.log;
    error_log /var/log/nginx/reverse-error.log;

    location / {
        proxy_set_header   X-Forwarded-For /$remote_addr;
        proxy_pass         "http://127.0.0.1:8080";
    }
}
EOT'
```

2.) Enable Nginx reverse proxy configuration
```
sudo unlink /etc/nginx/sites-enabled/default
sudo ln -s /etc/nginx/sites-available/reverse-proxy.conf /etc/nginx/sites-enabled/reverse-proxy.conf
sudo service nginx restart
```

!!! note
    Section about setting of the HTTPS in Nginx will be here

## Installing Apache 2

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

