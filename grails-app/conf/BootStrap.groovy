/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import javax.sql.DataSource;

class BootStrap {

	DataSource dataSource;
	
	def init = { servletContext ->
		
		// ================================    Static Data    ============================================
		//
		// Use the 'demo' environment to create a database with 'static' and 'demo' data.  Then  
		// run the following: 
		//
		// 		$ grails -Dgrails.env=demo run-app	
		//
		// In another terminal, run through these commands to generate the appropriate 
		// changelog files for a new version of the data model 
		// 
		// 		$ grails db-diff-schema > grails-app/migrations/x.x.x/changelog-initial-schema.xml
		// 		$ grails db-diff-index > grails-app/migrations/x.x.x/changelog-initial-indexes.xml
		// 		$ grails db-diff-data > grails-app/migrations/x.x.x/changelog-initial-data.xml
		// 
		// Migrating existing data to the new data model is still a work in progress, but you can 
		// use the previous versions changelogs.  
		//
		//if (GrailsUtil.environment == 'test' || GrailsUtil.environment == 'development' || 
			//GrailsUtil.environment == 'client' || GrailsUtil.environment == 'root') {
		log.info("Running liquibase changelog(s) ...")
		Liquibase liquibase = null
		try {
			
			def connection = dataSource.getConnection()
			if (connection == null) {
				throw new RuntimeException("Connection could not be created.");
			}
			//LiquibaseUtil.class.getClassLoader();
			def classLoader = getClass().classLoader;
			def fileOpener = classLoader.loadClass("org.liquibase.grails.GrailsFileOpener").getConstructor().newInstance()

			//def fileOpener = new ClassLoaderFileOpener()
			def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
			log.info("Setting default schema to " + connection.catalog)
			log.info("Product Version: " + database.databaseProductVersion)
			log.info("Database Version: " + database.databaseMajorVersion + "." + database.databaseMinorVersion)
			def ranChangeSets = database.getRanChangeSetList()
			database.setDefaultSchemaName(connection.catalog)
			
			// If nothing has been created yet, let's create all new database objects with the install scripts
			//if (!ranChangeSets) { 
			//	liquibase = new Liquibase("install.xml", fileOpener, database);
			//	liquibase.update(null)
			//}
			
			// Run through the updates in the master changelog
			liquibase = new Liquibase("changelog.xml", fileOpener, database);
			liquibase.update(null)
		}
		finally {
			if (liquibase && liquibase.database) {
				liquibase.database.close()
			}
		}
		log.info("Finished running liquibase changelog(s)!")
	}		
		
				
	def destroy = {
		
	}
		
	
}
