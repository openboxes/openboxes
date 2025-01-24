## Connection Pool Settings

OpenBoxes uses the Tomcat JDBC Pool. 

### Defaults
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
### Properties

We're not going to cover every property available in the Data Source `properties` section. For 
specifics, 

https://tomcat.apache.org/tomcat-9.0-doc/jdbc-pool.html#Common_Attributes
