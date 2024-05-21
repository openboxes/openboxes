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
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import liquibase.resource.ClassLoaderResourceAccessor

import java.nio.file.Paths

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
     * @deprecated Use databaseChangelogVersions
     * @return
     */
    @Deprecated
    static getExecutedChangelogVersions() {
        def ctx = Holders.grailsApplication.mainContext
        def dataService = ctx.getBean("dataService")
        String sql = """
            SELECT DISTINCT(SUBSTRING(filename, 1, locate('/', filename)-1)) as version
            FROM DATABASECHANGELOG"""
        return dataService.executeQuery(sql);
    }

    static Set<String> getChangeLogVersions() {
        return getChangeLogVersions("changelog.groovy")
    }

    static Set<String> getUpgradeChangeLogVersions() {
        return getChangeLogVersions("upgrade/changelog.xml")
    }

    static Set<String> getChangeLogVersions(String changeLogFile) {
        Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database)

        // Retrieve the file path for all changeset in the given the changeLogFile
        def changeSetFilePaths = liquibase.databaseChangeLog.rootChangeLog.changeSets.collect { ChangeSet changeSet ->
            return changeSet.filePath
        }

        // Find all parent directories with names matching current versions pattern which
        // will match to any version number between 0.0.x to 999.999.x
        Set<String> changeLogVersions = changeSetFilePaths
                    .collect { Paths.get(it).parent.toString() }
                    .findAll { it.matches("\\d{1,3}.\\d{1,3}.x") }
                    .sort()

        return changeLogVersions
    }

    static executeChangeLog(String changeLogFile) {
        Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database)
        liquibase.update(null as Contexts, new LabelExpression());
    }


}
