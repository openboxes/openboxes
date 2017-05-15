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
import org.springframework.cache.ehcache.EhCacheFactoryBean

beans = {

	customPropertyEditorRegistrar(util.CustomPropertyEditorRegistrar)

	//localeResolver(org.springframework.web.servlet.i18n.SessionLocaleResolver) {
	//	defaultLocale = new Locale("de","DE")
	//	java.util.Locale.setDefault(defaultLocale)
	//}

    dashboardCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "dashboardCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 86400       // 1 day = 60 * 60 * 24
        timeToIdle = 43200        // 12 hours = 60 * 60 * 12
    }
    dashboardTotalStockValueCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "dashboardTotalStockValueCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 86400       // 1 day = 60 * 60 * 24
        timeToIdle = 43200        // 12 hours = 60 * 60 * 12
    }
    dashboardProductSummaryCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "dashboardProductSummaryCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 86400       // 1 day = 60 * 60 * 24
        timeToIdle = 43200        // 12 hours = 60 * 60 * 12
    }
    dashboardGenericProductSummaryCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "dashboardGenericProductSummaryCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 86400       // 1 day = 60 * 60 * 24
        timeToIdle = 43200        // 12 hours = 60 * 60 * 12
    }
    inventorySnapshotCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "inventorySnapshotCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 86400       // 1 day = 60 * 60 * 24
        timeToIdle = 43200        // 12 hours = 60 * 60 * 12
    }
    inventoryBrowserCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "inventoryBrowserCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 3600       // 1 hour
        timeToIdle = 500        // 5 minutes
    }

    fastMoversCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "fastMoversCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 86400       // 1 day = 60 * 60 * 24
        timeToIdle = 43200        // 12 hours = 60 * 60 * 12
    }


    quantityOnHandCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "quantityOnHandCache"
        // these are just examples of properties you could set
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 86400       // 1 day = 60 * 60 * 24
        timeToIdle = 43200        // 12 hours = 60 * 60 * 12
    }

    megamenuCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "megamenuCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 3600       // 1 hour = 60 * 60 * 1
        timeToIdle = 1800       // 30 minutes = 60 * 60 * 0.5
    }

    selectTagCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "selectTagCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 3600       // 1 hour = 60 * 60 * 1
        timeToIdle = 1800       // 30 minutes = 60 * 60 * 0.5
    }

    selectTagsCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "selectTagsCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 3600       // 1 hour = 60 * 60 * 1
        timeToIdle = 1800       // 30 minutes = 60 * 60 * 0.5
    }
    selectCategoryCache(EhCacheFactoryBean) { bean ->
        cacheManager = ref("springcacheCacheManager")
        cacheName = "selectCategoryCache"
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 3600       // 1 hour = 60 * 60 * 1
        timeToIdle = 1800       // 30 minutes = 60 * 60 * 0.5
    }

    /**
	 * c3P0 pooled data source that allows 'DB keepalive' queries
	 * to prevent stale/closed DB connections
	 * Still using the JDBC configuration settings from DataSource.groovy
	 * to have easy environment specific setup available
	 */
	dataSource(ComboPooledDataSource) { bean ->
		bean.destroyMethod = 'close'
		//use grails' datasource configuration for connection user, password, driver and JDBC url
		user = CH.config.dataSource.username
		password = CH.config.dataSource.password
		driverClass = CH.config.dataSource.driverClassName
		jdbcUrl = CH.config.dataSource.url 
		//connection test settings
		idleConnectionTestPeriod = 2 * 60 * 60 // 2 hours
		initialPoolSize = 10
		maxPoolSize = 100
		maxStatements = 180
		// test connections 
		testConnectionOnCheckin = true
		//force connections to renew after 4 hours
		maxConnectionAge = 4 * 60 * 60
		//get rid too many of idle connections after 30 minutes
		maxIdleTimeExcessConnections = 30 * 60
	}
}


