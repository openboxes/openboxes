/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

beans = {

    customPropertyEditorRegistrar(util.CustomPropertyEditorRegistrar) {
        grailsApplication = ref('grailsApplication')
    }

    /**
     * c3P0 pooled data source that allows 'DB keepalive' queries
     * to prevent stale/closed DB connections
     * Still using the JDBC configuration settings from DataSource.groovy
     * to have easy environment specific setup available
     *
     * https://www.mchange.com/projects/c3p0/index.html
     */
    dataSource(ComboPooledDataSource) { bean ->
        bean.destroyMethod = 'close'
        //use grails' datasource configuration for connection user, password, driver and JDBC url
        user = CH.config.dataSource.username
        password = CH.config.dataSource.password
        driverClass = CH.config.dataSource.driverClassName
        jdbcUrl = CH.config.dataSource.url

        //connection pool settings

        // If this is a number greater than 0, c3p0 will test all idle, pooled but unchecked-out connections, every
        // this number of seconds.
        initialPoolSize = CH.config.dataSource.initialPoolSize

        // Minimum number of Connections a pool will maintain at any given time.
        minPoolSize = CH.config.dataSource.minPoolSize

        // Maximum number of Connections a pool will maintain at any given time.
        maxPoolSize = CH.config.dataSource.maxPoolSize

        // Determines how many connections at a time c3p0 will try to acquire when the pool is exhausted.
        acquireIncrement = CH.config.dataSource.acquireIncrement

        // The size of c3p0's global PreparedStatement cache. If both maxStatements and maxStatementsPerConnection are
        // zero, statement caching will not be enabled. If maxStatements is zero but maxStatementsPerConnection is a
        // non-zero value, statement caching will be enabled, but no global limit will be enforced, only the
        // per-connection maximum. maxStatements controls the total number of Statements cached, for all Connections.
        // If set, it should be a fairly large number, as each pooled Connection requires its own, distinct flock of
        // cached statements. As a guide, consider how many distinct PreparedStatements are used frequently in your
        // application, and multiply that number by maxPoolSize to arrive at an appropriate value. Though
        // maxStatements is the JDBC standard parameter for controlling statement caching, users may find c3p0's
        // alternative maxStatementsPerConnection more intuitive to use.
        maxStatements = CH.config.dataSource.maxStatements

        // The number of PreparedStatements c3p0 will cache for a single pooled Connection. If both maxStatements and
        // maxStatementsPerConnection are zero, statement caching will not be enabled. If maxStatementsPerConnection is
        // zero but maxStatements is a non-zero value, statement caching will be enabled, and a global limit enforced,
        // but otherwise no limit will be set on the number of cached statements for a single Connection. If set,
        // maxStatementsPerConnection should be set to about the number distinct PreparedStatements that are used
        // frequently in your application, plus two or three extra so infrequently statements don't force the more
        // common cached statements to be culled. Though maxStatements is the JDBC standard parameter for controlling
        // statement caching, users may find maxStatementsPerConnection more intuitive to use.
        maxStatementsPerConnection = CH.config.dataSource.maxStatementsPerConnection

        // If set to a value greater than 0, the statement cache will track when Connections are in use, and only
        // destroy Statements when their parent Connections are not otherwise in use. Although closing of a Statement
        // while the parent Connection is in use is formally within spec, some databases and/or JDBC drivers, most
        // notably Oracle, do not handle the case well and freeze, leading to deadlocks. Setting this parameter to a
        // positive value should eliminate the issue. This parameter should only be set if you observe that attempts by
        // c3p0 to close() cached statements freeze (usually you'll see APPARENT DEADLOCKS in your logs). If set, this
        // parameter should almost always be set to 1. Basically, if you need more than one Thread dedicated solely to
        // destroying cached Statements, you should set maxStatements and/or maxStatementsPerConnection so that you
        // don't churn through Statements so quickly.
        statementCacheNumDeferredCloseThreads = CH.config.dataSource.statementCacheNumDeferredCloseThreads

        // If true, an operation will be performed asynchronously at every connection checkin to verify that the
        // connection is valid. Use in combination with idleConnectionTestPeriod for quite reliable, always
        // asynchronous Connection testing. Also, setting an automaticTestTable or preferredTestQuery will usually
        // speed up all connection tests.
        testConnectionOnCheckin = CH.config.dataSource.testConnectionOnCheckin

        // If true, an operation will be performed at every connection checkout to verify that the connection is valid.
        // Be sure to set an efficient preferredTestQuery or automaticTestTable if you set this to true. Performing the
        // (expensive) default Connection test on every client checkout will harm client performance. Testing
        // Connections in checkout is the simplest and most reliable form of Connection testing, but for better
        // performance, consider verifying connections periodically using idleConnectionTestPeriod.
        testConnectionOnCheckout = CH.config.dataSource.testConnectionOnCheckout

        // Defines the query that will be executed for all connection tests, if the default ConnectionTester (or some
        // other implementation of QueryConnectionTester, or better yet FullQueryConnectionTester) is being used.
        // Defining a preferredTestQuery that will execute quickly in your database may dramatically speed up Connection
        // tests. (If no preferredTestQuery is set, the default ConnectionTester executes a getTables() call on the
        // Connection's DatabaseMetaData. Depending on your database, this may execute more slowly than a "normal"
        // database query.) NOTE: The table against which your preferredTestQuery will be run must exist in the database
        // schema prior to your initialization of your DataSource. If your application defines its own schema, try
        // automaticTestTable instead.
        preferredTestQuery = CH.config.dataSource.preferredTestQuery

        // Seconds a Connection can remain pooled but unused before being discarded. Zero means idle connections
        // never expire.
        maxIdleTime = CH.config.dataSource.maxIdleTime

        // If this is a number greater than 0, c3p0 will test all idle, pooled but unchecked-out connections, every
        // this number of seconds.
        idleConnectionTestPeriod = CH.config.dataSource.idleConnectionTestPeriod

        // Number of seconds that Connections in excess of minPoolSize should be permitted to remain idle in the pool
        // before being culled. Intended for applications that wish to aggressively minimize the number of open
        // Connections, shrinking the pool back towards minPoolSize if, following a spike, the load level diminishes
        // and Connections acquired are no longer needed. If maxIdleTime is set, maxIdleTimeExcessConnections should be
        // smaller if the parameter is to have any effect. Zero means no enforcement, excess Connections are not idled out.
        maxIdleTimeExcessConnections = CH.config.dataSource.maxIdleTimeExcessConnections

        // Seconds, effectively a time to live. A Connection older than maxConnectionAge will be destroyed and purged
        // from the pool. This differs from maxIdleTime in that it refers to absolute age. Even a Connection which has
        // not been much idle will be purged from the pool if it exceeds maxConnectionAge. Zero means no maximum
        // absolute age is enforced.
        maxConnectionAge = CH.config.dataSource.maxConnectionAge

        // unreturned connections - use with caution
        // Seconds. If set, if an application checks out but then fails to check-in [i.e. close()] a Connection within
        // the specified period of time, the pool will unceremoniously destroy() the Connection. This permits
        // applications with occasional Connection leaks to survive, rather than eventually exhausting the Connection
        // pool. And that's a shame. Zero means no timeout, applications are expected to close() their own Connections.
        // Obviously, if a non-zero value is set, it should be to a value longer than any Connection should reasonably
        // be checked-out. Otherwise, the pool will occasionally kill Connections in active use, which is bad. This is
        // basically a bad idea, but it's a commonly requested feature. Fix your $%!@% applications so they don't leak
        // Connections! Use this temporarily in combination with debugUnreturnedConnectionStackTraces to figure out
        // where Connections are being checked-out that don't make it back into the pool!
        unreturnedConnectionTimeout = CH.config.dataSource.unreturnedConnectionTimeout

        // If true, and if unreturnedConnectionTimeout is set to a positive value, then the pool will capture the stack
        // trace (via an Exception) of all Connection checkouts, and the stack traces will be printed when unreturned
        // checked-out Connections timeout. This is intended to debug applications with Connection leaks, that is
        // applications that occasionally fail to return Connections, leading to pool growth, and eventually exhaustion
        // (when the pool hits maxPoolSize with all Connections checked-out and lost). This parameter should only be
        // set while debugging, as capturing the stack trace will slow down every Connection check-out.
        debugUnreturnedConnectionStackTraces = CH.config.dataSource.debugUnreturnedConnectionStackTraces

        // The number of milliseconds a client calling getConnection() will wait for a Connection to be checked-in or
        // acquired when the pool is exhausted. Zero means wait indefinitely. Setting any positive value will cause the
        // getConnection() call to time-out and break with an SQLException after the specified number of milliseconds.
        checkoutTimeout = CH.config.dataSource.checkoutTimeout

        // c3p0 is very asynchronous. Slow JDBC operations are generally performed by helper threads that don't hold
        // contended locks. Spreading these operations over multiple threads can significantly improve performance by
        // allowing multiple operations to be performed simultaneously.
        numHelperThreads = CH.config.dataSource.numHelperThreads

        // Seconds before c3p0's thread pool will try to interrupt an apparently hung task. Rarely useful. Many of
        // c3p0's functions are not performed by client threads, but asynchronously by an internal thread pool. c3p0's
        // asynchrony enhances client performance directly, and minimizes the length of time that critical locks are
        // held by ensuring that slow jdbc operations are performed in non-lock-holding threads. If, however, some of
        // these tasks "hang", that is they neither succeed nor fail with an Exception for a prolonged period of time,
        // c3p0's thread pool can become exhausted and administrative tasks backed up. If the tasks are simply slow,
        // the best way to resolve the problem is to increase the number of threads, via numHelperThreads. But if tasks
        // sometimes hang indefinitely, you can use this parameter to force a call to the task thread's interrupt()
        // method if a task exceeds a set time limit. [c3p0 will eventually recover from hung tasks anyway by
        // signalling an "APPARENT DEADLOCK" (you'll see it as a warning in the logs), replacing the thread pool task
        // threads, and interrupt()ing the original threads. But letting the pool go into APPARENT DEADLOCK and then
        // recover means that for some periods, c3p0's performance will be impaired. So if you're seeing these
        // messages, increasing numHelperThreads and setting maxAdministrativeTaskTime might help.
        // maxAdministrativeTaskTime should be large enough that any resonable attempt to acquire a Connection from
        // the database, to test a Connection, or to destroy a Connection, would be expected to succeed or fail within
        // the time set. Zero (the default) means tasks are never interrupted, which is the best and safest policy
        // under most circumstances. If tasks are just slow, allocate more threads. If tasks are hanging forever,
        // try to figure out why, and maybe setting maxAdministrativeTaskTime can help in the meantime.
        maxAdministrativeTaskTime = CH.config.dataSource.maxAdministrativeTaskTime

        // Must be one of caller, library, or none. Determines how the contextClassLoader (see java.lang.Thread) of
        // c3p0-spawned Threads is determined. If caller, c3p0-spawned Threads (helper threads, java.util.Timer
        // threads) inherit their contextClassLoader from the client Thread that provokes initialization of the pool.
        // If library, the contextClassLoader will be the class that loaded c3p0 classes. If none, no
        // contextClassLoader will be set (the property will be null), which in practice means the system ClassLoader
        // will be used. The default setting of caller is sometimes a problem when client applications will be hot
        // redeployed by an app-server. When c3p0's Threads hold a reference to a contextClassLoader from the first
        // client that hits them, it may be impossible to garbage collect a ClassLoader associated with that client
        // when it is undeployed in a running VM. Setting this to library can resolve these issues. [See "Configuring
        // To Avoid Memory Leaks On Hot Redeploy Of Client"]
        contextClassLoaderSource = CH.config.dataSource.contextClassLoaderSource

        // If true, c3p0-spawned Threads will have the java.security.AccessControlContext associated with c3p0 library
        // classes. By default, c3p0-spawned Threads (helper threads, java.util.Timer threads) inherit their
        // AccessControlContext from the client Thread that provokes initialization of the pool. This can sometimes be
        // a problem, especially in application servers that support hot redeployment of client apps. If c3p0's Threads
        // hold a reference to an AccessControlContext from the first client that hits them, it may be impossible to
        // garbage collect a ClassLoader associated with that client when it is undeployed in a running VM. Also, it is
        // possible client Threads might lack sufficient permission to perform operations that c3p0 requires. Setting
        // this to true can resolve these issues. [See "Configuring To Avoid Memory Leaks On Hot Redeploy Of Client"]
        privilegeSpawnedThreads = CH.config.dataSource.privilegeSpawnedThreads


    }
}


