# Create Database

## Instructions

### Step 0. Configure credentials (optional)

Create or edit the user configuration file (~/.my.cnf) and set the root credentials. 
```shell
[client]
user=root
password=<password>
```
!!!tip 
    This allows you to execute mysql commands without having to provide a password non-interactively 
    via the command-line arguments (`-u username`, `-p<password`) or interactively when prompted. Make 
    sure this file is readable by the owner.

---


### Step 1. Create database 
```shell
mysql -e 'create database <database> default charset utf8;'

```

### Step 2. Create database user
```shell
mysql -e 'create user <username>@localhost identified by "<password>"'
```

!!!tip 
    
    Remember the password you set here, so you can include it in the OpenBoxes configuration file.

### Step 3. Grant permissions 
```shell
mysql -e 'grant all on <database>.* to <username>@localhost;'
```

