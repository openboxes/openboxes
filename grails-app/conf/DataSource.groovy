// General data source properties
dataSource { 	
	pooled = true
	driverClassName = "com.mysql.jdbc.Driver"
	dialect = org.hibernate.dialect.MySQL5InnoDBDialect
	loggingSql = false
	username = "root"
	password = "root"
}

// Hibernate caching properties
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'
}

// Environment specific settings
environments {
	development {
		dataSource {	
			// disable dbCreate when you create the initial database using '$ grails dev migrate'
			dbCreate = "create" 	
			url = "jdbc:mysql://localhost:3306/warehouse_dev?autoreconnect=true"
		}
	}
	test {
		dataSource {			
			url = "jdbc:mysql://localhost:3306/warehouse_test?autoreconnect=true"
		}
	}
	production {
		dataSource {
			url = "jdbc:mysql://localhost:3306/warehouse_prod?autoreconnect=true"
		}
	}
	diff {
		dataSource {
			// Used with the 'db-diff-incremental' script
			dbCreate = "create-drop"
			url = "jdbc:mysql://localhost:3306/warehouse_diff?autoreconnect=true"
		}
	}
	
	
}
