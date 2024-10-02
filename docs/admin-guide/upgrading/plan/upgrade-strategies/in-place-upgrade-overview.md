# In-Place Upgrade Overview

!!! warning

    The in-place upgrade process is deceptively simple. It's essential to be aware that if anything goes wrong during
    the upgrade, it is significantly harder to rollback compared to parallel upgrades.

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
* [ ] Optionally uninstall old dependency versions


## Tradeoffs

<div class="grid cards" markdown>

-   :material-cloud-plus:{ .lg .middle } __Positives__

    :white_check_mark: Minimal cost impact and requires no infrastructure changes

    :white_check_mark: Avoids needing to copy over any custom configuration and settings

    :white_check_mark: When successful, will be faster than parallel upgrades


-   :material-cloud-sync:{ .lg .middle } __Negatives__

    :warning: Much harder to roll back the upgrade if something goes wrong

    :warning: Higher risk of downtime and service disruption, especially when dependency upgrades are involved

    :warning: Once you start, it's hard to turn back

    :warning: Cannot test the upgrade in parallel. Once you start, you'll be incurring downtime

</div>
