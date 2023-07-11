package org.pih.warehouse.jobs

class DataMigrationJob {

    def migrationService

    static concurrent = false

    // By default this is true on QuartzDisplayJob, which invokes execute()
    // and if sessionRequired is true, then QuartzDisplayJob tries to do session flush
    // even if there is no session
    static sessionRequired = false

    static triggers = {}

    void execute() {
        if (JobUtils.shouldExecute(DataMigrationJob)) {
            log.info "Starting data migration job at ${new Date()}"
            def startTime = System.currentTimeMillis()
            migrationService.migrateInventoryTransactions()
            log.info "Finished data migration job in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
