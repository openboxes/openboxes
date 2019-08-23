/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
// General data source properties
dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
    dialect = org.hibernate.dialect.MySQL5InnoDBDialect
    username = "root"
    password = "root"
    loggingSql = false
    format_sql = false
    use_sql_comments = false

    // Basic Pool Configuration
    acquireIncrement = 5
    initialPoolSize = 10
    minPoolSize = 5
    maxPoolSize = 100

    // Statement Pooling
    maxStatements = 180
    maxStatementsPerConnection = 0
    statementCacheNumDeferredCloseThreads = 1

    // Connection Testing
    testConnectionOnCheckin = false
    testConnectionOnCheckout = false
    preferredTestQuery = "SELECT 1"
    idleConnectionTestPeriod = 7200

    // Pool Size and Connection Age
    maxIdleTime = 0
    maxConnectionAge = 14400
    maxIdleTimeExcessConnections = 1800

    // Unreturned Connections
    unreturnedConnectionTimeout = 0
    debugUnreturnedConnectionStackTraces = false

    // Recovery from Database Outages
    acquireRetryAttempts = 30
    acquireRetryDelay = 1000
    breakAfterAcquireFailure = false

    // Other Configuration
    checkoutTimeout = 0
    numHelperThreads = 3
    maxAdministrativeTaskTime = 0
    privilegeSpawnedThreads = false
    contextClassLoaderSource = "caller"
}

// Hibernate caching properties
hibernate {
    generate_statistics = false
    cache.use_second_level_cache = false
    cache.use_query_cache = false
    cache.use_minimal_puts = true
    cache.provider_class = 'org.hibernate.cache.EhCacheProvider'
    //cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'

    default_batch_fetch_size = 25
    jdbc.fetch_size = 25
    jdbc.batch_size = 15
    order_inserts = true
    order_updates = true
    //jdbc.batch_versioned_data = true
    //max_fetch_depth = 5
}

// Environment specific settings
environments {
    development {
        dataSource {
            //dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/openboxes_dev?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
            loggingSql = false
            format_sql = false
            use_sql_comments = false
        }
    }
    test {
        dataSource {
            url = "jdbc:mysql://localhost:3306/openboxes_diff?autoReconnect=true&zeroDateTimeBehavior=convertToNull"
        }
    }
    production {
        dataSource {
            url = "jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
        }
    }
    diff {
        dataSource {
            url = "jdbc:mysql://localhost:3306/openboxes_diff?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
        }
    }


}
