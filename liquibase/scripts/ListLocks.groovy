includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")

task ('default':'''Lists who currently has locks on the database changelog''') {
    setup()

    try {
        liquibase.reportLocks(System.out)
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        liquibase.getDatabase().getConnection().close();
    }
}
