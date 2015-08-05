includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(changelogSyncSql: '''Writes SQL to mark all changes as executed in the database to STDOUT''') {
    setup()

    try {
        def outStream = (args) ? new PrintStream(args) : System.out
        liquibase.changeLogSync(null, new PrintWriter(outStream))
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to update database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("changelogSyncSql")
