
import grails.util.GrailsUtil;
import liquibase.Liquibase;
import liquibase.ClassLoaderFileOpener;
import liquibase.database.DatabaseFactory;

/**
 * Runs the Liquibase migrate script if the environment is 'development'. 
 */
class BootStrap {
		
	def dataSource 

	def init = { servletContext ->
		
		if (GrailsUtil.environment == 'development') {
			log.info("\t\tRunning liquibase changelog(s) ...")
			Liquibase liquibase = null
			try {
				def c = dataSource.getConnection()
				if (c == null) {
					throw new RuntimeException("Connection could not be created.");
				}
				//LiquibaseUtil.class.getClassLoader();
				def classLoader = getClass().classLoader;
            			def fileOpener = classLoader.loadClass("org.liquibase.grails.GrailsFileOpener").getConstructor().newInstance() 

				//def fileOpener = new ClassLoaderFileOpener()
				def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(c)
				log.info("\t\tSeting default schema to " + c.catalog)
				database.setDefaultSchemaName(c.catalog)
				liquibase = new Liquibase("changelog.xml", fileOpener, database);
				liquibase.update(null)
			}
			//catch (Exception e) {
			//	log.error("Error running liquibase changelog(s) ...", e)
			//	throw e;				
			//}
			finally {
				if (liquibase && liquibase.database) {
					liquibase.database.close()
				}
			}
			log.info("\t\tFinished running liquibase changelog(s)!")
		}

		def destroy = {
			
		}
		
	}
	
}
