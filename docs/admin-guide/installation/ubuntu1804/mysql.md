# Installing MySQL 5.7+
Install MySQL from the APT repository. Finally, something easy!
```
sudo apt-get install mysql-server
```

!!! note "Important"
    Remember the password you enter for the `root` user as you will need that password to create
    the database and grant permissions in the following steps. If `mysql-server` was already 
    installed then the root password is probably blank 
    (just hit `<Enter>` when prompted for a password).

## Create database
```
$ mysql -u root -p -e 'create database openboxes default charset utf8;'
```

## Grant permissions to new database user
```
$ mysql -u root -p -e 'grant all on openboxes.* to openboxes@localhost identified by "<password>";'
```
!!! note
    For security reasons, you will want to set a good password.  These values should be used in the 
    `dataSource.username` and `dataSource.password` configuration properties in `openboxes-config.properties`.


## Login into MySQL
```
$ mysql -u openboxes -p openboxes
Enter password: <enter password from earlier>

```
## Execute a simple command
```
mysql> show tables;
Empty set (0.00 sec)
```

## Configure MySQL Server

### Allow external connections [optional]
If you need to allow external connections to MySQL then you'll need to edit the bind-address mysqld configuration. 
This is a security risk so please ensure that you set passwords and grant permissions in a way that does not leave
you vulnerable to attacks.

/etc/mysql/mysql.conf.d/mysqld.cnf
```
# Instead of skip-networking the default is now to listen only on
# localhost which is more compatible and is not less secure.
#bind-address           = 127.0.0.1
bind-address            = 0.0.0.0
```

!!! danger "Important"
    If you do enable external connections, please remember to change the root password!

### Restart MySQL
```
sudo service mysql restart
```
