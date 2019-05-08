
### Configure Sentry 

1. Edit **openboxes-config.properties** file
1. Add the following 
properties and paste the **DSN (Deprecated)** value to `grails.plugins.raven.dsn` in the config file.
```
grails.plugins.raven.active = true
grails.plugins.raven.dsn = http://<client-dsn-key>:<client-secret>@sentry.openboxes.com:9000/1
```

!!! note 
    `.properties` files do not handle boolean values very well. So if you ever need to disable Sentry support in the 
    future, comment out the `grails.plugins.raven.active` flag by 
    adding a hash (#) to the beginning of the line. You can also convert your `.properties` file to a `.groovy` file
    or simply add these lines to a new `.groovy`. For a `.groovy` file, the DSN value needs to be wrapped in double quotes (").
```
#grails.plugins.raven.active = true
grails.plugins.raven.dsn = http://<client-dsn-key>:<client-secret>@sentry.openboxes.com:9000/1
```

### Getting a Sentry DSN

1. Go to [sentry.io](https://sentry.io) and click the **Get started** button.
![Signup for Sentry](/img/sentry-signup.png)

1. Create a new project
![Create Project](/img/sentry-create-project.png)

1. Go to Settings > Projects > My Project > Client Keys (DSN)
![Create Sentry Account](/img/sentry-client-keys.png)

1. Copy DSN Deprecated
