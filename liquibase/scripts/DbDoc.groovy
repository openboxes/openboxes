includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(dbDoc: '''Generates Javadoc-like documentation based on current database and change log''') {
    setup()

    try {
        liquibase.generateDocumentation("dbdoc")
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("dbDoc")
