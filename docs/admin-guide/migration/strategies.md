
# Migration Strategies

## Migrating in Parallel 
A parallel migration is almost always recommended. This requires you to complete the following steps:

* [ ] Backup your database
* [ ] Provision new server environment (Ubuntu 22.04)
* [ ] Install dependencies (MySQL 8, Tomcat 9, Apache 2)
* [ ] Migrate database (restore from backup)
* [ ] Migrate configuration (restore existing config files to new server)
* [ ] Deploy latest version of OpenBoxes (v0.9.x)
* [ ] Validate the new environment
* [ ] Migrate database (backup and restore latest database)
* [ ] Switch over (usually requires a DNS change)

## Upgrading In-Place
Upgrading in-place would require the following steps.

* [ ] Backup your database
* [ ] Upgrade dependencies in place (MySQL 8, Tomcat 9)
* [ ] Migrate configuration (restore existing config files to new directory)
* [ ] Deploy latest version of OpenBoxes (v0.9.x)
* [ ] Configure web server (reverse proxy) to forward traffic to Tomcat 9
* [ ] Validate the new environment
* [ ] Remove old software dependencies (optional)

!!! tip

    If you don't feel comfortable completing the migration process on your own, you can request 
    assistance from the OpenBoxes support team.

