includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(validateChangelog: '''Checks changelog for errors''') {
    setup()

    try {
        liquibase.validate()
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("validateChangelog")
