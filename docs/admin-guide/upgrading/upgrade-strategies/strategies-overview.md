# Strategies Overview

When it comes to upgrading to a new version of the application, there are a number of things that you will likely
be focusing on:

- Reducing downtime
- Mitigating errors
- Reducing costs
- Designing a reliable backup and rollback strategy for your database and configuration

With those concerns in mind, we've documented two possible approaches that you can take when performing upgrades:

1. In-Place Upgrade
2. Parallel Upgrade (aka Side-By-Side Upgrade)

We encourage you to read through this whole page to get a better understanding of each of the strategies, but if you're
only going to take away one thing from this page, it should be the following recommendation:

!!! info

    If you're doing an upgrade on a patch release (Ex: 0.9.1 -> 0.9.2), you have a staging environment where you can
    test the upgrade, and there are no special instructions for that upgrade, you're likely safe doing an in-place
    upgrade.

    In **any** other scenario, we highly recommend you consider a parallel upgrade.


## In-Place Upgrades

An in-place upgrade is when you upgrade the application and its dependencies directly on the existing server. In-place
upgrades are simpler to perform since they require no new infrastructure, but they introduce significant risk due to
their destructive nature. If an upgrade fails, it can leave the system in an error state with the server unable to
start. Unless you're prepared with a rollback plan, this can result in significant downtime as you work to resolve
the issue.

The general steps for performing an in-place upgrade are as follows:

* [ ] Bring down the app server
* [ ] Take a database and custom configuration backup
* [ ] Update any dependencies as required by the new app version
* [ ] Download the new app version
* [ ] Start the server and run checks to verify it's working

!!! warning

    The in-place upgrade process is deceptively simple. It's essential to be aware that if anything goes wrong during
    the upgrade, it is significantly harder to rollback compared to parallel upgrades.


## Parallel Upgrades

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


## Which Strategy to Choose

The upgrade approach that you choose to follow will depend on the precise details of your unique implementation.
That said, we strongly recommend that you abide by the following guidelines:

!!! info

    If you're doing an upgrade on a patch release (Ex: 0.9.1 -> 0.9.2), you have a staging environment where you can
    test the upgrade, and there are no special instructions for that upgrade, you're likely safe doing an in-place
    upgrade.

    In **any** other scenario, we highly recommend you consider a parallel upgrade.

Regardless of the strategy that you choose, we highly recommend that you always test upgrades on a staging environment
before attempting them in production. Practicing upgrades on an environment that mirrors your production setup will help
alert you to errors early, and inform you of any configuration changes that will be required when applying that same
change on production.

The first place you test upgrades should never be production.


### Strategy Tradeoffs

<div class="grid cards" markdown>

-   :material-cloud-plus:{ .lg .middle } __Parallel Migration__

    Setting up a new server environment alongside the existing one, then migrating over when ready.

    ---

    :white_check_mark: Easier to roll back the upgrade if something goes wrong

    :white_check_mark: Easier and less risky to set up if you have numerous dependencies being upgraded

    :white_check_mark: You can perform the upgrade at your own pace

    :white_check_mark: Allows you to test the new environment thoroughly before committing to switching over

    :white_check_mark: Typically results in less downtime during the switch

    :warning: Requires more resources/costs to maintain two environments temporarily

    :warning: Riskier when you have a large amount of custom configuration to copy over

    :warning: Typically has a more time-consuming setup


-   :material-cloud-sync:{ .lg .middle } __In-Place Upgrade__

    Upgrading the application and its dependencies directly on the existing server.

    ---

    :warning: Much harder to roll back the upgrade if something goes wrong

    :warning: Higher risk of downtime and service disruption, especially when dependency upgrades are involved

    :warning: Once you start, it's hard to turn back

    :warning: Cannot test the upgrade in parallel. Once you start, you'll be incurring downtime

    :white_check_mark: Minimal cost impact and requires no infrastructure changes
    
    :white_check_mark: Avoids needing to copy over any custom configuration and settings

    :white_check_mark: When successful, will be faster than parallel upgrades






</div>
