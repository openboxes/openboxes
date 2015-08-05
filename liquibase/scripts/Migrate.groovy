includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(migrate: "Updates a database to the current version.") {
    setup()

    try {
        System.out.println("Migrating ${grailsEnv} database");
        liquibase.update(null)
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}

setDefaultTarget("migrate")
