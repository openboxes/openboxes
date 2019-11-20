Now that you have OpenBoxes running on Tomcat you might have noticed that you need to access
the server using an IP address over port 8080. That doesn't seem great. On top of that the 
connection is not secure ()

(add screenshot showing insecure connection)

## Enable HTTPS
### Install Apache2
```
sudo apt-get install apache2
```

### Install Let's Encrypt (Certbot)
Follow the instructions in the tutorial below.

https://www.digitalocean.com/community/tutorials/how-to-secure-nginx-with-let-s-encrypt-on-ubuntu-14-04


## Configure Apache to delegate requests to Tomcat

You can configure this however you'd like (mod_jk, mod_proxy_ajp) but the easiest way is probably 
to use mod_jk and configure your Apache VirtualHost to delegate `/openboxes` requests to Tomcat.

### Install mod_jk
Following the instructions from the tutorial below.

https://www.digitalocean.com/community/tutorials/how-to-encrypt-tomcat-8-connections-with-apache-or-nginx-on-ubuntu-16-04
```
sudo apt-get install libapache2-mod-jk
```

### Configure Tomcat to listen on port 8009 (AJP)
Uncomment the following line in `/var/lib/tomcat7/conf/server.xml`

    <!-- Define an AJP 1.3 Connector on port 8009 -->
    <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />


## Configure Apache to delegate requests to Tomcat

### Check workers.properties (/etc/libapache2-mod-jk/workers.properties)
Make sure these properties look ok.
```
workers.tomcat_home=/usr/share/tomcat7
workers.java_home=/usr/lib/jvm/default-java
```

```
ls -al /usr/share/tomcat7
ls -al /usr/lib/jvm/default-java
```





