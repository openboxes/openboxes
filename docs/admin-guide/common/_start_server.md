=== "Using command line"

    #### 1. SSH into the server

        ssh <database-server-ip>

    For example:

        ssh app.openboxes.com

    #### 2. Start Tomcat 
    
        sudo service tomcat9 start

    Or if the server is already running:

        sudo service tomcat9 restart

    Note that this may take a while if there are lots of data migrations.
