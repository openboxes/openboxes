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

    // https://grails.github.io/grails2-doc/1.3.9/guide/single.html#3.3%20The%20DataSource
    // https://javadoc.io/doc/commons-dbcp/commons-dbcp/1.4/index.html
    properties {
        initialSize = 5
        maxActive = 100
        maxIdle = 10
        minEvictableIdleTimeMillis = 120 * 1000
        minIdle = 5
        numTestsPerEvictionRun = 5
        testOnBorrow = false
        testOnReturn = false
        testWhileIdle = true
        timeBetweenEvictionRunsMillis = 60 * 1000
        validationQueryTimeout = 1
    }
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
            // dbCreate = "update"
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
