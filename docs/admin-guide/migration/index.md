# Upgrading from 0.8.x to 0.9.x

## Introduction
Generally speaking, upgrading a version of OpenBoxes (or any application packaed as a Java WAR file) 
is a relatively simple process: drop a new WAR file into the Tomcat webapps directory, 
restart the application server, watch the logs to make sure database migrations are executed 
properly, and you're done.

OpenBoxes 0.8.x releases were tied to the following tech stack. This upcoming 0.9.x release(s) support a bit more variability.

| Dependency         | 0.8.x Supported Versions | 0.9.x Supported Versions   |
|:-------------------|:-------------------------|----------------------------|
| Operating System   | Ubuntu 18.04             | Ubuntu 22.04               |
| Java               | Java 7                   | Java 8                     |
| Application Server | Tomcat 7                 | Tomcat 8.5, **Tomcat 9**   |
| Database           | MySQL 5.7                | **MySQL 8**, MariaDB 10.11 |
| Web Server         | Apache 2                 | **Apache 2.2**, nginx 1.23 |


With the latest releases (0.9.x) the technical dependencies have changed, so you'll need to upgrade 
those dependencies before proceeding with the application upgrade. In general, this process is also 
fairly straightforward. But given the number of moving parts involved with dependencies and their 
configuration, there are more many opportunities for errors, some of which we cannot foresee. 
Therefore, we recommend that you create your own migration plan and include a mitigation and 
rollback strategy in the case that the upgrade fails. 

## Approaches
There are primarily two approaches available for migrating. 

* **Parallel Upgrade**: Provision a new VM, install dependencies, migrate database 
* **In-Place Upgrade**: Upgrade dependencies on your existing VM

!!! tip
    
    If you don't feel comfortable completing the migration process on your own, you can request 
    assistance from the OpenBoxes support team.

## Decision Factors

* Downtime Tolerance: If downtime is a significant concern, a parallel migration might be safer.
* Testing and Rollback: A new server deployment allows for better testing and easier rollback options.
* Resource Availability: In-place upgrades require fewer resources but can be riskier in terms of service disruption.
* Complexity of Dependencies: If the dependencies are numerous or complex, creating a new server might simplify the upgrade process.



## Next Steps
// todo turn these into links 

* If you are upgrading to a new version in the latest release line (0.8.x to 0.9.x)
* If you are upgrading a version within the 0.8.x release line (e.g. 0.8.20 to 0.8.23)
* If you are upgrading a version within the 0.9.x release line (e.g. 0.9.1 to 0.9.2)
* If you are upgrading to a version (0.7.x to 0.8.x)


Include a link for every migration we know about, including configuration changes, API changes, breaking changes.

