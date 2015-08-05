includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(rollbackSql: '''Writes SQL to roll back the database to that state it was in at when the tag was applied to STDOUT.
Example: grails rollback-sql aTag
''') {
    setup()

    try {
        def outStream = (args) ? new PrintStream(args) : System.out
        liquibase.rollback(args, null, new OutputStreamWriter(System.out));
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("rollbackSql")
