
!!! reminder

    Document step for copying configuration for migration process.

This approach allows you to set up a new server environment with the updated dependencies, migrate 
the existing database to this new environment, and apply the application upgrade to this new server. 
The main advantage of this approach is that can perform the migration on your own timeline with 
significantly less downtime and dependency and configuration intermingling.

## Considerations
* :white_check_mark: Reduced risk of downtime
* :white_check_mark: Easier rollback to the old server if issues arise
* :white_check_mark: Opportunity to test the new environment thoroughly before switching over
* :warning: Requires more resources to maintain two environments temporarily
* :warning: Can be more time-consuming and complex to set up

## Procedure

### Basic Steps
* [ ] Backup Database: Backup your database. Copy the backup somewhere safe.
* [ ] Provision Resources: Provision virtual machines for the new environment.
* [ ] Configure Server(s): Configure a new server environment with the necessary updates and dependencies.
* [ ] Migrate Configuration: Migrate your existing configuration files to the new server.
* [ ] Migrate Database: Migrate database to new server.
* [ ] Deploy Application: Deploy the updated application on the new server.
* [ ] Verify Deployment: Check whether the server boots up properly and that there are no errors in database migrations.
* [ ] Testing: Conduct extensive testing to ensure the application works correctly in the new environment.
* [ ] Migrate Database: Migrate the database to the new server again (in case new data was stored since previous migration)
* [ ] Switch Over: Redirect traffic to the new server once testing is successful.
* [ ] Monitoring: Monitor the new server for any issues and validate that everything is functioning as expected.

## Step-by-step Instructions


### Backup Database

{% include 'admin-guide/migration/backup_database.md' %}

### Setup New Server

#### Provision Server(s)

Use the latest Installation Guide to provision your server(s). 

#### Install Dependencies

Use the latest Installation Guide to install dependencies.

#### Configure Server(s)

Use the latest Configuration Guide to configure your server(s).

### Migrate Database

#### Copy backup database to newly provisioned database server

    scp openboxes.sql openboxes.server.com


### Deploy Application 



### Testing


#### 

### Switch Over

* Schedule Switch-Over: Plan the final switch-over during a low-traffic period to minimize impact.
* DNS Update: Update DNS settings to point to the new server.
* Final Synchronization: Perform a final data synchronization just before the switch-over.
* Monitor: Monitor the new server and application closely after the switch-over for any issues.

#### Disable Production Environment

    ssh openboxes.server.com
    sudo service tomcat stop 
