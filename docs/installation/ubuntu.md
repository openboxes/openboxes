### Install pre-requisites
* Download and install Chrome Browser (currently using `Version 29.0.1547.57`)
* Download and install Java 6+
* Download and install Tomcat 6+
* Download and install MySQL 5.5+
* Download and install an SMTP Server (runs over `localhost:25` by default)

### Download latest release
* Go to the the "latest" release page (https://github.com/openboxes/openboxes/releases/latest)
* Download the WAR file associated with the latest release.

If you wanted to do this from the shell use wget with the following URL to get the latest WAR file.
```
$ wget https://github.com/openboxes/openboxes/releases/download/v0.6.6/openboxes.war
```

### Create database 
```
$ mysql -u root -p -e 'create database openboxes default charset utf8;'
```

### Grant permissions to new new database user
```
mysql -u root -p -e 'grant all on openboxes.* to 'openboxes'@'localhost' identified by "openboxes";'
```
NOTE: For security reasons, you might want to set a different password.  These values should be used in the `dataSource.username` and `dataSource.password` configuration properties in `openboxes-config.properties`.

### Configure application properties
Download the sample external configuration properties file ([openboxes-config.properties](https://github.com/openboxes/openboxes/blob/master/deploy/openboxes-config.properties)) and save it under `$HOME/.grails/openboxes-config.properties` where $HOME is the Tomcat users home directory.

Here's another example of the openboxes-config.properties file:
```
# Database connection settings
dataSource.username=openboxes
dataSource.password=openboxes

# Example of a simple JDBC URL (not to be used in production)
dataSource.url=jdbc:mysql://localhost:3306/openboxes

# Example of a more complex JDBC URL (used in our ccurent production environment)
#dataSource.url=jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB

# Used primarily with g:link when absoluteUrl is true (e.g. links in emails)
grails.serverURL=http://localhost:8080/openboxes

# OpenBoxes mail settings - disabled by default (unless you set up an SMTP server)
grails.mail.enabled=false

# SMTP error appender type
mail.error.appender=dynamic

# Miscellaneous application settings
inventoryBrowser.quickCategories=ARVs,MEDICAL SUPPLIES,FOOD,EQUIPMENT,MEDICINE

# The following property seems to be causing issues, so comment it out to use the system default
#openboxes.loginLocation.requiredActivities = ["MANAGE_INVENTORY"]

# Google Product Search
#google.api.key=<Google API key>

# Hipaaspace.com API (NDC Lookup)
#hipaaspace.api.key=<hipaaspace API key>

# RXNorm API
#rxnorm.api.key=<RxNorm API key>

# Google analytics
#google.analytics.enabled = false
#google.analytics.webPropertyID = <Google Analytics Key>

```
NOTE: Documentation for each available configuration will be provided soon.

### Deploy release to Tomcat
```
$ cp openboxes.war $TOMCAT_HOME/webapps/openboxes.war
```
### Configure Tomcat
You will likely encounter OutOfMemoryErrors with Tomcat's default memory settings.  Therefore, I usually add a file (`TOMCAT_HOME/bin/setenv.sh`) that is invoked by the Tomcat startup script and is used to control the amount of memory allocated to your instance of Tomcat.

A very basic `setenv.sh` will look like this:  
```
export CATALINA_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=256m"
```
You may be able to get away with using 256m as the max heap size, but 512m is a good setting, even for production environments.  Using more memory will allow you to cache more data, but does not always result in a better performing application.  So there's no need in getting carried away.  We've been using about 1024m in production for over a year and that suits us fine.    

### Start Tomcat
```
$ sudo service tomcat6 start
```

### Watch the Tomcat logs during startup
The log file should look something like this:
```
Using Java at: /usr/lib/jvm/java-6-openjdk-amd64
Welcome to Grails 1.3.7 - http://grails.org/
Licensed under Apache Standard License 2.0
Grails home is set to: /usr/share/grails/1.3.7

Base Directory: /home/jmiranda/git/openboxes
Resolving dependencies...
Dependencies resolved in 4485ms.
Running script /usr/share/grails/1.3.7/scripts/RunApp.groovy
Environment set to development
  [groovyc] Compiling 2 source files to /home/jmiranda/git/openboxes/target/classes
classesDirPath: target/classes
Using configuration locations [file:/home/jmiranda/.grails/openboxes-config.groovy, file:/home/jmiranda/.grails/openboxes-config.properties] [production]
/usr/share/grails/1.3.7/lib/groovy-all-1.7.8.jar:/usr/share/grails/1.3.7/dist/grails-bootstrap-1.3.7.jar
Unable to load specified config location file:/home/jmiranda/.grails/openboxes-config.groovy : /home/jmiranda/.grails/openboxes-config.groovy (No such file or directory)
grails.mail.enabled: 'true'
Using dynamic SMTP appender org.pih.warehouse.log4j.net.DynamicSubjectSMTPAppender
DEBUG: setDebug: JavaMail version 1.4.1ea-SNAPSHOT
2013-09-04 15:43:12,504 [main] INFO  cfg.Environment  - Hibernate 3.3.1.GA
2013-09-04 15:43:12,507 [main] INFO  cfg.Environment  - hibernate.properties not found
2013-09-04 15:43:12,510 [main] INFO  cfg.Environment  - Bytecode provider name : javassist
2013-09-04 15:43:12,538 [main] INFO  cfg.Environment  - using JDK 1.4 java.sql.Timestamp handling
     [copy] Copying 1 file to /home/jmiranda/.grails/1.3.7/projects/openboxes
2013-09-04 15:43:14,223 [main] INFO  warehouse._Events  - Setting build date, build number, and revision number ...
2013-09-04 15:43:15,535 [main] INFO  warehouse._Events  - Setting git revision number v0.5.6-276-gbb5a072
   [delete] Deleting directory /home/jmiranda/.grails/1.3.7/projects/openboxes/tomcat
Running Grails application..
2013-09-04 15:43:17,002 [main] INFO  context.ContextLoader  - Root WebApplicationContext: initialization started
2013-09-04 15:43:17,005 [main] INFO  support.XmlWebApplicationContext  - Refreshing Root WebApplicationContext: startup date [Wed Sep 04 15:43:17 EDT 2013]; root of context hierarchy
2013-09-04 15:43:17,036 [main] INFO  xml.XmlBeanDefinitionReader  - Loading XML bean definitions from ServletContext resource [/WEB-INF/applicationContext.xml]
2013-09-04 15:43:17,227 [main] INFO  support.DefaultListableBeanFactory  - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@18be6d81: defining beans [grailsApplication,pluginManager,grailsConfigurator,grailsResourceLoader,grailsResourceHolder,characterEncodingFilter]; root of factory hierarchy
2013-09-04 15:43:25,256 [main] INFO  log.MLog  - MLog clients using log4j logging.
2013-09-04 15:43:27,086 [main] INFO  c3p0.C3P0Registry  - Initializing c3p0-0.9.1.2 [built 21-May-2007 15:04:56; debug? true; trace: 10]
2013-09-04 15:43:27,524 [main] INFO  impl.AbstractPoolBackedDataSource  - Initializing c3p0 pool... com.mchange.v2.c3p0.ComboPooledDataSource [ acquireIncrement -> 5, acquireRetryAttempts -> 30, acquireRetryDelay -> 1000, autoCommitOnClose -> false, automaticTestTable -> null, breakAfterAcquireFailure -> false, checkoutTimeout -> 0, connectionCustomizerClassName -> null, connectionTesterClassName -> com.mchange.v2.c3p0.impl.DefaultConnectionTester, dataSourceName -> z8kfsx8w1t2d3yxhv8a05|27e47715, debugUnreturnedConnectionStackTraces -> false, description -> null, driverClass -> com.mysql.jdbc.Driver, factoryClassLocation -> null, forceIgnoreUnresolvedTransactions -> false, identityToken -> z8kfsx8w1t2d3yxhv8a05|27e47715, idleConnectionTestPeriod -> 7200, initialPoolSize -> 50, jdbcUrl -> jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB, maxAdministrativeTaskTime -> 0, maxConnectionAge -> 14400, maxIdleTime -> 3600, maxIdleTimeExcessConnections -> 300, maxPoolSize -> 100, maxStatements -> 180, maxStatementsPerConnection -> 0, minPoolSize -> 20, numHelperThreads -> 6, numThreadsAwaitingCheckoutDefaultUser -> 0, preferredTestQuery -> null, properties -> {user=******, password=******}, propertyCycle -> 0, testConnectionOnCheckin -> true, testConnectionOnCheckout -> false, unreturnedConnectionTimeout -> 3600, usesTraditionalReflectiveProxies -> false ]
2013-09-04 15:43:28,029 [main] INFO  annotations.Version  - Hibernate Annotations 3.4.0.GA
2013-09-04 15:43:28,065 [main] INFO  common.Version  - Hibernate Commons Annotations 3.1.0.GA
2013-09-04 15:43:28,969 [main] INFO  validator.Version  - Hibernate Validator 3.1.0.GA
2013-09-04 15:43:29,058 [main] INFO  search.HibernateSearchEventListenerRegister  - Unable to find org.hibernate.search.event.FullTextIndexEventListener on the classpath. Hibernate Search is not enabled.
2013-09-04 15:43:29,186 [main] INFO  connection.ConnectionProviderFactory  - Initializing connection provider: org.springframework.orm.hibernate3.LocalDataSourceConnectionProvider
2013-09-04 15:43:29,187 [main] INFO  cfg.SettingsFactory  - RDBMS: MySQL, version: 5.5.32-0ubuntu0.13.04.1-log
2013-09-04 15:43:29,187 [main] INFO  cfg.SettingsFactory  - JDBC driver: MySQL-AB JDBC Driver, version: mysql-connector-java-5.1.5 ( Revision: ${svn.Revision} )
2013-09-04 15:43:29,190 [main] INFO  dialect.Dialect  - Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
2013-09-04 15:43:29,194 [main] INFO  transaction.TransactionFactoryFactory  - Transaction strategy: org.springframework.orm.hibernate3.SpringTransactionFactory
2013-09-04 15:43:29,195 [main] INFO  transaction.TransactionManagerLookupFactory  - No TransactionManagerLookup configured (in JTA environment, use of read-write or transactional second-level cache is not recommended)
2013-09-04 15:43:29,195 [main] INFO  cfg.SettingsFactory  - Automatic flush during beforeCompletion(): disabled
2013-09-04 15:43:29,195 [main] INFO  cfg.SettingsFactory  - Automatic session close at end of transaction: disabled
2013-09-04 15:43:29,195 [main] INFO  cfg.SettingsFactory  - JDBC batch size: 15
2013-09-04 15:43:29,195 [main] INFO  cfg.SettingsFactory  - JDBC batch updates for versioned data: disabled
2013-09-04 15:43:29,195 [main] INFO  cfg.SettingsFactory  - Scrollable result sets: enabled
2013-09-04 15:43:29,196 [main] INFO  cfg.SettingsFactory  - JDBC3 getGeneratedKeys(): enabled
2013-09-04 15:43:29,196 [main] INFO  cfg.SettingsFactory  - Connection release mode: auto
2013-09-04 15:43:29,196 [main] INFO  cfg.SettingsFactory  - Maximum outer join fetch depth: 2
2013-09-04 15:43:29,196 [main] INFO  cfg.SettingsFactory  - Default batch fetch size: 1
2013-09-04 15:43:29,196 [main] INFO  cfg.SettingsFactory  - Generate SQL with comments: disabled
2013-09-04 15:43:29,196 [main] INFO  cfg.SettingsFactory  - Order SQL updates by primary key: disabled
2013-09-04 15:43:29,196 [main] INFO  cfg.SettingsFactory  - Order SQL inserts for batching: disabled
2013-09-04 15:43:29,196 [main] INFO  cfg.SettingsFactory  - Query translator: org.hibernate.hql.ast.ASTQueryTranslatorFactory
2013-09-04 15:43:29,219 [main] INFO  ast.ASTQueryTranslatorFactory  - Using ASTQueryTranslatorFactory
2013-09-04 15:43:29,219 [main] INFO  cfg.SettingsFactory  - Query language substitutions: {}
2013-09-04 15:43:29,219 [main] INFO  cfg.SettingsFactory  - JPA-QL strict compliance: disabled
2013-09-04 15:43:29,219 [main] INFO  cfg.SettingsFactory  - Second-level cache: enabled
2013-09-04 15:43:29,219 [main] INFO  cfg.SettingsFactory  - Query cache: enabled
2013-09-04 15:43:29,221 [main] INFO  cfg.SettingsFactory  - Cache region factory : org.hibernate.cache.impl.bridge.RegionFactoryCacheProviderBridge
2013-09-04 15:43:29,221 [main] INFO  bridge.RegionFactoryCacheProviderBridge  - Cache provider: org.hibernate.cache.EhCacheProvider
2013-09-04 15:43:29,222 [main] INFO  cfg.SettingsFactory  - Optimize cache for minimal puts: disabled
2013-09-04 15:43:29,222 [main] INFO  cfg.SettingsFactory  - Structured second-level cache entries: disabled
2013-09-04 15:43:29,222 [main] INFO  cfg.SettingsFactory  - Query cache factory: org.hibernate.cache.StandardQueryCacheFactory
2013-09-04 15:43:29,225 [main] INFO  cfg.SettingsFactory  - Statistics: enabled
2013-09-04 15:43:29,226 [main] INFO  cfg.SettingsFactory  - Deleted entity synthetic identifier rollback: disabled
2013-09-04 15:43:29,226 [main] INFO  cfg.SettingsFactory  - Default entity-mode: pojo
2013-09-04 15:43:29,226 [main] INFO  cfg.SettingsFactory  - Named query checking : enabled
2013-09-04 15:43:29,300 [main] INFO  impl.SessionFactoryImpl  - building session factory
2013-09-04 15:43:29,533 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.product.Product]; using defaults.
2013-09-04 15:43:29,640 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.shipping.Shipment]; using defaults.
2013-09-04 15:43:29,947 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.core.Document]; using defaults.
2013-09-04 15:43:30,012 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.shipping.ShipmentItem]; using defaults.
2013-09-04 15:43:30,037 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.core.Localization]; using defaults.
2013-09-04 15:43:30,047 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.core.Location]; using defaults.
2013-09-04 15:43:30,141 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.inventory.InventoryItem]; using defaults.
2013-09-04 15:43:30,321 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.inventory.Inventory]; using defaults.
2013-09-04 15:43:30,420 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.product.Category]; using defaults.
2013-09-04 15:43:30,433 [main] WARN  cache.EhCacheProvider  - Could not find configuration [org.pih.warehouse.inventory.InventoryLevel]; using defaults.
2013-09-04 15:43:30,633 [main] INFO  util.NamingHelper  - JNDI InitialContext properties:{}
2013-09-04 15:43:30,637 [main] INFO  cache.UpdateTimestampsCache  - starting update timestamps cache at region: org.hibernate.cache.UpdateTimestampsCache
2013-09-04 15:43:30,638 [main] INFO  cache.StandardQueryCache  - starting query cache at region: org.hibernate.cache.StandardQueryCache
2013-09-04 15:43:33,399 [main] INFO  ehcache.EhCacheManagerFactoryBean  - Initializing EHCache CacheManager
2013-09-04 15:43:33,416 [main] INFO  ehcache.EhCacheManagerFactoryBean  - Initializing EHCache CacheManager
2013-09-04 15:43:36,579 [main] INFO  support.DefaultLifecycleProcessor  - Starting beans in phase 2147483647
Starting Quartz Scheduler in QuartzFactoryBean
2013-09-04 15:43:37,655 [main] INFO  support.DefaultListableBeanFactory  - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@32ff5be1: defining beans [org.grails.plugin.zippedresources.ZipResourceMapper,org.grails.plugin.resource.CSSRewriterResourceMapper,org.grails.plugin.cachedresources.HashAndCacheResourceMapper,org.grails.plugin.resource.BundleResourceMapperInstance,org.grails.plugin.resource.CSSRewriterResourceMapperInstance,org.grails.plugin.resource.BundleResourceMapper,org.grails.plugin.cachedresources.HashAndCacheResourceMapperInstance,org.grails.plugin.zippedresources.ZipResourceMapperInstance,org.grails.plugin.resource.CSSPreprocessorResourceMapperInstance,org.grails.plugin.resource.CSSPreprocessorResourceMapper]; parent: org.codehaus.groovy.grails.commons.spring.ReloadAwareAutowireCapableBeanFactory@ba12d24
2013-09-04 15:43:58,803 [main] INFO  bootstrap.BootStrap  - Running liquibase changelog(s) ...
2013-09-04 15:43:58,888 [main] INFO  bootstrap.BootStrap  - Setting default schema to openboxes
2013-09-04 15:43:58,899 [main] INFO  bootstrap.BootStrap  - Product Version: 5.5.32-0ubuntu0.13.04.1-log
2013-09-04 15:43:58,899 [main] INFO  bootstrap.BootStrap  - Database Version: 5.5
2013-09-04 15:43:58,919 [main] INFO  liquibase  - Reading from `DATABASECHANGELOG`
2013-09-04 15:43:59,186 [main] INFO  liquibase  - Lock Database
2013-09-04 15:43:59,302 [main] INFO  liquibase  - Successfully acquired change log lock
2013-09-04 15:44:00,915 [main] INFO  liquibase  - Release Database Lock
2013-09-04 15:44:00,961 [main] INFO  liquibase  - Successfully released change log lock
2013-09-04 15:44:00,963 [main] INFO  bootstrap.BootStrap  - Finished running liquibase changelog(s)!
2013-09-04 15:44:01,620 [main] INFO  bootstrap.BootStrap  - Default TimeZone set to Eastern Standard Time
2013-09-04 15:44:01,622 [main] INFO  context.ContextLoader  - Root WebApplicationContext: initialization completed in 44620 ms
Server running. Browse to http://localhost:8080/openboxes
```

### Open application in Chrome
[http://localhost:8080/openboxes](http://localhost:8080/openboxes)
