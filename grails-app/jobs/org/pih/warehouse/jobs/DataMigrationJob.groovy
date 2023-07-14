package org.pih.warehouse.jobs

class DataMigrationJob {

    def migrationService

    static concurrent = false

    def sessionRequired = false

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
