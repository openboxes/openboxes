Hopefully you've compiled a list of all the custom configuration changes that you've made over time, but if not,
some common places to check include:

* [ ] MySQL
    * [ ] /etc/mysql/mysql.conf.d/mysql.cnf
    * [ ] /etc/mysql/mysql.conf.d/mysqld.cnf
* [ ] Tomcat
    * [ ] /opt/tomcat/bin/setenv.sh
    * [ ] /opt/tomcat/conf/server.xml
    * [ ] /etc/systemd/system/tomcat.service
* [ ] Apache
    *  [ ] /etc/apache2/sites-enabled/000-default-le-ssl.conf
* [ ] OpenBoxes
    * [ ] /opt/tomcat/.grails/openboxes-config.properties
    * [ ] /opt/tomcat/.grails.openboxes-config.groovy

!!! tip

    One way to remind yourself what you've changed on a particular server is to go through your Bash History to see
    what files have been changed.

    For example, the following command will show you all of the files you've opened with vi:

        $ history | grep vi
