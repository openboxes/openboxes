import util.LiquibaseUtil
import util.TaggedMigrationVersion

/*
 * Because we skip changelogs under the release folders (Ex: '0.9.x') when doing clean installs, we don't get tagged
 * changesets, and so we need to manually define what release the 'install' version of the migration brings us to. We
 * don't want to assume that it always brings us to the latest release because we may have forgotten or decided not to
 * update/regenerate the 'install' migrations during the release process.
 */
final TaggedMigrationVersion CLEAN_INSTALL_VERSION = new TaggedMigrationVersion('0.8.x')

/*
 * List all of our tagged migration versions. Any new tagged versions should be added to this list.
 * By convention, these should map to the migrations directories.
 */
final List<TaggedMigrationVersion> ALL_VERSIONS = [
        new TaggedMigrationVersion('0.5.x'),
        new TaggedMigrationVersion('0.6.x'),
        new TaggedMigrationVersion('0.7.x'),
        new TaggedMigrationVersion('0.8.x'),
        new TaggedMigrationVersion('0.9.x'),
]

/**
 * The entrypoint changelog for all liquibase migrations.
 *
 * When releasing a new major or minor version of the app we need to do the following:
 *
 * 1) Add a 'final' changelog that marks the end of the release with a tagged changeSet.
 *
 * 2) Create a folder for the next version with a base changelog.xml and add the version to ALL_VERSIONS (see above).
 *
 * 3) Regenerate the 'install' migrations. This is achieved by running: 'grails dbm-generate-changelog changelog.xml'
 *    (make sure your local database is migrated up to the end of the current release). This will generate new
 *    changeSets that we can use to update the changelogs in the install folder. After that is done, remember to update
 *    CLEAN_INSTALL_VERSION (see above) to be the newest release version.
 *    https://grails.github.io/grails-database-migration/2.0.x/index.html#ref-rollback-scripts-dbm-generate-changelog
 *
 * Our liquibase migrations take a conventions over configurations approach in that they assume all changelogs exist
 * within a release folder (such as 'migrations/0.9.x'). This allows us to more easily determine which changelogs have
 * already ran (which is necessary for fresh installs where we're not actually running changelogs for old releases).
 */
databaseChangeLog = {

    // Drop all views (they are recreated at the end).
    include(file: 'views/drop-all-views.xml')

    // If we have no migrations performed yet, we're starting from a clean install. This means we can use our
    // consolidated 'install' changelog to speed up migrations.
    TaggedMigrationVersion currentVersion
    if (!LiquibaseUtil.haveMigrationsEverRan()) {
        include(file: 'install/changelog.xml')

        // We only ever regenerate the install changelogs once we've fully locked down a release (aka there are no
        // more changelogs being added to that version). This means that if we've clean installed up to the version
        // defined in CLEAN_INSTALL_VERSION, our next scripts to run come from the version AFTER that one.
        currentVersion = LiquibaseUtil.getNextVersion(CLEAN_INSTALL_VERSION, ALL_VERSIONS)
    }
    // Otherwise we're doing an upgrade on an existing install so find out what migration version we got to so far.
    else {
        currentVersion = LiquibaseUtil.getCurrentVersion(ALL_VERSIONS)
    }

    // At this point we know we've run *some* migrations but we might not be fully up to date, so bring us to the
    // latest change set by running all changelogs from the current release version and newer (in order). This is safe
    // to do because although we might revisit change sets from the current release version that we've already run,
    // liquibase is smart enough to know to skip them.
    List<TaggedMigrationVersion> currentAndNewerReleases = ALL_VERSIONS.findAll{ it >= currentVersion }.sort()
    for (TaggedMigrationVersion release : currentAndNewerReleases) {
        include(file: release.toString() + "/changelog.xml")
    }

    // Rebuild all views
    include(file: 'views/changelog.xml')
}
