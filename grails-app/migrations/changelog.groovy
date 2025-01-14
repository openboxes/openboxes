import org.apache.log4j.Logger
import util.LiquibaseUtil
import util.TaggedMigrationVersion

final Logger LOGGER = Logger.getLogger('changelog')

/**
 * The entrypoint changelog for all liquibase migrations.
 *
 * When releasing a new major or minor version of the app we need to do the following:
 *
 * 1) Add a 'final' changelog that marks the end of the release with a tagged changeSet.
 *
 * 2) Create a folder for the next version with a base changelog.xml and add the version to LiquibaseUtil.ALL_VERSIONS.
 *
 * 3) Regenerate the 'install' migrations. This is achieved by running: 'grails dbm-generate-changelog changelog.xml'
 *    (make sure your local database is migrated up to the end of the current release). This will generate new
 *    changeSets that we can use to update the changelogs in the install folder.
 *    https://grails.github.io/grails-database-migration/2.0.x/index.html#ref-rollback-scripts-dbm-generate-changelog
 *
 *    If you are unable to perform step 3, you must set a fixed value for LiquibaseUtil.CLEAN_INSTALL_VERSION. Otherwise
 *    migrations will assume that the "install" path brings you up to the latest release version, which it won't.
 *
 * Our liquibase migrations take a conventions over configurations approach in that they assume all changelogs exist
 * within a release folder (such as 'migrations/0.9.x'). This allows us to more easily determine which changelogs have
 * already ran (which is necessary for fresh installs where we're not actually running changelogs for old releases).
 */
databaseChangeLog = {
    LOGGER.info("Dropping all views. They will be rebuilt after migrations complete...")
    include(file: 'views/drop-all-views.xml')

    // Find out what migration version/folder we're currently on.
    TaggedMigrationVersion currentVersion = LiquibaseUtil.getCurrentVersion()

    // If we have no migrations performed yet, we're starting from a clean install. This means we can use our
    // consolidated 'install' changelog to speed up migrations.
    if (currentVersion == null) {
        LOGGER.info("Executing migrations on new installation. Running consolidated migrations...")
        include(file: 'install/changelog.xml')

        // We only ever regenerate the install changelogs once we've fully locked down a release (aka there are no
        // more changelogs being added to that version). This means that if we've clean installed up to the version
        // defined in CLEAN_INSTALL_VERSION, our next scripts to run come from the version AFTER that one.
        currentVersion = LiquibaseUtil.getNextVersion(LiquibaseUtil.CLEAN_INSTALL_VERSION)
    }
    else {
        LOGGER.info("Current migration version is ${currentVersion}. Skipping older migrations. They've already ran.")
    }

    // At this point we know we've run *some* migrations but we might not be fully up to date, so bring us to the
    // latest change set by running all changelogs from the current release version and newer (in order). This is safe
    // to do because although we might revisit change sets from the current release version that we've already run,
    // liquibase is smart enough to know to skip them.
    List<TaggedMigrationVersion> currentAndNewerReleases = LiquibaseUtil.getCurrentAndNewerVersions(currentVersion)
    for (TaggedMigrationVersion release : currentAndNewerReleases) {
        // Sometimes these migrations take a long time and liquibase doesn't publish the logs until the whole
        // thing completes, which may cause people to assume the process is stuck.
        LOGGER.info("Executing migrations for release version: ${release}")
        include(file: release.toString() + "/changelog.xml")
    }

    // Rebuild all views
    include(file: 'views/changelog.xml')
}
