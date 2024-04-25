# Installing Tomcat 7
Because we're using Java 7, we need to install a version of Tomcat that has been compiled with Java 7. Otherwise, you'll
encounter the following error when deploying OpenBoxes.

!!! note "Important"
    Tomcat 7 in the APT repository was compiled with Java 8, which causes the aforementioned error during deployment.
    
## References
* [https://whiscardz.wordpress.com/tag/java7-tomcat-7-on-ubuntu-16-04/](https://whiscardz.wordpress.com/tag/java7-tomcat-7-on-ubuntu-16-04/)
* [https://www.techrepublic.com/article/how-to-install-apache-tomcat-on-ubuntu-server-16-04/](https://www.techrepublic.com/article/how-to-install-apache-tomcat-on-ubuntu-server-16-04/)


## Getting Started

* Go to the [Tomcat Downloads](https://tomcat.apache.org/download-70.cgi) page
* Choose a mirror (or leave the default)
* Right-click on the Core tar.gz link (paste in step 3 below)

## Download and unpack
```
$ cd /opt
$ sudo wget http://mirror.metrocast.net/apache/tomcat/tomcat-7/v7.0.94/bin/apache-tomcat-7.0.94.tar.gz
$ sudo tar xvzf apache-tomcat-7.0.94.tar.gz
$ ln -s /opt/apache-tomcat-7.0.94 tomcat
$ rm apache-tomcat-7.0.94.tar.gz
```

## Create user, group and change permissions
```
$ sudo groupadd tomcat
$ sudo useradd -s /bin/false -g tomcat -d /opt/tomcat tomcat 
$ sudo chown -R tomcat:tomcat /opt/apache-tomcat-7.0.94
# sudo chmod +x /opt/tomcat/bin/*.sh
```

## Determine Java JRE path
Copy and paste the output. It will be used to replace `<PASTE_PATH_TO_JRE_HERE>` in tomcat.service.
```
$ readlink -f /etc/alternatives/java
/usr/lib/jvm/zulu-7-amd64/jre/bin/java
```
Copy the path up to `/bin/java`. For example, `/usr/lib/jvm/zulu-7-amd64/jre`.

## Create service
```
sudo vi /etc/systemd/system/tomcat.service
```

/etc/systemd/system/tomcat.service
```
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

Environment=JAVA_HOME=<PASTE_PATH_TO_JRE_HERE>
Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat
Environment='CATALINA_OPTS=-Xms1024m -Xmx1024m -XX:MaxPermSize=128m -server -XX:+UseParallelGC'
Environment='JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom'

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
```

!!! note
    You will likely encounter OutOfMemoryErrors with Tomcat's default memory settings.  
    
    You may be able to get away with using 256m as the max heap size, but 512m is a good setting, 
    even for production environments.  
    
If you are in a limited memory environment (like an EC2 t2.micro which only has 1GB of memory) you 
will need to reduce your memory settings a little more. 
```
Environment='CATALINA_OPTS=-Xms128m -Xmx256m -XX:MaxPermSize=128m -Djava.security.egd=file:/dev/./urandom -server -XX:+UseParallelGC'
```

Unfortunately, with so little memory allocated you will probably run into several types of OutOfMemoryError issues 
(see Troublshooting section below).

## Make Tomcat service executable
```
chmod +x /etc/systemd/system/tomcat.service
```
## Reload services
```
sudo systemctl daemon-reload
```

## Start Tomcat
```
sudo systemctl start tomcat
```

## Enable Tomcat service
```
sudo systemctl enable tomcat
```

## Systemctl commands
```
systemctl start tomcat
systemctl stop tomcat
systemctl status tomcat
```

## Service wrapper
At this point I generally go back to using the Ubuntu's `service` wrapper which abstracts the underlying implementation 
(could be `/etc/init.d`, Upstart, or `systemctl`). But it's up to you whether you want to continue using `systemctl` 
or switch back to `service`.

Here are the commands available if using the `service` wrapper:
```
sudo service tomcat status
sudo service tomcat stop
sudo service tomcat start
sudo service tomcat restart
```

## Configure Tomcat manager **(optional)**
This configuration can be used to make future upgrades through the Tomcat manager web interface. Make sure the file is 
only readable by root and/or the user that runs Tomcat. Replace <manager-gui-\*> and <manager-script-\*> with 
appropriate values. See Tomcat docs for more information 
(https://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html#Configuring_Manager_Application_Access).

*/opt/tomcat/apache-tomcat-7.0.91/conf/tomcat-users.xml*
```
<tomcat-users>
  <role rolename="manager-gui"/>
  <role rolename="manager-script"/>
  <role rolename="manager-jmx"/>
  <role rolename="manager-status"/>
  <user username="<MANAGER-GUI-USERNAME>" password="<GUI-PASSWORD>" roles="manager-gui"/>
  <user username="<MANAGER-SCRIPT-USERNAME>" password="<SCRIPT-PASSWORD>" roles="manager-script"/>
</tomcat-users>
```
NOTE: Please don't use obvious passwords (i.e. tomcat, password, s3cret, etc) because your server will get exploited. 

