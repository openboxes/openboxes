import grails.util.GrailsUtil;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import java.util.Date;
import javax.sql.DataSource;
import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.DataType;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.DocumentType;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Role;
import org.pih.warehouse.core.RoleType;
import org.pih.warehouse.core.User;
import org.pih.warehouse.donation.Donor;
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.inventory.TransactionType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Attribute;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.ContainerType;
import org.pih.warehouse.shipping.ReferenceNumberType;
import org.pih.warehouse.shipping.ReferenceNumber;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentType;
import org.pih.warehouse.shipping.ShipmentMethod;
import org.pih.warehouse.shipping.Shipper;
import org.pih.warehouse.shipping.ShipperService;

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
		log.info("\t\tRunning liquibase changelog(s) ...")
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
			log.info("\t\tSetting default schema to " + connection.catalog)
			log.info("\t\tProduct Version: " + database.databaseProductVersion)
			log.info("\t\tDatabase Version: " + database.databaseMajorVersion + "." + database.databaseMinorVersion)
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
		log.info("\t\tFinished running liquibase changelog(s)!")
	}		
		
				
	def destroy = {
		
	}
		
	
}
