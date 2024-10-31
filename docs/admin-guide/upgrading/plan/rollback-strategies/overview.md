While we always do our best to document the upgrade process as thoroughly as possible, it's important to note that
upgrading will always come with some amount of risk. Given the high variability in configuration from machine to
machine, it's impossible to predict the precise results that the upgrade will have on your setup.

As such, you want to make sure that you have a plan to roll back the upgrade to the original version in the case
where something fails. We strongly recommend the following rollback processes be followed before you begin any upgrade:

* [Backup your database](backup-database.md)
* [Backup your custom app and dependency configuration](backup-config.md)

!!! note
    Because each setup is unique, the above recommendations may not sufficiently cover every detail of rolling back
    your environments. We recommend that you evaluate your setup independently, and design your own upgrade and rollback
    strategy that adequately accounts for your specific use case.
