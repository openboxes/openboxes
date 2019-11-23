## Configure Tomcat to listen for requests from Apache

1. Make sure that the following line in server.xml is uncommented.

        <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />

1. If you needed to uncomment it, save the file, and then restart Tomcat for the changes to take effect.

        sudo service tomcat restart


## Configure JK module for Apache

1. Install mod_jk module

        sudo apt-get install libapache2-mod-jk


1. Enable mod_jk 

        sudo a2enmod jk

1. Configure mod_jk workers.properties */etc/libapache2-mod-jk/workers.properties*

        workers.tomcat_home=/opt/tomcat
        workers.java_home=/usr/lib/jvm/zulu-7-amd64/jre

## To delegate requests from Apache to Tomcat

1. Add the following line to your apache configuration in sites-enabled. 

        JkMount /manager* ajp13_worker
        JkMount /openboxes* ajp13_worker

1. To redirect requests from the root context to /openboxes (optional, but recommended)

        RedirectMatch ^/$ /openboxes/


1. Restart Apache

        sudo service apache2 restart


!!! note
    If you've enabled HTTPS already, you may need to edit multiple files. 
    
    - /etc/apache2/sites-enabled/000-default.conf
    - /etc/apache2/sites-enabled/000-default-le-ssl.conf

