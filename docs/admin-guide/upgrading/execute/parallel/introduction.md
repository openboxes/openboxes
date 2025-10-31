# Parallel Upgrade Instructions

A parallel upgrade is when you create a totally new installation of the app that exists alongside the old one.

If you'd like more information on parallel upgrades, refer to its section in the
["plan" upgrade step](../../plan/upgrade-strategies/parallel-upgrade-overview.md).

!!! tip

    If you would like advice or support, please feel free to reach out to us on our
    [Community discussion forum](https://community.openboxes.com).


## Upgrade Overview

Parallel upgrades require you to complete the following steps:

* [ ] 1. Install the new version of the app somewhere
* [ ] 2. Copy custom configuration from the old installation to the new one
* [ ] 3. Import the database backup to the new installation (for testing purposes)
* [ ] 4. Start the new server
* [ ] 5. Import a new database backup to the new server and re-test
* [ ] 6. Route requests to the new installation
* [ ] 7. Deprovision the old instance (optional)

The subsequent pages outline step-by-step instructions for each of the above.
