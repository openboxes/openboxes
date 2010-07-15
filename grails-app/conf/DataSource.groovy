dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
	development {
		dataSource {			
			dbCreate = "create-drop" // one of 'create', 'create-drop','update'
			url = "jdbc:hsqldb:mem:devDB"
			
			//pooled = true
			//driverClassName = "com.mysql.jdbc.Driver"
			//username = "root"
			//password = "root"
			//dbCreate = "create-drop"
			//url = "jdbc:mysql://localhost:3306/warehouse"
		
			
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			url = "jdbc:hsqldb:mem:testDb"
		}
	}
	production {
		dataSource {
			dbCreate = "update"
			url = "jdbc:hsqldb:file:prodDb;shutdown=true"
		}
	}
	


}


/*
    datasource(name: 'navCtlDS') {
     domainClasses([com.comcast.bui.menu.Menu])
	    services([com.comcast.bui.browse.MenuService])
	    readOnly(true)
	    driverClassName('oracle.jdbc.driver.OracleDriver')
	    url('jdbc:oracle:thin:@11.111.11.1:1521:xxxx')
	    username('NAV')
	    password('nav')
	    dbCreate('update')
	    logSql(true)
	    dialect(org.hibernate.dialect.Oracle10gDialect)
	    pooled(true)
	    environments(['development'])
	    hibernate {
		    cache {
			    use_second_level_cache(false)
			    use_query_cache(false)
		    }
	    }
    }

*/