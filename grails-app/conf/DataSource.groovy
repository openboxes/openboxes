// General data source properties
dataSource { 	
	pooled = true
	driverClassName = "com.mysql.jdbc.Driver"
	dialect = org.hibernate.dialect.MySQL5InnoDBDialect
	//dialect = org.hibernate.dialect.MySQLInnoDBDialect
	loggingSql = false
	username = "root"
	password = "root"
}

// Hibernate caching properties
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='org.hibernate.cache.EhCacheProvider'
}

// Environment specific settings
environments {
	development {
		dataSource {	
			// disable dbCreate when you create the initial database using '$ grails dev migrate'
			//dbCreate = "update" 	
			//dbCreate = "create-drop"			
			url = "jdbc:mysql://localhost:3306/warehouse_dev?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
		}
	}
	test {
		dataSource {			
			url = "jdbc:mysql://localhost:3306/warehouse_test?autoReconnect=true&amp;zeroDateTimeBehavior=convertToNull&amp;sessionVariables=storage_engine=InnoDB"
		}
	}
	production {
		dataSource {
			url = "jdbc:mysql://localhost:3306/warehouse_prod?autoReconnect=true&amp;zeroDateTimeBehavior=convertToNull&amp;sessionVariables=storage_engine=InnoDB"
		}
	}
	diff {
		dataSource {
			// Used with the 'db-diff-incremental' script
			//dbCreate = "create-drop"
			url = "jdbc:mysql://localhost:3306/warehouse_diff?autoReconnect=true&amp;zeroDateTimeBehavior=convertToNull&amp;sessionVariables=storage_engine=InnoDB"
		}
	}
	
}
