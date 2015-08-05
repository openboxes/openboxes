includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(dropAll: '''Drops all objects in database owned by the connected user.
Example: grails dropAll
''') {
    setup()

    try {
        liquibase.dropAll();
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to drop database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("dropAll")
