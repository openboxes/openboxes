# 1. Upgrade Dependencies

A releases may require dependency upgrades (such as a new Java or Tomcat version). You'll need to manually apply
those upgrades to your system before installing the new version of the application.

!!! warning

    Because in-place upgrades are destructive, it is very important that you follow the steps in the
    ["plan" phase](../../plan/rollback-strategies/overview.md)
    for taking backups of your database and configuration before proceeding any further with the upgrade. Without a
    proper rollback strategy it will be incredibly difficult to revert any changes if the upgrade fails.


## Bring down the app server

Before you start installing dependency upgrades, you need to stop your application server.

!!! note
    This is where your downtime begins.

{% include 'admin-guide/common/_stop_server.md' %}


## Upgrade All Dependencies

Make sure to check if there are [version-specific instructions](../version-specific/index.md) for the version that
you're upgrading to. Those instructions will typically inform you of what dependencies need to be upgraded and how.
