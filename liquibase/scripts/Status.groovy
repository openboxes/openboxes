includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(status: '''Outputs list of unrun changesets''') {
    setup()

    try {
        liquibase.reportStatus(true, null, new OutputStreamWriter(System.out))
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("status")
