# AJP 

## Configure mod_jk

```
sudo apt-get install libapache2-mod-jk
```

## Enable mod_jk 

```
sudo a2enmod jk
```

## Update workers.properties
*/etc/libapache2-mod-jk/workers.properties*
```
workers.tomcat_home=/opt/tomcat/apache-tomcat-7.0.91
workers.java_home=/usr/lib/jvm/java-7-openjdk-amd64
```

## Configure Tomcat server.xml

Make sure that the following line in server.xml is uncommented.

```
<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
```

If you needed to uncomment it, save the file, and then restart Tomcat for the changes to take effect.
```
sudo service tomcat restart
```


## To delegate requests from Apache to Tomcat
Add the following line to your apache configuration in sites-enabled. If you've enabled HTTPS already, you might have
to edit two files. However, if you enabled redirect 

- /etc/apache2/sites-enabled/000-default.conf
- /etc/apache2/sites-enabled/000-default-le-ssl.conf

```
JkMount /manager* ajp13_worker
JkMount /openboxes* ajp13_worker
```

## To redirect requests from the root context to /openboxes
```
RedirectMatch ^/$ /openboxes/
```

## Restart Apache
```
sudo service apache2 restart
```

