# 3. Import Database Backup

If you haven't already done so as a part of the ["plan" phase](../../plan/rollback-strategies/backup-database.md),
take a backup of the old installation's database.

Of course this is useful as a safety measure, but for parallel upgrades we mainly do this so that we can test our
database migrations on the new installation with semi accurate data. We'll take another backup later once the old
server is stopped for the purpose of actually restoring the database with up-to-date data.

## Take Database Backup

{% include 'admin-guide/common/_backup_database.md' %}


## Restore Database Backup

Now that you have a backup of the old database, take that backup and upload it to the database in your new installation.

{% include 'admin-guide/common/_restore_database_from_backup.md' %}
