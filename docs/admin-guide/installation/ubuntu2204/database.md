
## Create User
```
sudo mysql -e "CREATE USER '<username>'@'localhost' IDENTIFIED BY '<password>'"
```

## Create Database
```
sudo mysql -e "CREATE DATABASE <database> default charset utf8;"
```

## Grant Permissions
```
sudo mysql -e "GRANT ALL on <database>.* to <username>@localhost;"
```

!!! important
    For security reasons, you will want to set a good password.  These values should be pasted into 
    the `dataSource.username` and `dataSource.password` configuration properties in 
    `openboxes-config.properties` during the Configuration Application step.


## Check Access
You can now authenticate as the `openboxes` user and execute commands. 
```
mysql -u <username> -p -e 'status'
Enter password: *********
```
The `status` command will send back useful status information about your current connection.
```
--------------
mysql  Ver 8.0.40-0ubuntu0.22.04.1 for Linux on x86_64 ((Ubuntu))

Connection id:		20
Current database:	
Current user:		openboxes@localhost
SSL:			Not in use
Current pager:		stdout
Using outfile:		''
Using delimiter:	;
Server version:		8.0.40-0ubuntu0.22.04.1 (Ubuntu)
Protocol version:	10
Connection:		Localhost via UNIX socket
Server characterset:	utf8mb4
Db     characterset:	utf8mb4
Client characterset:	utf8mb4
Conn.  characterset:	utf8mb4
UNIX socket:		/var/run/mysqld/mysqld.sock
Binary data as:		Hexadecimal
Uptime:			9 hours 23 min 46 sec

Threads: 2  Questions: 42  Slow queries: 0  Opens: 219  Flush tables: 3  Open tables: 138  Queries per second avg: 0.001
--------------
```

## Configure MySQL Server

### Allow external connections [optional]
If you need to allow external connections to MySQL then you'll need to edit the bind-address mysqld configuration. 
This is a security risk so please ensure that you set passwords and grant permissions in a way that does not leave
you vulnerable to attacks.


```title="/etc/mysql/mysql.conf.d/mysqld.cnf"
# Instead of skip-networking the default is now to listen only on
# localhost which is more compatible and is not less secure.
#bind-address           = 127.0.0.1
bind-address            = 0.0.0.0
```

!!! danger 
    Allowing external connections is not recommended for production environments. If you absolutely 
    need to enable external database connections, please remember to lock down your system as much 
    as possible (i.e. assign a secure root password, enable firewall rules to prevent access 
    from unauthorized clients, etc). 

### Restart MySQL
```
sudo service mysql restart
```
