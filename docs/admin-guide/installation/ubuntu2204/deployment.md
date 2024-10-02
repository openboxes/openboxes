


## Instructions

### Step 1. Stop Tomcat service
```shell
sudo service tomcat9 stop
```

### Step 2. Download the latest stable release
As of August 2024, OpenBoxes 0.9.x is only available through our Bamboo CI/CD pipeline. This is a stable 
release of version 0.9.2 that has been thoroughly tested and deployed to production environments. However, 
we would like to publish a draft version of our [Upgrade Guide](../../upgrading/introduction.md) before making the 
official release available to the public via GitHub Releases.

=== "Nightly Build"

    Download the latest nightly build into the Tomcat webapps directory. 
    ```shell
    sudo cd /var/lib/tomcat9/webapps
    sudo wget https://bamboo-ci.pih-emr.org/browse/OPENBOXES-OBNR/latestSuccessful/artifact/G3JOB/Latest-WAR/openboxes.war
    ```

=== "Official Release"

    !!! important 
        Once we have officially released OpenBoxes 0.9.x to the community, the latest official release will be 
        available from our [GitHub Release](https://github.com/openboxes/openboxes/releases) page along with 
        Release Notes. 

    
### Step 3. Change ownership
```shell
chown -R tomcat:tomcat /var/lib/tomcat9/
```

### Step 4. Start Tomcat instance
```shell
sudo service tomcat9 start
```

### Step 5. Monitor Tomcat logs
The deployment should take about 5-10 minutes. The majority of that time will be spent executing the
database migrations (DDL statements) that create the OpenBoxes data model. 

We recommend that you monitor the Tomcat stdout logs during deployment in order to catch any unexpected errors.
You should keep an eye out for any exception stacktraces that occur in the logs, but be aware that some exceptions
are normal. Check the Troubleshooting section for details on how to handle these issues.
```
journalctl -u tomcat9.service -f
```
Or 
```shell
tail -f /var/lib/tomcat9/logs/catalina.out
```


!!! note
    If you run into an error that's not covered in the [Troubleshooting Guide](troubleshooting.md) don't hesitate to
    [contact support](../../../support/index.md).  

    If you post to our community forum or GitHub issues, please describe the problem in as much detail 
    as you can and include the catalina.out log from your Tomcat instance. 
