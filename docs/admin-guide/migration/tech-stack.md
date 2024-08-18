
In previous versions of OpenBoxes (v0.8.x and earlier) releases were tied to a static tech stack
with very specific requirements (i.e. Java 7, Tomcat 7). 

The upcoming 0.9.x releases support a bit more variability.

| Dependency         | 0.8.x Supported Versions | 0.9.x Supported Versions   |
|:-------------------|:-------------------------|----------------------------|
| Operating System   | Ubuntu 18.04             | Ubuntu 22.04               |
| Java               | Java 7                   | Java 8                     |
| Application Server | Tomcat 7                 | Tomcat 8.5, **Tomcat 9**   |
| Database           | MySQL 5.7                | **MySQL 8**, MariaDB 10.11 |
| Web Server         | Apache 2                 | **Apache 2.2**, nginx 1.23 |

!!! note

    While it is possible to continue using MySQL 5.7, we will not officially 
    support this version in 0.9.x and beyound. Since we're recommending a Parallel
    migration process (i.e. provision a new server environment), the installation 
    instructions will target MySQL 8.x and MariaDB 10.x as supported database versions. 
