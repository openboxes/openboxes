import java.text.DateFormat
import java.text.SimpleDateFormat

includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(rollbackToDate: '''Rolls back the specified date.
Example: grails rollback-to-date 2007-05-15 18:15:12 
''') {
    setup()

    try {
        def DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        liquibase.rollback(dateFormat.parse(liquibase.util.StringUtils.join(Arrays.asList(args), " ").replaceAll("\n", " ")), null);
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("rollbackToDate")
