package org.pih.warehouse.migration

import grails.plugin.databasemigration.GrailsClassLoaderResourceAccessor
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import org.codehaus.groovy.grails.commons.GrailsApplication

class MigrationCallbacks {

    GrailsApplication grailsApplication
    def migrationResourceAccessor


    //public boolean isExecuting = false

    void beforeStartMigration(Database database) {
        //isExecuting = true
        log.info "Before starting migration " + database

    }

    void onStartMigration(Database database, Liquibase liquibase, String changelogName) {
        log.info "On start migration for database " + database + ", changelog " + changelogName

        def ranChangeSets = database.getRanChangeSetList()
        log.info "There are ${ranChangeSets?.size()} ranChangeSets "
        if (!ranChangeSets) {
            log.info "Unrun changesets: " + liquibase.listUnrunChangeSets()
            //String contexts = null
            //liquibase.update(contexts)
        }


    }

    void afterMigrations(Database database) {
        log.info "After migrations " + database
        //isExecuting = false
    }
}