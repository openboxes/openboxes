# Installing OpenBoxes on Ubuntu 14.04

## 1. Watch the Video

<div style="position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; max-width: 100%; height: auto;"><iframe src="//www.youtube.com/embed/TGC16JvbxiY?rel=0" frameborder="0" allowfullscreen style="position: absolute; top: 0; left: 0; width: 100%; height: 100%;"></iframe></div>

**NOTE:** If the video does not render properly above you can [watch it
 directly on YouTube](https://www.youtube.com/watch?v=TGC16JvbxiY).
         
## 2. Choose a cloud provider
Here are a few options for cheapish cloud hosting providers.

* [RimuHosting](https://rimuhosting.com/order/v2orderstart.jsp) - Customizable VPS (~20/month)
* [Amazon Web Services EC2](http://www.ec2instances.info/) - t2.small 2GB (~$20/month)
* [Google Compute Engine](https://cloud.google.com/compute/pricing) - g1-small 1.7GB (~$20/month)
* [Digital Ocean droplet](https://www.digitalocean.com/pricing/) - Droplet 2GB ($20/month)
* [Linode](https://www.linode.com/pricing) - Linode 2GB ($10/month)

NOTE: AWS has a free-tier that includes a free year of 750 hours per month for t2.micro EC2 instances (as well as other 
services). It's a great deal it if you're not going to be using OpenBoxes too heavily. Unfortunately, keeping a 
Java-based web application like OpenBoxes happy on a t2.micro (1GB of RAM) is not easy. You may need to reduce the heap 
size and perm generation memory allocated to Tomcat to something minimal (see step 5 for more details).

## 3. Install dependencies

### Required
* Ubuntu 14.04 LTS (your cloud provider should allow you to choose this as the base image for your server)
* Tomcat 7 (`sudo apt-get install tomcat7`)
* MySQL 5.5+ (`sudo apt-get install mysql-server`)
* Java 7 (`sudo apt-get install openjdk-7-jre`)

**IMPORTANT:** For the time being, you **MUST** use Java 7! The version of Grails that we're using does not support Java 8. We are working on upgrading to the latest version of Grails, but we're still several months away from completing that migration. 

### Optional dependencies
* SMTP Server (runs over `localhost:25` by default)
* Chrome Browser (currently using `Version 29.0.1547.57`)

## 4. Create database 
```
$ mysql -u root -p -e 'create database openboxes default charset utf8;'
```

**Grant permissions to new new database user**
```
mysql -u root -p -e 'grant all on openboxes.* to '<username>'@'localhost' identified by "<password>";'
```
NOTE: For security reasons, you will want to set a decent password.  These values should be used in the `dataSource.username` and `dataSource.password` configuration properties in `openboxes-config.properties`.

## 5. Configure application properties
Download the sample external configuration properties file ([openboxes-config.properties](https://github.com/openboxes/openboxes/blob/master/deploy/openboxes-config.properties)) and save it under `/usr/share/tomcat7/.grails/openboxes-config.properties`.

**REMINDER:** Change `dataSource.password` to the password you set in the `grant all` command above.

Here's another example of the openboxes-config.properties file:
```
# Database connection settings
dataSource.username=<username>
dataSource.password=<password>

# Example of a simple JDBC URL (not to be used in production)
dataSource.url=jdbc:mysql://localhost:3306/openboxes

# Example of a more complex JDBC URL (used in our ccurent production environment)
#dataSource.url=jdbc:mysql://localhost:3306/openboxes?autoReconnect=true&zeroDateTimeBehavior=convertToNull&sessionVariables=storage_engine=InnoDB

# Used primarily with g:link when absoluteUrl is true (e.g. links in emails)
grails.serverURL=http://localhost:8080/openboxes

# OpenBoxes mail settings - disabled by default (unless you set up an SMTP server)
#grails.mail.enabled=true
```

NOTE: Documentation for each available configuration will be provided in the Configuration section.

## 6. Configure Tomcat
You will likely encounter OutOfMemoryErrors with Tomcat's default memory settings.  Therefore, I usually add a file (`/usr/share/tomcat7/bin/setenv.sh`) that is invoked by the Tomcat startup script and is used to control the amount of memory allocated to your instance of Tomcat.

A basic `setenv.sh` script will look like this:  
```
export CATALINA_OPTS="$CATALINA_OPTS -server -Xms512m -Xmx1024m -XX:MaxPermSize=256m -Djava.security.egd=file:/dev/./urandom"
```
Make the script executable.
```
$ sudo chmod +x /usr/share/tomcat7/bin/setenv.sh 
```
You may be able to get away with using 256m as the max heap size, but 512m is a good setting, even for production environments.  Using more memory will allow you to cache more data, but does not always result in a better performing application.  So there's no need in getting carried away.  We've been using about 1024m in production for over a year and that suits us fine.    

If you are in a limited memory environment (like an EC2 t2.micro which only has 1GB of memory) you will need to tune these command line arguments a little more. 
```
export CATALINA_OPTS="$CATALINA_OPTS -Xms128m -Xmx256m -XX:MaxPermSize=128m -Djava.security.egd=file:/dev/./urandom"
```

Unfortunately you will probably run into several types of memory issues when running OpenBoxes in a short amount of memory. Here are a few examples to look out for.

### Java OutOfMemoryError
The following errors are related to the `-Xms` (min heap), `-Xmx` (max heap) , and `-XX:MaxPermSize=256m` (max perm gen space) memory settings. These errors indicate that the heap / permgen memory spaces are not allocated appropriately and/or there's a memory leak in the application. 

* Heap space (`OutOfMemoryError: Java heap space`)
* PermGen (`OutOfMemoryError: PermGen space`)

See [this article] (https://plumbr.eu/outofmemoryerror/java-heap-space) for a good description of the problem. Contact [support@openboxes.com](mailto:support@openboxes.com) if you have further questions.

### Out of Memory: Killed process 31088 (java)
In this case, the Linux kernel has killed your  Tomcat instance because it over stepped the OS bounds on memory. At this point, you may have increased the max heap size as much as you can. This probably means you need to upgrade to a larger instance type (i.e. as we mentioned above, an instance type that has 2GB of memory is a good start).

## 7. Deploy the application to Tomcat

### Stop tomcat
```
$ sudo service tomcat7 stop
```

### Download latest release

* Go to the  [latest release](https://github.com/openboxes/openboxes/releases/latest) page on GitHub.
* Download the WAR file (`openboxes.war`) associated with the latest release.

If you wanted to do this from the shell use wget with the following URL to get the latest WAR file.
```
$ wget https://github.com/openboxes/openboxes/releases/download/<version>/openboxes.war
```

### Copy WAR file to Tomcat
```
$ sudo cp openboxes.war /var/lib/tomcat7/webapps/openboxes.war
```

NOTE: If you'd like to deploy the application to the root context (to avoid having /openboxes) in every URL, you can copy the 
```
$ sudo cp openboxes.war /var/lib/tomcat7/webapps/ROOT.war
```

### Restart Tomcat
```
$ sudo service tomcat7 start
```

### Tail Tomcat logs

Keep an eye out for any errors/exceptions that pop up in the `catalina.out` log file.
```
$ tail -f /var/log/tomcat7/catalina.out
```


