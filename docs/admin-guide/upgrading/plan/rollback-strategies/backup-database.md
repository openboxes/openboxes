Before you start any upgrades on your application, we highly recommend that you take a backup of the current state
of your database. Upgrading to a new app version often comes with new database migrations, which will alter the state
of your database. If you need to roll back the upgrade for whatever reason, you'll want to make sure that you have a
backup of the database so that you can also roll back the data to its pre-migration state.

{% include 'admin-guide/common/_backup_database.md' %}
