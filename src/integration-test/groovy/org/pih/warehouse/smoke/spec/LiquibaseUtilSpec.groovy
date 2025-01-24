package org.pih.warehouse.smoke.spec

import util.LiquibaseUtil
import util.TaggedMigrationVersion

import org.pih.warehouse.smoke.spec.base.SmokeSpec

/**
 * Tests liquibase schema changes.
 */
class LiquibaseUtilSpec extends SmokeSpec {

    void "getCurrentVersionsByFolderName should only the newest version for new installs"() {
        when:
        List<TaggedMigrationVersion> versions = LiquibaseUtil.getCurrentVersionsByFolderName()

        then:
        // Integration tests are always ran from clean installs due to being run inside of test containers (which
        // are created from scratch whenever tests are ran). As such, the only versioned/non-install changelogs that we
        // should have run at this point are from the newest version folder (which hasn't yet been bundled into the
        // "install" changelogs). All other version folders/changelogs are skipped.
        assert versions.size() == 1
        assert versions.first() == LiquibaseUtil.getNewestAvailableVersion()
    }
}
