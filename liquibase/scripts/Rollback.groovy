includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(rollback: '''Rolls back the to a specific tag.
Example: grails rollback aTag
''') {
    setup()

    try {
        liquibase.rollback(args, null);
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("rollback")
