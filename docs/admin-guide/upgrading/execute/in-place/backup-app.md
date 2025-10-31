# 1. Backup Your Database and Configuration


## Backup The Database

Before starting the upgrade process, make sure to take a full backup of your existing databases. This backup will serve
as a safety net in case anything goes wrong during the upgrade process.

{% include 'admin-guide/common/_backup_database.md' %}


## Backup Custom Configuration

Similarly, if you've ever made configuration changes to the app or its dependencies over the years, you'll want to
take a backup of those changes in case you need to revert back to them if anything goes wrong during the upgrade.

{% include 'admin-guide/common/_determining_custom_config.md' %}
