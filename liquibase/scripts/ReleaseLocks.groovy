includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(releaseLocks: '''Releases all locks on the database changelog''') {
    setup()

    try {
        liquibase.forceReleaseLocks()
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("releaseLocks")
