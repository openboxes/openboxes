# Ubuntu 22.04

## Preconditions

The following assumptions must be met before proceeding
* You must have a created an account with a hosting provider. 
* You must have provisioned a virtual machine within your hosting provider.
* You must have installed Ubuntu 22.04 on the aforementioned virtual machine.
* 
* 

## Step 1: Update System Packages
First, ensure that your system is up to date by running:
```shell
sudo apt update
sudo apt upgrade -y
```

## Step 2: Install Java
Install OpenJDK 8 with the following command

```shell
sudo apt install openjdk-8-jdk -y
```
Verify the installation:
```shell
java -version
```

## Step 3: Install Tomcat
```

```

## Step 3: Install MySQL 

### MySQL 8 (recommended)
Install MySQL server and client.

```shell
sudo apt install mysql-server-8.0
```


### Secure MySQL 
```shell
sudo mysql_secure_installation

```

```shell

```

## Step 4: Create Database 

Log into MySQL as root
```groovy
sudo mysql -u root -p
```


