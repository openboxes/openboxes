/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package util

import grails.core.GrailsApplication
import grails.util.Holders
import groovy.sql.GroovyRowResult
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import liquibase.resource.ClassLoaderResourceAccessor
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.data.DataService

class LiquibaseUtil {

    static getDatabase() {
        def dataSource = Holders.grailsApplication.mainContext.getBean("dataSource")
        def connection = new JdbcConnection(dataSource.connection)
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
    }

    static synchronized isRunningMigrations() {
        try {
            Database database = getDatabase()
            LockService lockService = LockServiceFactory.getInstance().getLockService(database)
            return lockService.hasChangeLogLock()
        } catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            database.close()
        }
        return false
    }

    /**
     * Run all liquibase migrations using the entrypoint changelog file as defined in the configuration of
     * the migration plugin: https://grails.github.io/grails-database-migration/latest/index.html
     */
    static void executeMigrations() {
        String changeLogFile = Holders.grailsApplication.config.getProperty('grails.plugin.databasemigration.changelogFileName')
        if (StringUtils.isBlank(changeLogFile)) {
            throw new RuntimeException('Cannot find base changelog file when running Liquibase migrations. Check your configuration.')
        }

        Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database)
        liquibase.update(null as Contexts, new LabelExpression())
    }

    /**
     * @return true if any liquibase migrations have run on this machine.
     */
    static boolean haveMigrationsEverRan() {
        DataService dataService = Holders.grailsApplication.mainContext.getBean("dataService")
        List<GroovyRowResult> result = dataService.executeQuery("SELECT EXISTS (SELECT 1 FROM DATABASECHANGELOG)")
        return result[0]  // Will return 1 (which is truthy) if any rows exist and 0 (which is falsy) otherwise.
    }

    /**
     * @return The current tagged version that we've migrated up to.
     */
    static TaggedMigrationVersion getCurrentVersion(List<TaggedMigrationVersion> allVersions) {
        DataService dataService = Holders.grailsApplication.mainContext.getBean("dataService")
        List<GroovyRowResult> versionRows = dataService.executeQuery(
                "SELECT DISTINCT(tag) as version FROM DATABASECHANGELOG")

        // If at least one tag exists in the database we're in the new flow (we do a > 1 here because there's
        // also always a null row returned representing the un-tagged changesets).
        if (versionRows.size() > 1) {
            List<TaggedMigrationVersion> versions = rowsToVersions(versionRows)

            // Having a tagged version in the database means we've completed all migrations for that version,
            // so actually we're on the version AFTER the latest tagged one.
            return getNextVersion(versions.max(), allVersions)
        }

        // This is for backwards compatability when migrating 0.9.x deployments and older. Any newer versions will have
        // liquibase tags properly set in the database which allows us to avoid this awkward lookup on folder name.
        String sql = """
            SELECT DISTINCT(SUBSTRING(filename, 1, locate('/', filename)-1)) as version
            FROM DATABASECHANGELOG"""
        versionRows = dataService.executeQuery(sql)
        List<TaggedMigrationVersion> versions = rowsToVersions(versionRows)
        return versions.max()
    }

    /**
     * @return The next version given a list of all versions. If there is no next version, will return null.
     * Ex: given ['0.8.x', '0.9.x', '1.0.x'], the next version of '0.8.x' is '0.9.x'
     */
    static TaggedMigrationVersion getNextVersion(TaggedMigrationVersion currentVersion,
                                                 List<TaggedMigrationVersion> allVersions) {
        TaggedMigrationVersion nextVersion = currentVersion
        for (TaggedMigrationVersion version : allVersions) {
            if (currentVersion < version && nextVersion > version) {
                nextVersion = version
            }
        }
        return nextVersion
    }

    private static List<TaggedMigrationVersion> rowsToVersions(List<GroovyRowResult> rows) {
        List<TaggedMigrationVersion> versions = []
        for (GroovyRowResult row : rows) {
            try {
                versions.add(new TaggedMigrationVersion(row.getProperty('version') as String))
            }
            catch (IllegalArgumentException ignored) {
                // For simplicity, ignore malformed versions.
            }
        }
        return versions
    }
}
