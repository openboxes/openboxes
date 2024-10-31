# Strategies Overview

When it comes to upgrading to a new version of the application, there are a number of concerns that you will likely
be focusing on:

- Reducing downtime
- Mitigating errors
- Reducing costs

With those concerns in mind, we've documented two possible approaches that you can take when performing upgrades:

1. [In-Place Upgrade](in-place-upgrade-overview.md): The upgrade is made directly to the current in-place environment hardware.
2. [Parallel Upgrade](parallel-upgrade-overview.md): A new, independent installation is created with the upgraded app version and is configured to
   mirror the current production environment. We migrate over to using the new installation when it is ready.


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
