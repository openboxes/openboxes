=== "mysqldump"

    If you've granted external access to the database, the easiest way to backup the database would
    be to use mysqldump from your local machine.

        mysqldump -h <database-server-ip> openboxes > openboxes.sql

    For example:

        mysqldump -h openboxes.server.com openboxes > openboxes.sql

    Or if you don't have your credentials configured in ~/.my.cnf

        mysqldump -u root -p -h openboxes.server.com openboxes > openboxes.sql

=== "SSH / Secure Shell"

    A more secure way to perform the backup (and this could be basis for a cron job).

    #### Connect to database server

        ssh <database-server-ip>

    For example:

        ssh openboxes.server.com

    #### Generate a backup 
    
        mysqldump openboxes > openboxes.sql 

    Or if you don't have your credentials configured in ~/.my.cnf

        mysqldump -u root -p openboxes > openboxes.sql 
    
    #### Copy the backup to somewhere safe

        scp openboxes.sql <backup-server-ip>:<backup-directory>

    For example:

        scp openboxes.sql backup.server.com:/backup/directory/openboxes.sql

=== "Cron"

    You might elect to convert your backup script to a cron job that will run on regular intervals to take periodic
    backups.

    In that case, you can simply wait for the cron to run, or trigger it manually to take a new backup.

    !!! tip

        When configuring your cron job, it is recommended that you specify the credentials in ~/.my.cnf so that you
        don't need to enter them (in plain text) in the commands of your cron scripts. See the following regarding 
        [MySQL client passwordless access](https://serverfault.com/questions/358903/store-the-mysql-client-password-or-setup-password-less-authentication)
