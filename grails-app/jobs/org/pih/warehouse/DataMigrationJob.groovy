package org.pih.warehouse

import org.quartz.DisallowConcurrentExecution

@DisallowConcurrentExecution
class DataMigrationJob {

    def concurrent = false  // make `static` in Grails 3
    def migrationService
    static triggers = {}

    def execute() {
        if (JobUtils.shouldExecute(DataMigrationJob)) {
            log.info "Starting data migration job at ${new Date()}"
            def startTime = System.currentTimeMillis()
            migrationService.migrateInventoryTransactions()
            log.info "Finished data migration job in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
