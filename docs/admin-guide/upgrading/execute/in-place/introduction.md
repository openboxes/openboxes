# In-Place Upgrade Instructions

An in-place upgrade is when you upgrade the application and its dependencies directly on the existing server.

If you'd like more information on in-place upgrades, refer to its section in the
["plan" upgrade step](../../plan/upgrade-strategies/in-place-upgrade-overview.md).

!!! warning

    In-place Upgrades have the potential to be destructive. If you're doing a major or minor upgrade
    (Ex: 0.8.x -> 0.9.x) or if you don't have a staging server to test the upgrade on, we **strongly** encourage you to
    follow the [parallel upgrade strategy](../parallel/introduction.md) instead.

    Additionally, if your upgrade has [version-specific instructions](../version-specific/index.md), please
    read through that version-specific upgrade documentation in its entirety before proceeding. It is likely that
    in-place upgrades are further discouraged in those scenarios.

!!! tip

    If you would like advice or support, please feel free to reach out to us on our
    [Community discussion forum](https://community.openboxes.com).


## Upgrade Overview

In-place upgrades require you to complete the following steps:

* [ ] 1. Backup your database and configuration
* [ ] 2. Upgrade dependencies
* [ ] 3. Upgrade the application to the new version
* [ ] 4. Start the server
* [ ] 5. Remove the old dependency versions

The subsequent pages outline step-by-step instructions for each of the above.
