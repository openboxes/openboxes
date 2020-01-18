# ajp

## Configure Tomcat to listen for requests from Apache

1. Make sure that the following line in server.xml is uncommented.
2. If you needed to uncomment it, save the file, and then restart Tomcat for the changes to take effect.

   ```text
    sudo service tomcat restart
   ```

## Configure JK module for Apache

1. Install mod\_jk module

   ```text
    sudo apt-get install libapache2-mod-jk
   ```

2. Enable mod\_jk

   ```text
    sudo a2enmod jk
   ```

3. Configure mod\_jk workers.properties _/etc/libapache2-mod-jk/workers.properties_

   ```text
    workers.tomcat_home=/opt/tomcat
    workers.java_home=/usr/lib/jvm/zulu-7-amd64/jre
   ```

## To delegate requests from Apache to Tomcat

1. Add the following line to your apache configuration in sites-enabled.

   ```text
    JkMount /manager* ajp13_worker
    JkMount /openboxes* ajp13_worker
   ```

2. To redirect requests from the root context to /openboxes \(optional, but recommended\)

   ```text
    RedirectMatch ^/$ /openboxes/
   ```

3. Restart Apache

   ```text
    sudo service apache2 restart
   ```

!!! note If you've enabled HTTPS already, you may need to edit multiple files.

* /etc/apache2/sites-enabled/000-default.conf
* /etc/apache2/sites-enabled/000-default-le-ssl.conf

