databaseChangeLog = {

    // When we release a new major version, we need to:
    //  - Recreate the install changelogs off the latest data model using gbm-generate-changelog
    //  - Add the previous version (e.g. 0.9.x/changelog.xml) to the grails-app/migrations/upgrade/changelog.xml
    //  - Add a new directory (e.g. 0.10.x) for new changelogs
    //  - Replace this changelog with the new version (e.g. 0.10.x/changelog.xml)
    include file: '0.9.x/changelog.xml'

    // Recreate all views AFTER all changesets have been applied (just in case)
    include file: 'views/changelog.xml'

}
