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
			//dbCreate = "update"			
			url = "jdbc:mysql://localhost:3306/openboxes_dev?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
		}
	}
	test {
		dataSource {			
			url = "jdbc:mysql://localhost:3306/openboxes_test?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
        }
	}
	loadtest {
		dataSource {			
			url = "jdbc:mysql://localhost:3306/openboxes_loadtest?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
        }
	}
	production {
		dataSource {
			url = "jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
		}
	}
	staging {
		dataSource {
			url = "jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
		}
	}
	qa {
		dataSource {
			url = "jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
		}
	}
	diff {
		dataSource {
			url = "jdbc:mysql://localhost:3306/openboxes_diff?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
		}
	}
	root {
		dataSource {
			url = "jdbc:mysql://localhost:3306/openboxes_root?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
		}
	}
	client {
		dataSource {
			url = "jdbc:mysql://localhost:3306/openboxes_client?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB"
		}
	}

}
