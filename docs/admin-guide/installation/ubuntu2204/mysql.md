
## Install MySQL
Install the MySQL server and client from the APT repository. 

```shell
sudo apt install mysql-server
```

## Check Version
Check to make sure the correct version (8.0.x) has been installed. 

```shell
sudo mysql --version
mysql  Ver 8.0.40-0ubuntu0.22.04.1 for Linux on x86_64 ((Ubuntu))
```

[//]: # (!!! note "Important")

[//]: # (    Remember the password you enter for the `root` user as you will need that password to create)

[//]: # (    the database and grant permissions in the following steps. If `mysql-server` was already )

[//]: # (    installed then the root password is probably blank )

[//]: # (    &#40;just hit `<Enter>` when prompted for a password&#41;.)
