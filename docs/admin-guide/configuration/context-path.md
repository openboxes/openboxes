
Defines the context path for the application.

## Configuration
```shell

server:
  contextPath: "/openboxes"
```

## Customization
If the application is deployed at the root level, set the context path to "/". This usually also requires 
changing the name of the WAR file from `openboxes.war` to `ROOT.war` before deploying to the Tomcat 
webapps directory.
```
server:
  contextPath: "/"
```
If running behind a proxy or load balancer, ensure the context path aligns with the external 
URL structure, as well as with the `grails.serverURL`.

## Related

* [Howto change the application context path](https://community.openboxes.com/t/howto-change-the-application-context-path-openboxes/758)

