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

/**
 * Utility methods for running Liquibase database migrations.
 */
class LiquibaseUtil {

    /**
     * Because we skip changelogs under the release folders (Ex: '0.9.x') when doing clean installs, we don't get tagged
     * changesets, and so we need to manually define what release the 'install' version of the migration brings us to.
     *
     * There are two methods of defining this:
     * 1) Setting CLEAN_INSTALL_VERSION = 'LATEST_VERSION'.
     *    Automatically computes the version to use based on the values in 'ALL_VERSIONS'. This is the preferred option.
     *
     * 2) Setting a specific version. Ex: CLEAN_INSTALL_VERSION = new TaggedMigrationVersion('0.8.x')
     *    Useful in the case where you are unable to regenerated the 'install' migrations during the release process for
     *    whatever reason. The version to set is the latest version that *is* consolidated in the "install" scripts.
     */
    public static final TaggedMigrationVersion CLEAN_INSTALL_VERSION = TaggedMigrationVersion.LATEST_VERSION

    /**
     * List all of our tagged migration versions. Any new tagged versions should be added to this list.
     * By convention, these should map to the directories under /grails-app/migrations
     */
    public static final List<TaggedMigrationVersion> ALL_VERSIONS = [
            new TaggedMigrationVersion('0.5.x'),
            new TaggedMigrationVersion('0.6.x'),
            new TaggedMigrationVersion('0.7.x'),
            new TaggedMigrationVersion('0.8.x'),
            new TaggedMigrationVersion('0.9.x'),
    ]

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
            throw new RuntimeException('Cannot determine base changelog file when running Liquibase migrations. Check your configuration.')
        }

        Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database)
        liquibase.update(null as Contexts, new LabelExpression())
    }

    /**
     * @return a list of all changelog tags (ex: '0.9.x') that have been successfully migrated to.
     */
    static List<TaggedMigrationVersion> getCurrentTaggedVersions() {
        DataService dataService = Holders.grailsApplication.mainContext.getBean("dataService")
        List<GroovyRowResult> versionRows = dataService.executeQuery(
                "SELECT DISTINCT(tag) as version FROM DATABASECHANGELOG")
        return rowsToVersions(versionRows)
    }

    /**
     * @return a list of all release versions (ex: '0.9.x') that have had at least one changelog in their folder run.
     */
    static List<TaggedMigrationVersion> getCurrentVersionsByFolderName() {
        DataService dataService = Holders.grailsApplication.mainContext.getBean("dataService")
        List<GroovyRowResult> versionRows = dataService.executeQuery(
                """SELECT DISTINCT(SUBSTRING(filename, 1, locate('/', filename)-1)) as version
                         FROM DATABASECHANGELOG"""
        )
        return rowsToVersions(versionRows)
    }

    /**
     * @return The current tagged version that we've migrated up to.
     */
    static TaggedMigrationVersion getCurrentVersion() {
        // If at least one tag exists in the database we're in an existing install in the new flow.
        List<TaggedMigrationVersion> versions = getCurrentTaggedVersions()
        if (versions.size() > 0) {
            // Having a tagged version in the database means we've completed all migrations for that version,
            // so actually we're on the version AFTER the latest tagged one.
            return getNextVersion(versions.max())
        }

        // This is for clean installs and for backwards compatability when running upgrades/migrations on 0.8.x
        // deployments and older (both of which won't have any tagged changelogs).
        versions = getCurrentVersionsByFolderName()
        return versions.max()
    }

    /**
     * @return the currently upgraded to migration version (Ex: '0.9.x') and any versions with a higher semver value.
     */
    static List<TaggedMigrationVersion> getCurrentAndNewerVersions(TaggedMigrationVersion currentVersion) {
        return currentVersion == null ? [] : ALL_VERSIONS.findAll{ it >= currentVersion }.sort()
    }

    /**
     * @return the newest migration version as defined by the migration folders.
     */
    static TaggedMigrationVersion getNewestAvailableVersion() {
        return ALL_VERSIONS.sort().last()
    }

    /**
     * @return The next available migration version. If there is no next version, will return the current version.
     * Ex: if we have the following versions: ['0.8.x', '0.9.x', '1.0.x'], the next version of '0.8.x' is '0.9.x'
     * Ex: if we have the following versions: ['0.8.x', '0.9.x'], the next version of '0.9.x' is '0.9.x'
     * Ex: if we have the following versions: ['0.8.x', '0.9.x'], the next version of 'LATEST' is '0.9.x'
     */
    static TaggedMigrationVersion getNextVersion(TaggedMigrationVersion currentVersion) {
        if (currentVersion == TaggedMigrationVersion.LATEST_VERSION) {
            return getNewestAvailableVersion()
        }

        TaggedMigrationVersion nextVersion = null
        for (TaggedMigrationVersion version : ALL_VERSIONS) {
            if (currentVersion < version && (nextVersion == null || nextVersion > version)) {
                nextVersion = version
            }
        }
        return nextVersion ?: currentVersion
    }

    private static List<TaggedMigrationVersion> rowsToVersions(List<GroovyRowResult> rows) {
        List<TaggedMigrationVersion> versions = []
        for (GroovyRowResult row : rows) {
            try {
                versions.add(new TaggedMigrationVersion(row.getProperty('version') as String))
            }
            catch (IllegalArgumentException ignored) {
                // For simplicity, ignore malformed versions. This will also ignore things like untagged changelogs.
            }
        }
        return versions
    }
}
