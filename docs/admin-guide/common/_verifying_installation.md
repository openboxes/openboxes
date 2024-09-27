The first thing you should do when verifying an installation is to check the logs for startup errors.

    sudo tail -f /var/lib/tomcat9/logs/catalina.out

While there, you can also verify that any new database migrations are being applied correctly.
