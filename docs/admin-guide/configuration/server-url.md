Defines the default base URL for OpenBoxes. This should be updated based on the deployment environment.

## Configuration

```shell
grails:
  serverURL: http://localhost:8080/openboxes
```

## Customization

The default is fine if you're running OpenBoxes locally. When OpenBoxes is deployed on a server, 
you'll need to update this value to match the public IP address of the deployment.

```shell
grails:
  serverURL: http://your_vm_ip_address:8080/openboxes
```

In addition, if you follow our [Post Deployment](../../admin-guide/installation/ubuntu2204/post-deployment.md)
guide (i.e. using a custom domain, enabling SSL, and putting OpenBoxes behind a reverse proxy server) then
your `grails.serverURL` will need to match (i.e. http->https, remove port, add custom domain)

```shell
grails:
  serverURL: https://openboxes.example.com/openboxes
```

Finally, if you decide to change the [Context Path](context-path.md) (i.e. deploy OpenBoxes via 
the root context path) you'll also need to make sure you update the `grails.serverURL` to reflect 
the change.
```shell
grails:
  serverURL: https://custom.example.com/

```
