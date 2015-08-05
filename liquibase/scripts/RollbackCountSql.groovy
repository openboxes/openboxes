includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(rollbackCountSql: '''Writes SQL to roll back the specified number of changes to STDOUT.
Example: grails rollback-count-sql 3
''') {
    setup()

    try {
        def outStream = (args) ? new PrintStream(args) : System.out
        liquibase.rollback(Integer.parseInt(args), null, new OutputStreamWriter(outStream));
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("rollbackCountSql")
