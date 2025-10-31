# Parallel Upgrade Overview

A parallel upgrade is when you create a totally new installation of the app that exists alongside the old one. Once the
new installation is fully configured, you can simply migrate over to using it. This new installation (which is running
the new version of the app) can live anywhere from a newly provisioned machine on a cloud server, to a new virtual
machine or container on the same host. All that matters is that the installation exists somewhere where making changes
to it won't affect the old installation.

Parallel upgrades are more complex than in-place upgrades, but they allow you to upgrade at your own pace, only causing
downtime when you're ready to finalize the switch over to the new installation. They also save you from having to worry
about dependency upgrades, since you can simply install up-to-date dependencies fresh on the new machine. Additionally,
rollbacks are much simpler to perform since the old installation remains untouched throughout the process. If done
correctly, parallel upgrades are non-destructive.

The general steps for performing a parallel upgrade are as follows:

* [ ] Install the new version of the app in a newly provisioned machine or container
* [ ] Copy any custom configuration from the old installation to the new one
* [ ] Take a database backup from the old installation (for testing purposes)
* [ ] Import the database backup to the new installation
* [ ] Start the server for the new installation and run checks to verify it's working
* [ ] Bring down both the old and new app servers
* [ ] Repeat the previous steps to take and restore a new database backup to the server, restart it, and test it once more
* [ ] Update your DNS settings to point to the new installation
* [ ] Deprovision the old instance


## Tradeoffs

<div class="grid cards" markdown>

-   :material-cloud-plus:{ .lg .middle } __Positives__

    :white_check_mark: Easier to roll back the upgrade if something goes wrong

    :white_check_mark: Easier and less risky to set up if you have numerous dependencies being upgraded

    :white_check_mark: You can perform the upgrade at your own pace

    :white_check_mark: Allows you to test the new environment thoroughly before committing to switching over

    :white_check_mark: Typically results in less downtime during the switch


-   :material-cloud-sync:{ .lg .middle } __Negatives__

    :warning: Requires more resources/costs to maintain two environments temporarily

    :warning: Riskier when you have a large amount of custom configuration to copy over

    :warning: Typically has a more time-consuming setup

</div>
