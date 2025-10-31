

## Stop Tomcat service
We recommend stopping the Tomcat service to allow you to have full control over the deployment 
process and to avoid partial downloads causing deployment issues.
```shell
sudo service tomcat stop
```

## Download the latest stable release

Download the latest nightly build into the Tomcat webapps directory. 

!!! note 
    As of January 2025, OpenBoxes 0.9.x is only available through our Bamboo CI/CD pipeline. This is a stable 
    release of version 0.9.3 that has been thoroughly tested and deployed to production environments. However, 
    we would like to publish a draft version of our [Upgrade Guide](../../upgrading/introduction.md) before making the 
    official release available to the public via GitHub Releases.

=== "Nightly Build"

    ```shell
    sudo wget --directory-prefix=/opt/tomcat/webapps \
        https://bamboo-ci.pih-emr.org/browse/OPENBOXES-OBNR/latestSuccessful/artifact/G3JOB/Latest-WAR/openboxes.war
    ```

=== "Official Release"

    !!! important 
        Once we have officially released OpenBoxes 0.9.x to the community, the latest official release will be 
        available from our [GitHub Release](https://github.com/openboxes/openboxes/releases) page along with 
        Release Notes. 
 
## Change ownership
```shell
chown -R tomcat:tomcat /opt/tomcat
```

## Start Tomcat instance
```shell
sudo service tomcat start
```

## Monitor Tomcat logs
The deployment should take a few minutes (5-10 minutes at most). The majority of that time will be 
spent executing Liquibase database migrations (DDL statements) that create and update the 
OpenBoxes data model. 

We highly recommend that you monitor the Tomcat stdout logs during deployment in order to catch 
any unexpected errors. You should keep an eye out for any exception stacktraces that occur in the 
logs, but be aware that some exceptions are normal. Check the Troubleshooting section for details 
on how to handle these issues.

To tail the log 
```
journalctl -u tomcat.service -f
```
Or 
```shell
sudo tail -f /opt/tomcat/logs/catalina.out
```


!!! note
    If you run into an error that's not covered in the [Troubleshooting Guide](troubleshooting.md) don't hesitate to
    [contact support](../../../support/index.md).  

    If you post to our community forum or GitHub issues, please describe the problem in as much detail 
    as you can and include the catalina.out log from your Tomcat instance. 


