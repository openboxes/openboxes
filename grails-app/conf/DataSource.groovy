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
			dbCreate = "update" 	
			//dbCreate = "create-drop"			
			url = "jdbc:mysql://localhost:3306/warehouse_dev?zeroDateTimeBehavior=convertToNull"
		}
	}
	test {
		dataSource {			
			url = "jdbc:mysql://localhost:3306/warehouse_test?zeroDateTimeBehavior=convertToNull"
		}
	}
	production {
		dataSource {
			url = "jdbc:mysql://localhost:3306/warehouse_prod?zeroDateTimeBehavior=convertToNull"
		}
	}
	diff {
		dataSource {
			// Used with the 'db-diff-incremental' script
			dbCreate = "create-drop"
			url = "jdbc:mysql://localhost:3306/warehouse_diff?autoreconnect=true"
		}
	}
	demo {
		dataSource {
			// Used when starting a new version of the migrations changelog 
			dbCreate = "create-drop"
			url = "jdbc:mysql://localhost:3306/warehouse_demo?zeroDateTimeBehavior=convertToNull"
		}
	}

	
}
