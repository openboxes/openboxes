includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(rollbackCount: '''Rolls back the specified number of changes.
Example: grails rollback-count 3
''') {
    setup()

    try {
        liquibase.rollback(Integer.parseInt(args), null);
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().close();
    }
}

setDefaultTarget("rollbackCount")
