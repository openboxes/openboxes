# Installing OpenBoxes system
With the upgrade of OpenBoxes to Grails 3 the suggested way of hosting the application is to run embedded Tomcat in a WAR file.

### Download OpenBoxes (Grails 3) WAR
```
sudo mkdir -p /opt/openboxes/.grails
cd /opt/openboxes/
sudo wget http://bamboo.pih-emr.org:8085/browse/OPENBOXES-SDOD2/latest/artifact/shared/Latest-WAR/openboxes.war
```

### Create openboxes.yml file using heredoc
Change `your_mysql_password` (and db user if it's not a default) with credentials set in [MySQL](/installation/ubuntu1804-grails3/mysql)
```
cat <<-EOT > /tmp/openboxes.yml
dataSource.dbCreate: none 
dataSource.url: jdbc:mysql://localhost:3306/openboxes?useSSL=false
dataSource.username: openboxes
dataSource.password: <your_mysql_password>
openboxes.jobs.calculateQuantityJob.cronExpression: "0 0 0 * * ?"
openboxes.anonymize.enabled: false
EOT
```

### Copy the openboxes.yml with sudo

```
sudo mv /tmp/openboxes.yml /opt/openboxes/.grails/openboxes.yml
```

### Create openboxes user, group and change permissions
```
sudo groupadd openboxes
sudo useradd -s /bin/false -g openboxes -d /opt/openboxes openboxes 
sudo chown -R openboxes:openboxes /opt/openboxes
```

### Create systemd service with heredoc 
```
sudo bash -c 'cat <<-EOT > /etc/systemd/system/openboxes.service
[Unit]
Description=OpenBoxes app
After=syslog.target

[Service]
User=openboxes
WorkingDirectory=/opt/openboxes
ExecStart=/usr/bin/java -Dgrails.env=prod -jar /opt/openboxes/openboxes.war
SuccessExitStatus=143
RestartSec=10
Restart=always

Environment="CATALINA_OPTS=-Xms1024m -Xmx1024m -XX:MaxPermSize=128m -server -XX:+UseParallelGC"
Environment="JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"

[Install]
WantedBy=multi-user.target
EOT'
```

### Reload systemd
```
sudo systemctl daemon-reload
```

### Run openboxes on startup
```
sudo systemctl enable openboxes
```

!!! note
    You will likely encounter OutOfMemoryErrors with Tomcat's default memory settings.  
    
    You may be able to get away with using 256m as the max heap size, but 512m is a good setting, 
    even for production environments.  
    
If you are in a limited memory environment (like an EC2 t2.micro which only has 1GB of memory) you 
will need to reduce your memory settings a little more. 
```
Environment='CATALINA_OPTS=-Xms128m -Xmx256m -XX:MaxPermSize=128m -Djava.security.egd=file:/dev/./urandom -server -XX:+UseParallelGC'
```

Unfortunately, with so little memory allocated you will probably run into several types of OutOfMemoryError issues 
(see Troublshooting section below).

### Systemctl commands
```
systemctl start tomcat
systemctl stop tomcat
systemctl status tomcat
```

### Service wrapper
At this point I generally go back to using the Ubuntu's `service` wrapper which abstracts the underlying implementation 
(could be `/etc/init.d`, Upstart, or `systemctl`). But it's up to you whether you want to continue using `systemctl` 
or switch back to `service`.

Here are the commands available if using the `service` wrapper:
```
sudo service openboxes status
sudo service openboxes stop
sudo service openboxes start
sudo service openboxes restart
```