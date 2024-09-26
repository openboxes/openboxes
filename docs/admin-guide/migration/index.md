## Upgrading 
Generally speaking, upgrading a version of OpenBoxes (or any application packaed as a Java WAR file) 
is a relatively simple process: 

* [ ] Backup your database
* [ ] Download the latest version of the application
* [ ] Copy the .war file into the Tomcat webapps directory 
* [ ] Restart Tomcat
* [ ] Tail the logs to make sure database migrations are executed properly

... and you're done.

## Upgrading from 0.8.x to 0.9.x

With the latest releases (0.9.x) the technical dependencies have changed, so you'll need to upgrade
those dependencies before proceeding with the application upgrade. In general, this process is also
fairly straightforward. But given the number of moving parts involved with dependencies and their
configuration, there are more many opportunities for errors, some of which we cannot foresee.
Therefore, we recommend that you create your own migration plan and include a mitigation and
rollback strategy in the case that the upgrade fails.










