# 5. Reimport Database Backup

Now that you've confirmed the upgrade has succeeded, we need to restore the database once again from a fresh backup
so that it contains the absolute latest data.


## Stop The Servers

To ensure no further data is created while the backup process us taking place, we must stop the servers at this point.
Make sure to stop both the old **and** the new application servers.

!!! note
    This is where your downtime begins.

{% include 'admin-guide/common/_stop_server.md' %}


## Restore Database Backup

With the old server stopped, we can safely take an up-to-date backup of the old database. Repeat steps 3 and 4 to take
another database backup from the old installation, import it to the new installation's database, start the server, and
retest that the migration succeeds.
