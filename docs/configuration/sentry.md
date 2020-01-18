# sentry

## Configure Sentry

1. Edit **openboxes-config.properties** file
2. Add the following 

   properties and paste the **DSN \(Deprecated\)** value to `grails.plugins.raven.dsn` in the config file.

   ```text
   grails.plugins.raven.active = true
   grails.plugins.raven.dsn = http://<client-dsn-key>:<client-secret>@sentry.openboxes.com:9000/1
   ```

!!! note `.properties` files do not handle boolean values very well. So if you ever need to disable Sentry support in the future, comment out the `grails.plugins.raven.active` flag by adding a hash \(\#\) to the beginning of the line. You can also convert your `.properties` file to a `.groovy` file or simply add these lines to a new `.groovy`. For a `.groovy` file, the DSN value needs to be wrapped in double quotes \("\).

```text
#grails.plugins.raven.active = true
grails.plugins.raven.dsn = http://<client-dsn-key>:<client-secret>@sentry.openboxes.com:9000/1
```

## Getting a Sentry DSN

1. Go to [sentry.io](https://sentry.io) and click the **Get started** button. ![Signup for Sentry](https://github.com/openboxes/openboxes/tree/ce29e7cd11a8a01a369e191de532c747c20c6040/img/sentry-signup.png)
2. Create a new project ![Create Project](https://github.com/openboxes/openboxes/tree/ce29e7cd11a8a01a369e191de532c747c20c6040/img/sentry-create-project.png)
3. Go to Settings &gt; Projects &gt; My Project &gt; Client Keys \(DSN\) ![Create Sentry Account](https://github.com/openboxes/openboxes/tree/ce29e7cd11a8a01a369e191de532c747c20c6040/img/sentry-client-keys.png)
4. Copy DSN Deprecated

