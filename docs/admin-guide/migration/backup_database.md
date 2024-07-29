Before starting the migration process, ensure to take a full backup of your existing Tomcat 7 and 
MySQL 5.7 databases. This backup will serve as a safety net in case anything goes wrong during 
the migration process.

=== "mysqldump"

    !!! tip 
        You can use .my.cnf to store your credentails locally to avoid having to enter them 
        on the command line.

    If you've granted external access to the database, the easiest way to backup the database would
    be to use mysqldump from your local machine. 
 
    #### Generate database backup
    
        mysqldump -u root -p -h openboxes.server.com openboxes > openboxes.sql
    

=== "Secure Shell"

    A more secure way to perform the backup (and this could be basis for a cron job 

    #### Connect to database server
    
        ssh openboxes.server.com
    
    #### Generate a backup 
    
        mysqldump -u root -p openboxes > openboxes.sql 
    
    #### Copy backup to your favorite 

        scp openboxes.sql backup.server.com:/backup/directory/openboxes.sql


=== "Cron"

    If you already have a cron job working to backup the database, it's probably safe to use one 
    of these backups. Just make sure you can restore the database. 

    It is recommended that you specify the credentials in .my.cnf so you don't need to enter
    them in the command. See the following Stack Exchange regarding 
    [MySQL client passwordless access](https://serverfault.com/questions/358903/store-the-mysql-client-password-or-setup-password-less-authentication)

    
