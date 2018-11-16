# References 
* SSH https://www.digitalocean.com/community/tutorials/how-to-create-a-sudo-user-on-ubuntu-quickstart
* SSH https://www.digitalocean.com/community/tutorials/how-to-set-up-ssh-keys-on-ubuntu-1604
* OpenJDK https://askubuntu.com/questions/761127/how-do-i-install-openjdk-7-on-ubuntu-16-04-or-higher
* Mailutils https://www.digitalocean.com/community/tutorials/how-to-install-and-configure-postfix-as-a-send-only-smtp-server-on-ubuntu-16-04
* Tomcat https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-16-04
* Certbot https://www.digitalocean.com/community/tutorials/how-to-secure-apache-with-let-s-encrypt-on-ubuntu-16-04
* Apache2 + mod JK http://www.brandsoftonline.com/installing-tomcat-7-and-apache2-with-mod_jk-on-ubuntu-14-04/
* Tomcat 8 https://www.liquidweb.com/kb/how-to-install-apache-tomcat-7-on-ubuntu-16-04/
* Tomcat 8 https://www.digitalocean.com/community/questions/how-to-access-tomcat-8-admin-gui-from-different-host

# Installation 

## Log onto server
```
ssh <root-username>@server.domain.tld
```

## Create new user
```
sudo adduser <username>
sudo usermod -aG sudo <username>
```

## Copy ssh public key to server
```
ssh-copy-id <username>@server.domain.tld
```

Or alternatively manually copy your public key to the server
```
sudo cat /home/username/.ssh/id_rsa.pub | ssh <username>@server.domain.tld 'cat - >> ~/.ssh/authorized_keys'
```

NOTE: we need to copy the root user's public key because that's the user running Bamboo

## Update apt-get repo
```
sudo apt update
```

## Install various dependencies
```
sudo apt-get install mailutils
sudo apt-get install unzip

```

## Install Java 7
You have a few options here and I would recommend the first one, but follow the instructions carefully.

### Automatic Installation #1 [recommended] 
See option 2 in StackOverflow answer 
https://askubuntu.com/a/803616/292943

### Automatic Installation #2
Easiest, but very insecure [not recommended]
https://askubuntu.com/a/761527/292943

### Manual Installation #1
See option 1 in StackOverflow answer
https://askubuntu.com/a/803616/292943

### Manual Installation #2 
Manually install OpenJDK7 from Azul
https://www.azul.com/downloads/zulu/zulu-linux/


## Check Java Version
Check to make sure that 
```
$ java -version
java version "1.7.0_161"
OpenJDK Runtime Environment (IcedTea 2.6.12) (7u161-2.6.12-1)
OpenJDK 64-Bit Server VM (build 24.161-b01, mixed mode)
```

## MySQL 5.7

### Install MySQL
```
sudo apt-get install mysql-server
```

### Configure MySQL 
Add the following lines to `/etc/alternatives/my.cnf` using your favorite text editor.
```
[mysqld]
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION
```


### External Connections [optional]
If you need to allow external connections to MySQL then you'll need to edit the mysqld configuration.

/etc/mysql/mysql.conf.d/mysqld.cnf

# Instead of skip-networking the default is now to listen only on
# localhost which is more compatible and is not less secure.
#bind-address           = 127.0.0.1
bind-address            = 0.0.0.0

### Restart MySQL
```
sudo service mysql restart
```




## Install Tomcat 7 
https://www.liquidweb.com/kb/how-to-install-apache-tomcat-7-on-ubuntu-16-04/

1. Create tomcat directory
```
mkdir /opt/tomcat
cd /opt/tomcat
```



1. Find download
https://tomcat.apache.org/download-70.cgi

### Copy 'zip' link under Core 

### Download zip file
```
wget http://mirrors.gigenet.com/apache/tomcat/tomcat-7/v7.0.91/bin/apache-tomcat-7.0.91.zip
```

```
unzip apache-tomcat-7.0.91.zip
```

### Create new tomcat user


### Configure tomcat-users.xml [optional]
This configuration can be used for future upgrades. Make sure the file is only readable by root and/or the user that runs Tomcat. 
Replace <manager-gui-\*> and <manager-script-\*> with appropriate values. See Tomcat docs for more information 
(https://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html#Configuring_Manager_Application_Access).
```
<tomcat-users>
  <role rolename="manager-gui"/>
  <role rolename="manager-script"/>
  <role rolename="manager-jmx"/>
  <role rolename="manager-status"/>
  <user username="<gui-username>" password="<gui-password>" roles="manager-gui"/>
  <user username="<script-username>" password="<script-password>" roles="manager-script"/>
</tomcat-users>
```
NOTE: Please don't use obvious passwords (i.e. tomcat, password, s3cret, etc) because your server will get exploited. 



## Install Tomcat 8 
* Tomcat 7 https://www.liquidweb.com/kb/how-to-install-apache-tomcat-7-on-ubuntu-16-04/
* Tomcat 8 https://www.digitalocean.com/community/questions/how-to-access-tomcat-8-admin-gui-from-different-host

sudo groupadd tomcat

### TODO Start Tomcat automatically 

Also note that we're setting memory config in tomcat.service so we no longer need to edit $CATALINA_HOME/bin/setenv.sh

```
sudo vi /etc/systemd/system/tomcat.service
```


tomcat.service
```
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

Environment=JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64/jre
Environment=CATALINA_PID=/opt/tomcat/apache-tomcat-8.5.34/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat/apache-tomcat-8.5.34
Environment=CATALINA_BASE=/opt/tomcat/apache-tomcat-8.5.34
Environment='CATALINA_OPTS=-Xms2048M -Xmx2048M -server -XX:+UseParallelGC'
Environment='JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom'

ExecStart=/opt/tomcat/apache-tomcat-8.5.34/bin/startup.sh
ExecStop=/opt/tomcat/apache-tomcat-8.5.34/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
```


## Deployment

### Create database
```
mysql -u root -p -e 'create database openboxes default charset utf8;'
mysql -u root -p -e 'grant all on openboxes.* to 'openboxes'@'localhost' identified by "openboxes";'
```

### Configuration

#### Create openboxes-config.properties file
```
mkdir ~/.grails
vi ~/.grails/openboxes-config.properties
```

#### Copy the following into ~/.grails/openboxes-config.properties
```
# Database connection settings
dataSource.username=openboxes
dataSource.password=<password>
dataSource.url=jdbc:mysql://localhost:3306/openboxes
```

#### TODO Advanced configuration

* Email 
* Sentry 
* ??


### Add environment variables to ~/.bashrc
```
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export CATALINA_HOME=/opt/tomcat/apache-tomcat-7.0.91
```

### Execute ~/.bashrc to setup environment
```
. ~/.bashrc
```

### Ensure the correct Java version is running
```
sudo update-alternatives --config java
There is 1 choice for the alternative java (providing /usr/bin/java).

  Selection    Path                                     Priority   Status
------------------------------------------------------------
  0            /usr/lib/jvm/java-8-oracle/jre/bin/java   1081      auto mode
* 1            /usr/lib/jvm/java-8-oracle/jre/bin/java   1081      manual mode

Press <enter> to keep the current choice[*], or type selection number: 
```

### Download latest development verison 
```
cd ~
wget http://bamboo.pih-emr.org:8085/browse/OPENBOXES-SDDEV-226/artifact/DJ/Latest-WAR/openboxes.war
```

### Deploy WAR file
```
cp openboxes.war /opt/tomcat/apache-tomcat-7.0.91/webapps
```

### Tail the log file and wait awhile (could take about 10 minutes to create the database)
```
tail -f /opt/tomcat/apache-tomcat-7.0.91/logs/catalina.out
```

### Open Chrome and profit


# Additional Configuration
In order to get rid of the port 8080 and enable HTTPs, a common solution is to run a web server in front of Tomcat. 

## Install Apache 2.2
```
sudo apt-get install apache2
```

NOTE: You can configure Tomcat to listen on port 80/443 and enable HTTPS.


## Configure HTTPS 
The easiest way to do this is to use Let's Encrypt (Certbot). See docs here https://www.digitalocean.com/community/tutorials/how-to-secure-apache-with-let-s-encrypt-on-ubuntu-16-04

### Install Certbot for Apache 
```
sudo add-apt-repository ppa:certbot/certbot
sudo apt-get update
sudo apt-get install python-certbot-apache
```
### Create new HTTPS certificate
Ensure that your domain is accessible before proceeding.
```
sudo certbot --apache -d <subdomain.domain.tld>
```


### Configure automatic update of certificate
```
```

## 

### Configure mod_jk
```
sudo apt-get install libapache2-mod-jk
```

### Enable mod_jk 
```
sudo a2enmod mod_jk
```

### Update workers.properties
```
workers.tomcat_home=/opt/tomcat/apache-tomcat-7.0.91
workers.java_home=/usr/lib/jvm/java-7-openjdk-amd64
```

### Configure Tomcat server.xml
Uncomment the following line in server.xml
```
<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
```

### Configure Apache 
Add the following line to all of your apache configuration in sites-enabled
* /etc/apache2/sites-enabled/000-default.conf
* /etc/apache2/sites-enabled/000-default-le-ssl.conf
```
JkMount /openboxes/* ajp13_worker
JkMount /manager/* ajp13_worker
```

### Restart Apache
### Restart Tomcat





# Troubleshooting

## Unable to add the resource at [/WEB-INF/classes/] to the cache for web application 

### Stacktrace
27-Sep-2018 03:30:09.077 WARNING [localhost-startStop-1] org.apache.catalina.webresources.Cache.getResource Unable to add the resource at [/WEB-INF/classes/] to the cache for web application [/openboxes] because there was insufficient free space available after evicting expired cache entries - consider increasing the maximum size of the cache

### Solution
Increase size of cache in tomcat configuration.


## Errors were encountered while processing ...


### Stacktrace
Errors were encountered while processing:
 fontconfig
 libpango-1.0-0:amd64
 libpangoft2-1.0-0:amd64
 libpangocairo-1.0-0:amd64
 libgtk2.0-0:amd64
 libgtk2.0-bin
 openjdk-7-jre:amd64
 openjdk-7-jdk:amd64
E: Sub-process /usr/bin/dpkg returned an error code (1)


## NullPointerException: Cannot invoke method getAt() on null object

This means that you're running Java 8 instead of Java 7. 

### Stacktrace
```
2018-09-26 15:31:48,575 [localhost-startStop-1] ERROR context.ContextLoader  - Context initialization failed
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'pluginManager' defined in ServletContext resource [/WEB-INF/applicationContext.xml]: Invocation of init method failed; nested exception is java.lang.NullPointerException: Cannot invoke method getAt() on null object
```

### Full logs
```
Sep 26, 2018 3:20:39 PM org.apache.catalina.core.StandardServer await
INFO: A valid shutdown command was received via the shutdown port. Stopping the Server instance.
Sep 26, 2018 3:20:39 PM org.apache.coyote.AbstractProtocol pause
INFO: Pausing ProtocolHandler ["http-bio-8080"]
Sep 26, 2018 3:20:39 PM org.apache.coyote.AbstractProtocol pause
INFO: Pausing ProtocolHandler ["ajp-bio-8009"]
Sep 26, 2018 3:20:39 PM org.apache.catalina.core.StandardService stopInternal
INFO: Stopping service Catalina
Sep 26, 2018 3:20:39 PM org.apache.coyote.AbstractProtocol stop
INFO: Stopping ProtocolHandler ["http-bio-8080"]
Sep 26, 2018 3:20:39 PM org.apache.coyote.AbstractProtocol stop
INFO: Stopping ProtocolHandler ["ajp-bio-8009"]
Sep 26, 2018 3:20:39 PM org.apache.coyote.AbstractProtocol destroy
INFO: Destroying ProtocolHandler ["http-bio-8080"]
Sep 26, 2018 3:20:39 PM org.apache.coyote.AbstractProtocol destroy
INFO: Destroying ProtocolHandler ["ajp-bio-8009"]
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Server version:        Apache Tomcat/7.0.91
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Server built:          Sep 13 2018 19:52:12 UTC
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Server number:         7.0.91.0
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: OS Name:               Linux
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: OS Version:            4.15.0-1023-azure
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Architecture:          amd64
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Java Home:             /usr/lib/jvm/java-8-oracle/jre
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: JVM Version:           1.8.0_181-b13
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: JVM Vendor:            Oracle Corporation
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: CATALINA_BASE:         /opt/tomcat/apache-tomcat-7.0.91
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: CATALINA_HOME:         /opt/tomcat/apache-tomcat-7.0.91
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Command line argument: -Djava.util.logging.config.file=/opt/tomcat/apache-tomcat-7.0.91/conf/logging.properties
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Command line argument: -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Command line argument: -Djdk.tls.ephemeralDHKeySize=2048
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Command line argument: -Dignore.endorsed.dirs=
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Command line argument: -Dcatalina.base=/opt/tomcat/apache-tomcat-7.0.91
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Command line argument: -Dcatalina.home=/opt/tomcat/apache-tomcat-7.0.91
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.VersionLoggerListener log
INFO: Command line argument: -Djava.io.tmpdir=/opt/tomcat/apache-tomcat-7.0.91/temp
Sep 26, 2018 3:31:39 PM org.apache.catalina.core.AprLifecycleListener lifecycleEvent
INFO: The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: /usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
Sep 26, 2018 3:31:39 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["http-bio-8080"]
Sep 26, 2018 3:31:39 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["ajp-bio-8009"]
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.Catalina load
INFO: Initialization processed in 567 ms
Sep 26, 2018 3:31:39 PM org.apache.catalina.core.StandardService startInternal
INFO: Starting service Catalina
Sep 26, 2018 3:31:39 PM org.apache.catalina.core.StandardEngine startInternal
INFO: Starting Servlet Engine: Apache Tomcat/7.0.91
Sep 26, 2018 3:31:39 PM org.apache.catalina.startup.HostConfig deployWAR
INFO: Deploying web application archive /opt/tomcat/apache-tomcat-7.0.91/webapps/openboxes.war
Sep 26, 2018 3:31:44 PM org.apache.catalina.startup.TldConfig execute
INFO: At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
log4j:WARN No appenders could be found for logger (com.opensymphony.clickstream.ClickstreamListener).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
Using configuration locations [classpath:openboxes-config.properties, classpath:openboxes-config.groovy, file:/home/jmiranda/.grails/openboxes-config.properties, file:/home/jmiranda/.grails/openboxes-config.groovy] [production]
Unable to load specified config location classpath:openboxes-config.properties : class path resource [openboxes-config.properties] cannot be opened because it does not exist
Unable to load specified config location classpath:openboxes-config.groovy : class path resource [openboxes-config.groovy] cannot be opened because it does not exist
Unable to load specified config location file:/home/jmiranda/.grails/openboxes-config.groovy : /home/jmiranda/.grails/openboxes-config.groovy (No such file or directory)
grails.mail.enabled: 'true'
Using [:] SMTP appender org.apache.log4j.net.SMTPAppender
2018-09-26 15:31:45,314 [localhost-startStop-1] INFO  context.ContextLoader  - Root WebApplicationContext: initialization started
2018-09-26 15:31:45,334 [localhost-startStop-1] INFO  support.XmlWebApplicationContext  - Refreshing Root WebApplicationContext: startup date [Wed Sep 26 15:31:45 UTC 2018]; root of context hierarchy
2018-09-26 15:31:45,366 [localhost-startStop-1] INFO  xml.XmlBeanDefinitionReader  - Loading XML bean definitions from ServletContext resource [/WEB-INF/applicationContext.xml]
2018-09-26 15:31:45,774 [localhost-startStop-1] INFO  support.DefaultListableBeanFactory  - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@4e04539a: defining beans [grailsApplication,pluginManager,grailsConfigurator,grailsResourceLoader,grailsResourceHolder,characterEncodingFilter]; root of factory hierarchy
2018-09-26 15:31:48,382 [localhost-startStop-1] INFO  cfg.Environment  - Hibernate 3.3.1.GA
2018-09-26 15:31:48,385 [localhost-startStop-1] INFO  cfg.Environment  - hibernate.properties not found
2018-09-26 15:31:48,387 [localhost-startStop-1] INFO  cfg.Environment  - Bytecode provider name : javassist
2018-09-26 15:31:48,389 [localhost-startStop-1] INFO  cfg.Environment  - using JDK 1.4 java.sql.Timestamp handling
2018-09-26 15:31:48,567 [localhost-startStop-1] INFO  support.DefaultListableBeanFactory  - Destroying singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@4e04539a: defining beans [grailsApplication,pluginManager,grailsConfigurator,grailsResourceLoader,grailsResourceHolder,characterEncodingFilter]; root of factory hierarchy
2018-09-26 15:31:48,575 [localhost-startStop-1] ERROR context.ContextLoader  - Context initialization failed
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'pluginManager' defined in ServletContext resource [/WEB-INF/applicationContext.xml]: Invocation of init method failed; nested exception is java.lang.NullPointerException: Cannot invoke method getAt() on null object
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: java.lang.NullPointerException: Cannot invoke method getAt() on null object
	... 5 more
log4j:ERROR Error occured while sending e-mail notification.
javax.mail.MessagingException: Could not connect to SMTP host: localhost, port: 25;
  nested exception is:
	java.net.ConnectException: Connection refused (Connection refused)
	at com.sun.mail.smtp.SMTPTransport.openServer(SMTPTransport.java:1391)
	at com.sun.mail.smtp.SMTPTransport.protocolConnect(SMTPTransport.java:412)
	at javax.mail.Service.connect(Service.java:288)
	at javax.mail.Service.connect(Service.java:169)
	at javax.mail.Service.connect(Service.java:118)
	at javax.mail.Transport.send0(Transport.java:188)
	at javax.mail.Transport.send(Transport.java:118)
	at org.apache.log4j.net.SMTPAppender.sendBuffer(SMTPAppender.java:416)
	at org.apache.log4j.net.SMTPAppender.append(SMTPAppender.java:256)
	at org.apache.log4j.AppenderSkeleton.doAppend(AppenderSkeleton.java:251)
	at org.apache.log4j.helpers.AppenderAttachableImpl.appendLoopOnAppenders(AppenderAttachableImpl.java:66)
	at org.apache.log4j.Category.callAppenders(Category.java:206)
	at org.apache.log4j.Category.forcedLog(Category.java:391)
	at org.apache.log4j.Category.log(Category.java:856)
	at org.slf4j.impl.Log4jLoggerAdapter.log(Log4jLoggerAdapter.java:597)
	at org.apache.commons.logging.impl.SLF4JLocationAwareLog.error(SLF4JLocationAwareLog.java:225)
	at org.springframework.web.context.ContextLoader.initWebApplicationContext(ContextLoader.java:220)
	at org.springframework.web.context.ContextLoaderListener.contextInitialized(ContextLoaderListener.java:47)
	at org.apache.catalina.core.StandardContext.listenerStart(StandardContext.java:5157)
	at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5680)
	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:145)
	at org.apache.catalina.core.ContainerBase.addChildInternal(ContainerBase.java:1018)
	at org.apache.catalina.core.ContainerBase.addChild(ContainerBase.java:994)
	at org.apache.catalina.core.StandardHost.addChild(StandardHost.java:652)
	at org.apache.catalina.startup.HostConfig.deployWAR(HostConfig.java:1127)
	at org.apache.catalina.startup.HostConfig$DeployWar.run(HostConfig.java:2021)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: java.net.ConnectException: Connection refused (Connection refused)
	at java.net.PlainSocketImpl.socketConnect(Native Method)
	at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)
	at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:206)
	at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:188)
	at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
	at java.net.Socket.connect(Socket.java:589)
	at com.sun.mail.util.SocketFetcher.createSocket(SocketFetcher.java:231)
	at com.sun.mail.util.SocketFetcher.getSocket(SocketFetcher.java:189)
	at com.sun.mail.smtp.SMTPTransport.openServer(SMTPTransport.java:1359)
	... 30 more
Sep 26, 2018 3:31:48 PM org.apache.catalina.core.StandardContext startInternal
SEVERE: One or more listeners failed to start. Full details will be found in the appropriate container log file
Sep 26, 2018 3:31:48 PM org.apache.catalina.core.StandardContext startInternal
SEVERE: Context [/openboxes] startup failed due to previous errors
Sep 26, 2018 3:31:48 PM org.apache.catalina.loader.WebappClassLoaderBase clearReferencesThreads
SEVERE: The web application [/openboxes] appears to have started a thread named [AsyncAppender-Dispatcher-Thread-2] but has failed to stop it. This is very likely to create a memory leak.
Sep 26, 2018 3:31:48 PM org.apache.catalina.startup.HostConfig deployWAR
INFO: Deployment of web application archive /opt/tomcat/apache-tomcat-7.0.91/webapps/openboxes.war has finished in 9,229 ms
Sep 26, 2018 3:31:48 PM org.apache.catalina.startup.HostConfig deployDirectory
INFO: Deploying web application directory /opt/tomcat/apache-tomcat-7.0.91/webapps/ROOT
Sep 26, 2018 3:31:48 PM org.apache.catalina.startup.HostConfig deployDirectory
INFO: Deployment of web application directory /opt/tomcat/apache-tomcat-7.0.91/webapps/ROOT has finished in 62 ms
Sep 26, 2018 3:31:48 PM org.apache.catalina.startup.HostConfig deployDirectory
INFO: Deploying web application directory /opt/tomcat/apache-tomcat-7.0.91/webapps/docs
Sep 26, 2018 3:31:48 PM org.apache.catalina.startup.HostConfig deployDirectory
INFO: Deployment of web application directory /opt/tomcat/apache-tomcat-7.0.91/webapps/docs has finished in 33 ms
```


## MessagingException: Could not connect to SMTP host: localhost, port: 25;

### Solution 
* Configure OpenBoxes to use an external SMTP server [recommended]
* Install and configure an SMTP server on localhost [not recommended] 

### Stacktrace
```log4j:ERROR Error occured while sending e-mail notification.
javax.mail.MessagingException: Could not connect to SMTP host: localhost, port: 25;
  nested exception is:
	java.net.ConnectException: Connection refused (Connection refused)

