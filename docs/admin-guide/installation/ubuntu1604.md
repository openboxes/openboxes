# Installing OpenBoxes on Ubuntu 16.04

## 1. Check Requirements

### System Requirements
* Internet connection (recommended)
* 2GB RAM (minimum)
* 25GB disk storage (minimum)

### Software Requirements
* Ubuntu 16.04 LTS 
* Java 7 
* Tomcat 7
* MySQL 5.7+ 
* SMTP service

## 2. Choose where to install 
Whether you plan to install OpenBoxes on-premise or in the cloud, the installation instructions should be the same. 
However, getting to a place 

### Cloud
Here are a few options for cheapish cloud hosting providers.


| Hosting Provider | Instance Type | Monthly Cost |
| ----- | ------ | --- |
| [Digital Ocean droplet](https://www.digitalocean.com/pricing/) | Droplet 2GB | $10 |
| [Amazon Web Services EC2](http://www.ec2instances.info/) | t2.small 2GB | $20 |
| [Google Compute Engine](https://cloud.google.com/compute/pricing) | g1-small 1.7GB | $20 |
| [RimuHosting](https://rimuhosting.com/order/v2orderstart.jsp) | Customizable VPS | $20 |
| [Linode](https://www.linode.com/pricing) | Linode 2GB | $10 |

NOTE: AWS has a free-tier that includes a free year of 750 hours per month for t2.micro EC2 instances (as well as other 
services). It's a great deal it if you're not going to be using OpenBoxes too heavily. Unfortunately, keeping a 
Java-based web application like OpenBoxes happy on a t2.micro (1GB of RAM) is not easy. You may need to reduce the heap 
size and perm generation memory allocated to Tomcat to something minimal (see step 5 for more details).

### On-Premise
Installing OpenBoxes on-premise requires a bit of work to install the appropriate Ubuntu version on the rack-mounted 
server, desktop, or laptop that you've designated as your server. Our installation docs will not describe how to install 
Ubuntu 16.04 Desktop or Server, so you'll need to consult Ubuntu docs. Here are a few tutorials that might be helpful.

* [How to install Ubuntu 16.04 Desktop](https://tutorials.ubuntu.com/tutorial/tutorial-install-ubuntu-desktop-1604#0)
* [How to install Ubuntu 16.04 Server](https://tutorials.ubuntu.com/tutorial/tutorial-install-ubuntu-server-1604#0)

## 3. Install dependencies

### 3.1 Update package information
```
sudo apt-get update
```

### 3.2 Upgrade packages already installed
```
sudo apt-get upgrade
```

### 3.3 Install Java 7
You must install a Java 7 JRE/JDK. Unfortunately, the APT Repository on Ubuntu 16.04 does not include a version 
of the Java 7 JRE or JDK so we'll need to do some work to get this working. I would personally recommend 
**Option 2** below.


!!! note
    For the time being, you **MUST** use Java 7! The version of Grails that we're using does not support Java 8+ or 
    beyond. We are working on upgrading to the latest version of Grails, but we're still several months away from 
    completing that migration. 

!!! important
    In case it wasn't clear from the box above this means you should NOT attempt to install the `default-jre`, 
    `openjdk-8-jre`, or `openjdk-9-jre` packages from the APT repository. 
    
    Grails 1.3.9 does not support Java 8+ so Tomcat will fail to deploy OpenBoxes. 
    When it fails, you will send an email to 
    [support@openboxes.com](mailto:support@openboxes.com) asking why the installation failed. When we receive your email, 
    we will point you back to the little blue box that you ignored. Don't be that person.

#### Choose your choice
See the following [StackExchange question](https://askubuntu.com/questions/761127/how-do-i-install-openjdk-7-on-ubuntu-16-04-or-higher) for more details 

* [Option 1: Manual Installation](https://askubuntu.com/a/803616) **(not recommended)** You can certainly use this solution, but I had some trouble getting it to work so I would not recommend it. See 

* [Option 2: Automatic Installation](https://askubuntu.com/a/803616) **(recommended)** 
    I would recommend this solution. It's a little more complex but seems to behave as you'd expect when installing 
from the APT repository.

* [Option 3: Oracle Java](https://askubuntu.com/a/761527) **(not not recommended)**[^1]
This would probably be a recommended option, but Oracle ended public support for JDK 7 along time ago.

* [Option 4: Install from Zulu Linux](https://askubuntu.com/a/840945) **(not not recommended)**[^1]
This seems like an ok option if you're comfortable manually install Debian packages - it's pretty straightforward.

* [Option 5: Install from an unsupported package maintainer](https://askubuntu.com/a/761527) **(not recommended)**
This is probably the easiest solution, but also the least secure. Your friends will call you names.

* [Option 6: Use docker](https://askubuntu.com/a/1059859) **(recommended)** I would recommend using docker, but unfortunately do not have any instructions to share at this time.

!!! danger
    Do not use Option 5 in production. Do not use this solution unless you are in a rush and either plan to do it 
    properly when you have time or you plan to throw away this server after evaluating OpenBoxes. 

[^1]: You read that correctly. I wrote "not not recommended", which is a double negative meaning 
"we're not recommending it, but we're also not not recommending it."

#### Configure Java version 
```
$ sudo update-alternatives --config java
There is only one alternative in link group java (providing /usr/bin/java): /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java
Nothing to configure.
```

#### Check Java version
Make sure you see something like `java version 1.7.0_xyz`.
```
$ java -version
java version "1.7.0_161"
OpenJDK Runtime Environment (IcedTea 2.6.12) (7u161-2.6.12-1)
OpenJDK 64-Bit Server VM (build 24.161-b01, mixed mode)
```


---
### 3.4 Tomcat 

#### Install Tomcat 7
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
    
#### References
* [https://whiscardz.wordpress.com/tag/java7-tomcat-7-on-ubuntu-16-04/](https://whiscardz.wordpress.com/tag/java7-tomcat-7-on-ubuntu-16-04/)
* [https://www.techrepublic.com/article/how-to-install-apache-tomcat-on-ubuntu-server-16-04/](https://www.techrepublic.com/article/how-to-install-apache-tomcat-on-ubuntu-server-16-04/)


#### Getting Started

* Go to the [Tomcat Downloads](https://tomcat.apache.org/download-70.cgi) page
* Choose a mirror (or leave the default)
* Right-click on the Core tar.gz link (paste in step 3 below)

#### Download and unpack
```
$ sudo mkdir /opt/tomcat
$ cd /opt/tomcat
$ sudo wget http://mirror.metrocast.net/apache/tomcat/tomcat-7/v7.0.91/bin/apache-tomcat-7.0.91.tar.gz
$ sudo tar xvzf apache-tomcat-7.0.91.tar.gz
```

#### Create user, group and change permissions
```
$ sudo groupadd tomcat
$ sudo useradd -s /bin/false -g tomcat -d /opt/tomcat tomcat 
$ cd apache-tomcat-7.0.91
$ sudo chgrp -R tomcat /opt/tomcat
$ sudo chown -R tomcat:tomcat /opt/tomcat
$ sudo chown -R tomcat webapps/ work/ temp/ logs/ conf/
```

#### Create service
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
    
    You may be able to get away with using 256m as the max heap size, but 512m is a good setting, even for production environments.  Using more memory will allow you to cache more data, but does not always result in a better performing application.  So there's no need in getting carried away.  We've been using about 1024m in production for over a year and that suits us fine.    

If you are in a limited memory environment (like an EC2 t2.micro which only has 1GB of memory) you will need to reduce 
your memory settings a little more. 
```
Environment='CATALINA_OPTS=-Xms128m -Xmx256m -XX:MaxPermSize=128m -Djava.security.egd=file:/dev/./urandom -server -XX:+UseParallelGC'
```

Unfortunately, with so little memory allocated you will probably run into several types of OutOfMemoryError issues 
(see Troublshooting section below).



#### Register service
```
sudo systemctl daemon-reload
```

#### Start Tomcat
```
systemctl start tomcat
```

#### Systemctl commands
```
systemctl start tomcat
systemctl stop tomcat
systemctl status tomcat
```

#### Service wrapper
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

#### Configure Tomcat manager **(optional)**
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
  <user username="<GUI-USERNAME>" password="<GUI-PASSWORD>" roles="manager-gui"/>
  <user username="<SCRIPT-USERNAME>" password="<SCRIPT-PASSWORD>" roles="manager-script"/>
</tomcat-users>
```
NOTE: Please don't use obvious passwords (i.e. tomcat, password, s3cret, etc) because your server will get exploited. 


 
---
### 3.5 MySQL 

#### Install MySQL Server
Install MySQL from the APT repository. Finally, something easy!
```
sudo apt-get install mysql-server
```

!!! note "Important"
    Remember the password you enter for the `root` user.

#### Configure MySQL Server

##### Allow external connections [optional]
If you need to allow external connections to MySQL then you'll need to edit the bind-address mysqld configuration. 
This is a security risk so please ensure that you set passwords and grant permissions in a way that does not leave
you vulnerable to attacks.

**/etc/mysql/mysql.conf.d/mysqld.cnf**
```
# Instead of skip-networking the default is now to listen only on
# localhost which is more compatible and is not less secure.
#bind-address           = 127.0.0.1
bind-address            = 0.0.0.0
```

##### This is incompatible with sql_mode=only_full_group_by [recommended]

!!! note "Important"
    You may encounter an error when using the default mysql-server package (MySQL 5.7). The error (below) requires
    a configuration change within MySQL. 

```
Expression #1 of SELECT list is not in GROUP BY clause and contains nonaggregated column 
'openboxes.tag.id' which is not functionally dependent on columns in GROUP BY clause; this 
is incompatible with sql_mode=only_full_group_by
```

This error is caused by the fact that ONLY_FULL_GROUP_BY is enabled in MySQL 5.7 by default. 
```
mysql> show variables like '%sql_mode%';
+---------------+-------------------------------------------------------------------------------------------------------------------------------------------+
| Variable_name | Value                                                                                                                                     |
+---------------+-------------------------------------------------------------------------------------------------------------------------------------------+
| sql_mode      | ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION |
+---------------+-------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.01 sec)
```

In order to this avoid error, please copy the `sql_mode` value above, remove the `ONLY_FULL_GROUP_BY` option and add 
it as a new line under the `[mysqld]` section within `/etc/alternatives/my.cnf`.
```
[mysqld]
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION
```

!!! note 
    Eventually we plan to fix the bug, but for now the config change is recommended.


#### Restart MySQL
```
sudo service mysql restart
```

#### Create database
```
$ mysql -u root -p -e 'create database openboxes default charset utf8;'
```

#### Grant permissions to new new database user
```
mysql -u root -p -e 'grant all on openboxes.* to '<username>'@'localhost' identified by "<password>";'
```
!!! note
    For security reasons, you will want to set a good password.  These values should be used in the 
    `dataSource.username` and `dataSource.password` configuration properties in `openboxes-config.properties`.


---
### 3.6 Configure Environment

Determine the path to Java 
```
$ sudo update-java-alternatives --list
java-1.7.0-openjdk-amd64       1071       /usr/lib/jvm/java-1.7.0-openjdk-amd64
``` 

Add environment variables to `~/.bashrc`

```
export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64
export CATALINA_HOME=/opt/tomcat/apache-tomcat-7.0.91
```

Refresh environment
```
. ~/.bashrc
```

---
### 3.7 Configure application 


#### Create openboxes-config.properties file
```
cd /opt/tomcat
sudo mkdir /opt/tomcat/.grails
sudo vi /opt/tomcat/.grails/openboxes-config.properties
```

#### Copy the following contents into opneboxes-config.properties
**/opt/tomcat/.grails/openboxes-config.properties**
```
# Database connection settings
dataSource.username=<username>
dataSource.password=<password>

# Example of a simple JDBC URL (not to be used in production)
dataSource.url=jdbc:mysql://localhost:3306/openboxes

# Example of a more complex JDBC URL (used in our ccurent production environment)
#dataSource.url=jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB

# Used primarily with g:link when absoluteUrl is true (e.g. links in emails)
grails.serverURL=http://localhost:8080/openboxes

# OpenBoxes mail settings - disabled by default (unless you set up an SMTP server)
#grails.mail.enabled=true
```

!!! note "Reminder" 
    Change `dataSource.username` and `dataSource.password` to the `username` and `password` you set in the `grant all` command above.

!!! note "Reminder" 
    Change `grails.serverURL` to the IP address or domain name you plan to use for your server.


!!! note 
    Documentation for all available configuration properties is provided in the Configuration section.


---
### 3.8 Deployment


#### Stop tomcat
```
$ sudo service tomcat stop
```

#### Download latest release

1. Go to the the [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub.
1. Right-click on `openboxes.war` 
1. Select **Copy link address**
1. Paste the link address in the following command

```
$ sudo wget https://github.com/openboxes/openboxes/releases/download/<version>/openboxes.war
```

#### Copy WAR file to webapps
```
$ sudo cp openboxes.war /opt/tomcat/apache-tomcat-7.0.91/webapps/openboxes.war
```

#### Change file ownership
```
$ sudo chown tomcat:tomcat /opt/tomcat/apache-tomcat-7.0.91/webapps/openboxes.war
```

#### Restart Tomcat
```
$ sudo service tomcat start
```

#### Watch Tomcat logs
The deployment will take about 10-20 minutes the first time because the application needs to perform hundreds of
database migrations. Keep an eye out for any errors/exceptions that pop up in the `catalina.out` log file and check 
the Troubleshooting section for details on how to handle these issues.
```
$ sudo tail -f /opt/tomcat/apache-tomcat-7.0.91/logs/catalina.out
```
---
### 3.9 Troubleshooting

#### Unable to load specified config location 
You can ignore these errors because these files are only used to override the default `openboxes-config.properties`.
```
Using configuration locations [classpath:openboxes-config.properties, classpath:openboxes-config.groovy, file:/opt/tomcat/.grails/openboxes-config.properties, file:/opt/tomcat/.grails/openboxes-config.groovy] [production]
Unable to load specified config location classpath:openboxes-config.properties : class path resource [openboxes-config.properties] cannot be opened because it does not exist
Unable to load specified config location classpath:openboxes-config.groovy : class path resource [openboxes-config.groovy] cannot be opened because it does not exist
Unable to load specified config location file:/opt/tomcat/.grails/openboxes-config.groovy : /opt/tomcat/.grails/openboxes-config.groovy (No such file or directory)
```

However, if the log shows that the following file could not be found, then we might have a problem. Check that the 
file exists and that the ownership and permissions on this file allow the `tomcat` user to read it.
```
Unable to load specified config location file:/opt/tomcat/.grails/openboxes-config.properties : /opt/tomcat/.grails/openboxes-config.properties (No such file or directory)
```

#### Java OutOfMemoryError
The following errors are related to the `-Xms` (min heap), `-Xmx` (max heap) , and `-XX:MaxPermSize=256m` 
(max perm gen space) memory settings. These errors indicate that the heap / permgen memory spaces are not allocated 
appropriately and/or there's a memory leak in the application. 

* Heap space (`OutOfMemoryError: Java heap space`)
* PermGen (`OutOfMemoryError: PermGen space`)

See [this article] (https://plumbr.eu/outofmemoryerror/java-heap-space) for a good description of the problem. 
Contact [support@openboxes.com](mailto:support@openboxes.com) if you have further questions.

#### Out of Memory: Killed process 31088 (java)
In this case, the Linux kernel has killed your Tomcat instance because it over stepped the OS bounds on memory. At 
this point, you may have increased the max heap size as much as you can. This probably means you need to upgrade to a 
larger instance type (i.e. as we mentioned above, an instance type that has 2GB of memory is a good start).


#### java.io.FileNotFoundException: stacktrace.log (Permission denied)
You can safely ignore this error. 
```
log4j:ERROR setFile(null,true) call failed.
java.io.FileNotFoundException: stacktrace.log (Permission denied)
	at java.io.FileOutputStream.open(Native Method)
	at java.io.FileOutputStream.<init>(FileOutputStream.java:221)
	at java.io.FileOutputStream.<init>(FileOutputStream.java:142)
	at org.apache.log4j.FileAppender.setFile(FileAppender.java:294)
	at org.apache.log4j.FileAppender.activateOptions(FileAppender.java:165)
	at org.apache.log4j.spi.OptionHandler$activateOptions.call(Unknown Source)
	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:40)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:116)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:120)
	at org.codehaus.groovy.grails.plugins.logging.Log4jConfig.createFullstackTraceAppender(Log4jConfig.groovy:177)
	at org.codehaus.groovy.grails.plugins.logging.Log4jConfig.this$2$createFullstackTraceAppender(Log4jConfig.groovy)
	at org.codehaus.groovy.grails.plugins.logging.Log4jConfig$this$2$createFullstackTraceAppender.callCurrent(Unknown Source)
	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:44)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:141)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:145)
	at org.codehaus.groovy.grails.plugins.logging.Log4jConfig.configure(Log4jConfig.groovy:145)
	at org.codehaus.groovy.grails.web.util.Log4jConfigListener.contextInitialized(Log4jConfigListener.java:62)
	at org.apache.catalina.core.StandardContext.listenerStart(StandardContext.java:5157)
	at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5680)
	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:145)
	at org.apache.catalina.core.ContainerBase.addChildInternal(ContainerBase.java:1018)
	at org.apache.catalina.core.ContainerBase.addChild(ContainerBase.java:994)
	at org.apache.catalina.core.StandardHost.addChild(StandardHost.java:652)
	at org.apache.catalina.startup.HostConfig.deployWAR(HostConfig.java:1127)
	at org.apache.catalina.startup.HostConfig$DeployWar.run(HostConfig.java:2021)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:473)
	at java.util.concurrent.FutureTask.run(FutureTask.java:262)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1152)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:622)
	at java.lang.Thread.run(Thread.java:748)
```

#### Could not connect to SMTP host: localhost, port: 25;
By default, the system assumes that there's an SMTP server listening on port 25 so that it can send exceptions to
`errors@openboxes.com`. This should probably be disabled by default. For now you can just ignore these errors.
```
javax.mail.MessagingException: Could not connect to SMTP host: localhost, port: 25;
  nested exception is:
	java.net.ConnectException: Connection refused (Connection refused)
```

#### MySQLSyntaxErrorException: Unknown column 'this_.created_by_id' in 'field list'

This is related to a bug / limitation with the Quartz scheduler. By default we can only limit the Quartz 
scheduler from starting tasks for up to X seconds. We cannot set dependencies such as 
"when the database has been created". Because of this limitation, the Quartz scheduler triggers all jobs during 
the application bootstrapping and before the database migrations have been completed. Therefore, some Quartz jobs
which depend on the database are executed before the database is ready. Hence the "Unknown column" errors.

Related issues:
* https://github.com/openboxes/openboxes/issues/26
* https://github.com/openboxes/openboxes/issues/259

```
Caused by: com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Unknown column 'this_.created_by_id' in 'field list'
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:57)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:526)
	at com.mysql.jdbc.Util.handleNewInstance(Util.java:411)
	at com.mysql.jdbc.Util.getInstance(Util.java:386)
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:1054)
	at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:4190)
	at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:4122)
	at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:2570)
	at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2731)
	at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2818)
	at com.mysql.jdbc.PreparedStatement.executeInternal(PreparedStatement.java:2157)
	at com.mysql.jdbc.PreparedStatement.executeQuery(PreparedStatement.java:2324)
	at com.mchange.v2.c3p0.impl.NewProxyPreparedStatement.executeQuery(NewProxyPreparedStatement.java:76)
	at org.hibernate.jdbc.AbstractBatcher.getResultSet(AbstractBatcher.java:208)
	at org.hibernate.loader.Loader.getResultSet(Loader.java:1808)
	at org.hibernate.loader.Loader.doQuery(Loader.java:697)
	at org.hibernate.loader.Loader.doQueryAndInitializeNonLazyCollections(Loader.java:259)
	at org.hibernate.loader.Loader.doList(Loader.java:2228)
	... 75 more
```

Once the database migration process has completed you should stop seeing these errors and the logs will 
show that the deployment has completed successfully.

```
...
2018-11-16 18:11:25,783 [localhost-startStop-1] INFO  liquibase  - Release Database Lock
2018-11-16 18:11:25,784 [localhost-startStop-1] INFO  liquibase  - Successfully released change log lock
2018-11-16 18:11:26,583 [localhost-startStop-1] INFO  bootstrap.BootStrap  - Finished running liquibase changelog(s)!
2018-11-16 18:11:26,584 [localhost-startStop-1] INFO  bootstrap.BootStrap  - Insert test fixtures?  true
2018-11-16 18:11:26,585 [localhost-startStop-1] INFO  bootstrap.BootStrap  - Inserting test fixtures ...
2018-11-16 18:11:26,598 [localhost-startStop-1] INFO  bootstrap.BootStrap  - Creating uploads directory if it doesn't already exist
2018-11-16 18:11:26,598 [localhost-startStop-1] INFO  context.ContextLoader  - Root WebApplicationContext: initialization completed in 523311 ms
Nov 16, 2018 6:11:26 PM org.apache.catalina.startup.HostConfig deployWAR
INFO: Deployment of web application archive /opt/tomcat/apache-tomcat-7.0.91/webapps/openboxes.war has finished in 545,140 ms
Nov 16, 2018 6:11:27 PM org.apache.coyote.AbstractProtocol start
INFO: Starting ProtocolHandler ["http-bio-8080"]
Nov 16, 2018 6:11:27 PM org.apache.coyote.AbstractProtocol start
INFO: Starting ProtocolHandler ["ajp-bio-8009"]
Nov 16, 2018 6:11:27 PM org.apache.catalina.startup.Catalina start
INFO: Server startup in 546085 ms
```