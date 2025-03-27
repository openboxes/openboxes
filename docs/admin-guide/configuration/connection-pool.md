
OpenBoxes relies on a connection pool to manage database connections efficiently, reducing 
overhead from frequent connection creation. The previous section ([Data Source](database.md)) 
covers the bare minimum around database connections. This section includes all of the properties 
that need to be configured to tune your database connection pool to allow optimal performance.

!!! caution 
    Please use extreme caution when modifying connection pool settings, as improper configurations 
    can lead to performance issues, connection exhaustion, and database instability. Always test 
    changes in a staging environment before applying them to production. Ensure that settings 
    align with your database capacity and application workload. Misconfigured pools may cause 
    slow queries, timeouts, or excessive resource usage. If unsure, consult database and server 
    logs to fine-tune settings or seek expert guidance from our 
    [community](https://community.openboxes.com).


## Configuration
The default configuration uses Tomcat JDBC Connection Pool, along with other default settings 
defined in application.yml.


```shell
dataSource:
    pooled: true
    jmxExport: true
    driverClassName: com.mysql.cj.jdbc.Driver
    dbCreate: none
    dialect: org.hibernate.dialect.MySQL57InnoDBDialect
    factory: org.apache.tomcat.jdbc.pool.DataSourceFactory
    type: org.apache.tomcat.jdbc.pool.DataSource

    # Settings for database logging
    logger: com.mysql.cj.jdbc.log.StandardLogger
    dumpQueriesOnException: false
    logSql: false  # Logs SQL queries to the console. We configure this via Logback instead.
    formatSql: false
    logSlowQueries: false
    includeInnodbStatusInDeadlockExceptions: true

    properties:
        # from https://docs.grails.org/3.3.16/guide/conf.html#dataSource
        jmxEnabled: true
        initialSize: 5
        maxActive: 50
        minIdle: 5
        maxIdle: 25
        maxWait: 10000
        maxAge: 10 * 60000
        timeBetweenEvictionRunsMillis: 5000
        minEvictableIdleTimeMillis: 60000
        validationQuery: SELECT 1
        validationQueryTimeout: 3
        validationInterval: 15000
        testOnBorrow: false
        testWhileIdle: true
        testOnReturn: false
        # https://tomcat.apache.org/tomcat-8.5-doc/jdbc-pool.html#JDBC_interceptors
        jdbcInterceptors: "ConnectionState;StatementCache(max=200)"
        defaultTransactionIsolation: java.sql.Connection.TRANSACTION_READ_COMMITTED
```

## Customization

We're not going to cover every property available for override in the `dataSource` section, so 
please refer to the following documentation for more information. 
* [Grails dataSource](https://docs.grails.org/3.3.16/guide/conf.html#dataSource) 
* [Tomcat JDBC Pool](https://tomcat.apache.org/tomcat-9.0-doc/jdbc-pool.html#Common_Attributes) 

A few of the critical properties include:

* maxActive (maximum connections, e.g., 50)
* maxIdle (max idle connections, e.g., 25)
* minIdle (minimum idle connections, e.g., 5)
* maxWait (timeout for acquiring a connection, e.g., 10,000ms)

For high-traffic environments, tuning these values based on workload and database capacity ensures 
optimal performance and stability. Enabling`testOnBorrow` and setting `validationQuery (SELECT 1)` 
can help detect stale connections.


## Optimization
There are resources (including the two links below) on the web related to performance tuning connection 
pools. I would highly recommend that you read through the science behind some of the 
recommendations before you tune your connection pool settings. It may seem counterintuitive at times 
(i.e. less is more) so don't get caught up in always increasing resources if/when you encounter 
performance issues.

* [Why does HikariCP recommend fixed size pool for better performance](https://stackoverflow.com/questions/28987540/why-does-hikaricp-recommend-fixed-size-pool-for-better-performance)
* [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
