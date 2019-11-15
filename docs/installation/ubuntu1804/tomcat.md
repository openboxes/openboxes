# Installing Tomcat 7
Because we're using Java 7, we need to install a version of Tomcat that has been compiled with Java 7. Otherwise, you'll
encounter the following error when deploying OpenBoxes.
```
2018-10-31 12:44:45,463 [localhost-startStop-1] INFO  xml.XmlBeanDefinitionReader  - Loading XML bean definitions from ServletContext resource [/WEB-INF/applicationContext.xml]
2018-10-31 12:44:46,299 [localhost-startStop-1] ERROR context.ContextLoader  - Context initialization failed
org.springframework.beans.factory.access.BootstrapException: Error executing bootstraps; nested exception is java.lang.NoSuchMethodError: java.util.concurrent.ConcurrentHashMap.keySet()Ljava/util/concurrent/ConcurrentHashMap$KeySetView;
  at org.codehaus.groovy.grails.web.context.GrailsContextLoader.createWebApplicationContext(GrailsContextLoader.java:87)
  at org.springframework.web.context.ContextLoader.initWebApplicationContext(ContextLoader.java:197)
  at org.springframework.web.context.ContextLoaderListener.contextInitialized(ContextLoaderListener.java:47)
  at org.apache.catalina.core.StandardContext.listenerStart(StandardContext.java:5068)
  at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5584)
  at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:147)
  at org.apache.catalina.core.ContainerBase.addChildInternal(ContainerBase.java:899)
  at org.apache.catalina.core.ContainerBase.addChild(ContainerBase.java:875)
  at org.apache.catalina.core.StandardHost.addChild(StandardHost.java:652)
  at org.apache.catalina.startup.HostConfig.deployWAR(HostConfig.java:1091)
  at org.apache.catalina.startup.HostConfig$DeployWar.run(HostConfig.java:1980)
  at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:471)
  at java.util.concurrent.FutureTask.run(FutureTask.java:262)
  at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
  at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
  at java.lang.Thread.run(Thread.java:745)
```

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

## Create service
```
sudo vi /etc/systemd/system/tomcat.service
```
*tomcat.service*
```
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

Environment=JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64/jre
Environment=CATALINA_PID=/opt/tomcat/apache-tomcat-7.0.91/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat/apache-tomcat-7.0.91
Environment=CATALINA_BASE=/opt/tomcat/apache-tomcat-7.0.91
Environment='CATALINA_OPTS=-Xms512m -Xmx512m -server -XX:+UseParallelGC'
Environment='JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom'

ExecStart=/opt/tomcat/apache-tomcat-7.0.91/bin/startup.sh
ExecStop=/opt/tomcat/apache-tomcat-7.0.91/bin/shutdown.sh

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

## Make service executable
```
chmod +x /etc/systemd/system/tomcat.service
```

## Register service
```
sudo systemctl daemon-reload
```

## Start Tomcat
```
sudo systemctl start tomcat
```

## Enable Tomcat to start on boot
```
sudo systemctl enable tomcat
```

## Other Systemctl commands
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

