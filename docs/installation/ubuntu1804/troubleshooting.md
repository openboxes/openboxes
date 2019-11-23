## Using a version of Tomcat compiled with Java 8 (or only have Java 8 installed)
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

Please see the instructions for installing Java and Tomcat.

## Unable to load specified config location 
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

## Java OutOfMemoryError
The following errors are related to the `-Xms` (min heap), `-Xmx` (max heap) , and `-XX:MaxPermSize=256m` 
(max perm gen space) memory settings. These errors indicate that the heap / permgen memory spaces are not allocated 
appropriately and/or there's a memory leak in the application. 

* Heap space (`OutOfMemoryError: Java heap space`)
* PermGen (`OutOfMemoryError: PermGen space`)

See [this article] (https://plumbr.eu/outofmemoryerror/java-heap-space) for a good description of the problem. 
Contact [support@openboxes.com](mailto:support@openboxes.com) if you have further questions.

## Out of Memory: Killed process 31088 (java)
In this case, the Linux kernel has killed your Tomcat instance because it over stepped the OS bounds on memory. At 
this point, you may have increased the max heap size as much as you can. This probably means you need to upgrade to a 
larger instance type (i.e. as we mentioned above, an instance type that has 2GB of memory is a good start).


## java.io.FileNotFoundException: stacktrace.log (Permission denied)
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

## Could not connect to SMTP host: localhost, port: 25;
By default, the system assumes that there's an SMTP server listening on port 25 so that it can send exceptions to
`errors@openboxes.com`. This should probably be disabled by default. For now you can just ignore these errors.
```
javax.mail.MessagingException: Could not connect to SMTP host: localhost, port: 25;
  nested exception is:
	java.net.ConnectException: Connection refused (Connection refused)
```

## MySQLSyntaxErrorException: Unknown column 'this_.created_by_id' in 'field list'

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

## Uploads directory does not exist

### Error
```
Unable to upload file due to exception: uploads/inventory (32).xls (No such file or directory)
```
![Uploads directory does not exist](/img/import-data-uploads-directory-does-not-exist.png)

### Solution
Create `uploads` directory under Tomcat root directory (`/opt/tomcat`). 

```
sudo mkdir /opt/tomcat/uploads
sudo chown tomcat:tomcat /opt/tomcat/uploads
```

There are times where this may not resolve the issue (particularly when you install Tomcat from scatch like we've done
here in these instructions). We're still looking into this bug, but in case you get the same error even after creating the 
`uploads` directory under Tomcat, consider creating it under the root (/) directory.
```
sudo mkdir /uploads
sudo chown tomcat:tomcat /uploads
```
