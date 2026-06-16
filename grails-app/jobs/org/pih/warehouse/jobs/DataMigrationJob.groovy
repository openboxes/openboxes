package org.pih.warehouse.jobs

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.User

class DataMigrationJob {

    def migrationService

    static concurrent = false

    def sessionRequired = false

    static triggers = {}

    void execute() {
        if (JobUtils.shouldExecute(DataMigrationJob)) {
            // Run as the robot user so any records created/updated are stamped with a valid current
            // user. withNewSession provides the Hibernate session needed to look up the robot user.
            User.withNewSession {
                AuthService.withRobotUser {
                    log.info "Starting data migration job at ${new Date()}"
                    def startTime = System.currentTimeMillis()
                    migrationService.migrateInventoryTransactions()
                    log.info "Finished data migration job in " + (System.currentTimeMillis() - startTime) + " ms"
                }
            }
        }
    }
}
