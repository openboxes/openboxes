# In-Place Upgrade From 0.8.x to 0.9.x

!!! danger

    Because the upgrade from 0.8.x to 0.9.x requires updating core dependencies, unless you've successfully tested the
    upgrade on a staging server, **we strongly encourage you to use the [parallel upgrade strategy](parallel.md)**.
    Running the in-place upgrade strategy directly in production without testing it elsewhere beforehand is a large
    risk for this upgrade.


## Assumptions

This guide assumes that you have the following dependencies in your 0.8.x setup:

* Ubuntu 18.04 is the operating system
* Java 7 is installed
* Tomcat 7 is installed under /opt/tomcat
* tomcat user has been created
* MySQL 5.7 is installed
* Apache 2.2 is installed
* OpenBoxes v0.8.x is installed and running
* OpenBoxes v0.8.x configuration is stored under /opt/tomcat/.grails

!!! note

    If any of the above assumptions don't match your setup, know that you're entering into an unpredictable area.
    A parallel migration will help reduce risk of dependency upgrade conflicts, so if it's an option, we recommend it.

    If you *must* do an in-place upgrade, it is essential that you spend time reviewing your environments to ensure that
    you understand the specific migration steps that you will need to execute in order to upgrade the application.

    Given that these instructions can only officially support a migration plan for environments that match the above
    versions, we highly encourage you to design your own contingency/rollback plan for your specific setup.


## 1. Backup Database and Configuration

As with any upgrade, before you begin, make sure to follow the
["plan" phase](../../../plan/rollback-strategies/overview.md) take proper backups of your database and app and
dependency configurations. Given the complexity of this upgrade, it is very important to have backups that you
can roll back to in the case of failures.


## 2. Upgrade Dependencies

We've upgraded a number of dependencies with the release of 0.9.x, so you'll need to upgrade those dependencies
on your host machines before proceeding with the OpenBoxes application upgrade.

Previous releases of OpenBoxes (v0.8.x and earlier) were tied to a static tech stack with very specific requirements
(i.e. Java 7, Tomcat 7). 0.9.x releases support a bit more variability:

| Dependency         | 0.8.x Supported Versions | 0.9.x Supported Versions   |
|:-------------------|:-------------------------|----------------------------|
| Operating System   | Ubuntu 18.04             | Ubuntu 22.04               |
| Java               | Java 7                   | Java 8                     |
| Application Server | Tomcat 7                 | Tomcat 8.5, **Tomcat 9**   |
| Database           | MySQL 5.7                | **MySQL 8**, MariaDB 10.11 |
| Web Server         | Apache 2                 | **Apache 2.2**, nginx 1.23 |

Steps for each of the above upgrades are outlined below.


### Upgrading to Ubuntu 22.04

!!! warning

    If you are currently on an older distribution of Ubuntu (such as 14.x or 16.x) we would highly recommend against
    attempting an in-place upgrade and instead favoring a parallel upgrade.

If you are running a more recent LTS distribution such as Ubuntu 18.04 or 20.04, it might be possible to perform a
simple dist-upgrade to upgrade your distribution.

```
sudo dist-upgrade
```


### Upgrading to Java 8

=== "Zulu"

        sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9
        sudo apt-add-repository 'deb http://repos.azulsystems.com/ubuntu stable main' -y
        sudo apt-get update
        sudo apt-get install zulu-8 -y


### Upgrading to Tomcat 9 (or 8.5)

=== "Tomcat 9 (APT)"

        sudo apt install tomcat9 

=== "Tomcat 9 (Manual)"

        wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.91/bin/apache-tomcat-9.0.91.tar.gz
        tar xfv apache-tomcat-9.0.91.tar.gz
        sudo mv apache-tomcat-9.0.91 /opt/tomcat9
        sudo chown -R tomcat:tomcat /opt/tomcat9
        sudo chmod +x /opt/tomcat9/bin/*
        rm apache-tomcat-9.0.91.tar.gz

=== "Tomcat 8.5 (Manual)"

        wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.89/bin/apache-tomcat-8.5.89.tar.gz
        tar xfv apache-tomcat-8.5.89.tar.gz
        sudo mv apache-tomcat-8.5.89 /opt/tomcat85
        sudo chown -R tomcat:tomcat /opt/tomcat85
        sudo chmod +x /opt/tomcat85/bin/*
        rm apache-tomcat-8.5.89.tar.gz


### Upgrading to MySQL 8 or MariaDB 10

!!! note

    While it is possible to continue using MySQL 5.7, we will not officially support this version in 0.9.x and beyond.

Since we're recommending a parallel upgrade for 0.9.x, we will not be providing specific instructions on how to
upgrade from MySQL 5.7 to 8. If you would like to continue with an in-place upgrade, you can view
[MySQL's official upgrade instructions](https://dev.mysql.com/blog-archive/inplace-upgrade-from-mysql-5-7-to-mysql-8-0/).


### Upgrading to Apache 2.2 or nginx 1.23

!!! todo


### Web Server / HTTPS / SSL (optional)
This step is optional since you can stay with Apache 2, if that's what was installed previously. We
have started to migrate some of our production environments to use nginx, so that's why these
instructions are included.

=== "Apache"

    If you've configured SSL/HTTPS on Apache 2 then you can stay with that configuration. You might 
    need to re-configure Apache 2 to delegate requests to Tomcat 8.5 or Tomcat 9 through mod_jk or 
    whatever plugin was used. We'll try to upgrade the docs to include those instructions soon.
    
    !!! todo
    
            Include configuration changes for mod_jk or mod_http or whatever. 

=== "nginx"

    #### Install nginx apt
    
        sudo apt-get install nginx -y
    
    #### Configure SSL
    
        sudo touch /etc/nginx/conf.d/default.conf
        cat 9_nginx_config.template > /etc/nginx/conf.d/default.conf
        echo "Bruteforce creating missing files in the /etc/letsencrypt"
        curl https://raw.githubusercontent.com/certbot/certbot/master/certbot-nginx/certbot_nginx/_internal/tls_configs/options-ssl-nginx.conf
        curl https://raw.githubusercontent.com/certbot/certbot/master/certbot/certbot/ssl-dhparams.pem > /etc/letsencrypt/ssl-dhparams.pem
    
    #### Disable Apache2 
    
        service apache2 stop
        systemctl daemon-reload
        systemctl disable apache2
    
    #### Enable Nginx
    
        systemctl enable nginx
        service nginx start

=== "Tomcat"

    This documentation does not officially support enabling HTTPS/SSL on Tomcat, but there's
    nothing preventing you from using this feature.


## 3. Proceed With The App Upgrade

Once all dependencies have been successfully upgraded, you can
[follow the rest of the in-place upgrade documentation](../../in-place/upgrade-app.md) to proceed with the remainder
of the upgrade.
