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

import liquibase.database.DatabaseFactory
import liquibase.lockservice.LockService
import liquibase.lockservice.StandardLockService
import org.apache.commons.logging.LogFactory
import grails.util.Holders

class LiquibaseUtil {

    private static final log = LogFactory.getLog(this)

    static getDatabase() {
        def ctx = Holders.getGrailsApplication().getMainContext()
        def dataSource = ctx.getBean("dataSource")
        def connection = dataSource.getConnection()
        def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
        return database
    }

    static synchronized isRunningMigrations() {
        boolean isRunning = false
        try {
            LockService lockService = new StandardLockService()
            lockService.database = getDatabase()
            isRunning = lockService.hasChangeLogLock()
        } catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            if (database) {
                database.close()
            }
        }
        return isRunning
    }
}
