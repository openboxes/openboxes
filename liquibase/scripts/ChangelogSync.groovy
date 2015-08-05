includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(changelogSync: '''Mark all changes as executed in the database''') {
    setup()

    try {
        liquibase.changeLogSync(null)
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to update database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("changelogSync")
