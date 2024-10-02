To verify that the application can start up correctly, the first thing to do (aside from visiting the site url to see
if it loads) is to check the logs for startup errors.

    sudo tail -f /var/lib/tomcat9/logs/catalina.out

While there, you can also verify that any new database migrations are being applied correctly.
