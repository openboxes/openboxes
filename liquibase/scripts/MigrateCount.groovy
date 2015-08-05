includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

target(migrateCount: "Applies the specified number of changes to a database.") {
    setup()

    try {
        System.out.println("Migrating ${grailsEnv} database");
        liquibase.update(Integer.parseInt(args), null);
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().close();
    }
}

setDefaultTarget("migrateCount")
