import java.sql.Driver
import liquibase.Liquibase
import liquibase.database.DatabaseFactory

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )
includeTargets << new File ( "${grailsHome}/scripts/Package.groovy" )
includeTargets << new File ( "${grailsHome}/scripts/Bootstrap.groovy" )

config = new ConfigObject()

liquibase = null
connection = null;

target('setup': "Migrates the current database to the latest") {
    depends(configureProxy, packageApp, classpath)

    loadApp()

    profile("automigrate the current database") {
        Properties p = config.toProperties()
        def driverClassName = config.dataSource.driverClassName
        def username = prepareString(p, config.dataSource.username, null)
        def password = prepareString(p, config.dataSource.password, null)
        def url = prepareString(p, config.dataSource.url, null)

        Driver driver;
        try {
            if (p.driver == null) {
                p.driver = DatabaseFactory.getInstance().findDefaultDriver(url)
            }
            if (p.driver == null) {
                throw new RuntimeException("Driver class was not specified and could not be determined from the url")
            }
            driver = (Driver) Class.forName(driverClassName, true, classLoader).newInstance()
        }
        catch (Exception e) {
            throw new RuntimeException("Cannot get database driver: " + e.getMessage())
        }
        Properties info = new Properties()
        info.put("user", username)
        if (password != null) {
            info.put("password", password);
        }

        try {
            println "Connecting to database with URL: ${url}"
            connection = driver.connect(url, info);
            if (connection == null) {
                throw new RuntimeException("Connection could not be created to ${url} with driver ${driverClassName}.  Possibly the wrong driver for the given database URL");
            }
            def fileOpener = classLoader.loadClass("org.liquibase.grails.GrailsFileOpener").getConstructor().newInstance()
            liquibase = new Liquibase("changelog.xml", fileOpener, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

DEFAULT_PLACEHOLDER_PREFIX = '${'
DEFAULT_PLACEHOLDER_SUFFIX = '}'

def prepareString(Properties props, String strVal, String originalPlaceholder)
{
    int startIndex = strVal.indexOf(DEFAULT_PLACEHOLDER_PREFIX);
    while (startIndex != -1)
    {
        int endIndex = strVal.indexOf(DEFAULT_PLACEHOLDER_SUFFIX, startIndex + DEFAULT_PLACEHOLDER_PREFIX.length());
        if (endIndex != -1)
        {
            String placeholder = strVal.substring(startIndex + DEFAULT_PLACEHOLDER_PREFIX.length(), endIndex);
            String originalPlaceholderToUse = null;

            if (originalPlaceholder != null)
            {
                originalPlaceholderToUse = originalPlaceholder;
                if (placeholder.equals(originalPlaceholder))
                {
                    throw new RuntimeException("Circular placeholder reference '" + placeholder +"' in property definitions [" + props + "]");
                }
            }
            else
            {
                originalPlaceholderToUse = placeholder;
            }

            // get the property directly, and fall back to System properties as required
            String propVal = props.getProperty(placeholder);
            if (propVal == null)
                propVal = System.getProperty(placeholder);

            if (propVal != null)
            {
                propVal = prepareString(props, propVal, originalPlaceholderToUse);
                strVal = strVal.substring(0, startIndex) + propVal + strVal.substring(endIndex + 1);
                startIndex = strVal.indexOf(DEFAULT_PLACEHOLDER_PREFIX, startIndex + propVal.length());
            }
            else
            {
                // return unprocessed value
                return strVal;
            }
        }
        else
        {
            startIndex = -1;
        }
    }
    return strVal;
}