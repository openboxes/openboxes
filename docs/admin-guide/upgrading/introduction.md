# Upgrading
Generally speaking, upgrading to a new version of OpenBoxes is a relatively simple process:

* [ ] Backup your database and custom configuration
* [ ] Download the latest version of the application
* [ ] Copy the .war file into the Tomcat webapps directory
* [ ] Restart Tomcat
* [ ] Tail the logs to make sure database migrations are executed properly

For a majority of patch upgrades (Ex: 0.9.1 -> 0.9.2), the above process is often enough. However, major and minor
upgrades (Ex: 0.8.x -> 0.9.x), especially those that require dependency upgrades, can be a more involved process.

While we always attempt to document the upgrade process as thoroughly as possible, it's important to note that
upgrading will always come with some amount of risk. Given the high variability in configuration from machine to machine,
it's impossible to predict the precise results that the upgrade will have on your setup. We recommend that you always
have your own migration plan and include a mitigation and rollback strategy in the case that the upgrade fails.
