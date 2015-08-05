includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(migrateSql: "Writes SQL to update database to current version to STDOUT.") {
    setup()

    try {
        def outStream = (args) ? new PrintStream(args) : System.out
        liquibase.update(null, new PrintWriter(outStream));
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("migrateSql")
