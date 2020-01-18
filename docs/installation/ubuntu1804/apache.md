# Installing Apache 2 \(optional\)

The best way to enable HTTPS is to add a load balancer using your hosting provider. One of your next best options is to install Apache or Nginx locally. The nice thing about this option is that it's fairly straightforward and works very well. However, it's not really a load balancer since it's only fronting a single Tomcat instance.

```text
sudo apt-get install apache2
```

!!! note To enable request delegation from Apache to Tomcat, please see [Configuration Guide &gt; Tomcat AJP](https://github.com/openboxes/openboxes/tree/ce29e7cd11a8a01a369e191de532c747c20c6040/configuration/ajp/README.md).

!!! note To enable HTTPS, please see [Configuration Guide &gt; Apache HTTPS](https://github.com/openboxes/openboxes/tree/ce29e7cd11a8a01a369e191de532c747c20c6040/configuration/https/README.md).

