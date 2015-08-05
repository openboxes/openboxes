import liquibase.diff.Diff
import liquibase.database.DatabaseFactory

includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(generateChangelog: '''Writes Change Log XML to copy the current state of the database to standard out''') {
    setup()

    try {
        def outStream = System.out
        if (args) {
            outStream = new PrintStream(args)
        }
        def diff = new Diff(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection), null);
        //        diff.addStatusListener(new OutDiffStatusListener());
        def diffResult = diff.compare();
        
        diffResult.printChangeLog(outStream, liquibase.getDatabase(), classLoader.loadClass("org.liquibase.grails.GrailsXmlWriter").getConstructor().newInstance());
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("generateChangelog")
