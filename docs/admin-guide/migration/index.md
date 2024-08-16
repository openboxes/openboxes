# Upgrading from 0.8.x to 0.9.x

## Introduction
Generally speaking, upgrading a version of OpenBoxes (or any application packaed as a Java WAR file) 
is a relatively simple process: 

* [ ] Backup your database
* [ ] Download the latest version of the application
* [ ] Copy the .war file into the Tomcat webapps directory 
* [ ] Restart Tomcat
* [ ] Tail the logs to make sure database migrations are executed properly, and you're done.

In previous version of OpenBoxes (v0.8.x and earlier) releases were tied to a static tech stack 
with very specific requirements. Only the operating system changed over time because 
This upcoming 0.9.x release(s) support a bit more variability.

| Dependency         | 0.8.x Supported Versions | 0.9.x Supported Versions   |
|:-------------------|:-------------------------|----------------------------|
| Operating System   | Ubuntu 18.04             | Ubuntu 22.04               |
| Java               | Java 7                   | Java 8                     |
| Application Server | Tomcat 7                 | Tomcat 8.5, **Tomcat 9**   |
| Database           | MySQL 5.7                | **MySQL 8**, MariaDB 10.11 |
| Web Server         | Apache 2                 | **Apache 2.2**, nginx 1.23 |


| Dependency  | 0.8.x Supported Versions | 0.9.x Supported Versions   |
|:------------|:-------------------------|----------------------------|
| Grails      | Grail 1.3.x              | Grails 3.3.x               |
| Spring      | Grail 1.3.x              | Grails 3.3.x               |
| Spring Boot | Not Applicable           | Grails 3.3.x               |
| Hibernate   | Grail 1.3.x              | Grails 3.3.x               |


With the latest releases (0.9.x) the technical dependencies have changed, so you'll need to upgrade 
those dependencies before proceeding with the application upgrade. In general, this process is also 
fairly straightforward. But given the number of moving parts involved with dependencies and their 
configuration, there are more many opportunities for errors, some of which we cannot foresee. 
Therefore, we recommend that you create your own migration plan and include a mitigation and 
rollback strategy in the case that the upgrade fails. 

## Differences between in-place and parallel migration paths
There are primarily two approaches available for migrating. 

### Upgrading in Parallel (recommended)
A parallel migration is almost always recommended. This requires you to 

* [ ] Backup your database
* [ ] Provision new server environment (Ubuntu 22.04)
* [ ] Install dependencies (MySQL 8, Tomcat 9)
* [ ] Migrate database 
* [ ] Deploy latest version of OpenBoxes (v0.9.x)
* [ ] Validate the new environment
* [ ] Switch over (usually requires a DNS change)

### Upgrading In-Place
Upgrading in-place would 

* [ ] Backup your database
* [ ] Upgrade dependencies (MySQL 8, Tomcat 9)
* [ ] Deploy latest version of OpenBoxes (v0.9.x)
* [ ] Configure web server (load balancer) to direct traffic to Tomcat 9 
* [ ] Validate the new environment
* [ ] Remove old software dependencies (optional)

!!! tip
    
    If you don't feel comfortable completing the migration process on your own, you can request 
    assistance from the OpenBoxes support team.

### Decision Factors

* Server Accessibility: The ability to provision resources on the hosting provider may not be available 
* Cost : (even for a short period of time) may incur additional costs that are prohibitive.
* Downtime Tolerance: If downtime is a significant concern, a parallel migration might be safer.
* Testing and Rollback: A new server deployment allows for better testing and easier rollback options.
* Resource Availability: In-place upgrades require fewer resources but can be riskier in terms of service disruption.
* Complexity of Dependencies: If the dependencies are numerous or complex, creating a new server might simplify the upgrade process.



### Next Steps
// todo turn these into links 

* If you are upgrading to a new version in the latest release line (0.8.x to 0.9.x)
* If you are upgrading a version within the 0.8.x release line (e.g. 0.8.20 to 0.8.23)
* If you are upgrading a version within the 0.9.x release line (e.g. 0.9.1 to 0.9.2)
* If you are upgrading to a version (0.7.x to 0.8.x)


Include a link for every migration we know about, including configuration changes, API changes, breaking changes.

