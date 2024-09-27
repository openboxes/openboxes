# Parallel Upgrade Instructions

A parallel upgrade is when you create a totally new installation of the app that exists alongside the old one.

!!! tip

    If you would like advice or support, please feel free to reach out to us on our
    [Community discussion forum](https://community.openboxes.com).


## Upgrade Overview

Parallel upgrades require you to complete the following steps:

* [ ] 1. Install the new version of the app somewhere
* [ ] 2. Copy custom configuration from the old installation to the new one
* [ ] 3. Take a database backup from the old installation (for testing purposes)
* [ ] 4. Import the database backup to the new installation
* [ ] 5. Start the new server
* [ ] 6. Run checks to verify the new server is working
* [ ] 7. Bring down both the old and new servers
* [ ] 8. Import a new database backup to the new server and re-test
* [ ] 9. Route requests to the new installation
* [ ] 10. Deprovision the old instance


## Step-by-step Instructions


### 1. Install New Server

Follow the [installation guide](../../installation/index.md) to install the new version of the application
inside a newly provisioned machine or VM/container.

!!! tip

    The advantage of a parallel upgrade over an in-place one is most reflected in this step. You skip having to
    manage upgrading all of your dependencies and can instead install them from scratch, bringing your setup to a more
    knowable state.


### 2. Copy Custom Configuration

If you've ever made configuration changes to the app or its dependencies over the years, you'll need to ensure that you
migrate all those changes to the new installation if you want them to take effect as well.

{% include 'admin-guide/common/_determining_custom_config.md' %}


### 3. Backup Database

Take a backup of the old database. Of course this is useful as a safety measure, but for parallel upgrades we mainly
do this so that we can test our database migrations on the new installation with semi accurate data. We'll take another
backup later once the old server is stopped for the purpose of actually restoring the database with up-to-date data.

{% include 'admin-guide/common/_backup_database.md' %}


### 4. Import Database Backup To New Server

Now take that backup and upload it to the database in your new installation.

{% include 'admin-guide/common/_restore_database_from_backup.md' %}


### 5. Start New Server

With all data and dependencies properly configured, all we need to do now is start the server.

{% include 'admin-guide/common/_start_server.md' %}


### 6. Test the New Server

With the server started, it's time to verify that the installation worked.

{% include 'admin-guide/common/_verifying_installation.md' %}


### 7. Stop New and Old Servers

Now that you've confirmed the upgrade has succeeded, we need to restore the database once again from a new backup
so that it contains the latest data. To ensure no further data is created while this process us taking place, we
must stop the servers at this point. Make sure to stop both the old **and** the new server.

!!! note
    This is where your downtime begins.

{% include 'admin-guide/common/_stop_server.md' %}


### 8. Import Backup Database (again) and re-test

With the old server stopped, we can safely take an up-to-date backup of the old database. Repeat step 3-6 to take
another database backup from the old installation, import it to the new installation's database, start the server, and
retest that the migration succeeds.


### 9. Route Requests To the New Installation

With your new server ready, you'll likely need to update your DNS settings to point to the new installation so that
new user requests go to the right server.

!!! note
    This is where your downtime should end.


### 10. Deprovision Old Instance (optional)

Now that you're fully running on the new server, you likely want to decommission the old one to save costs or resources.
Once you feel confident about the new installation, you can deprovision the old instance or stop the container or VM.
