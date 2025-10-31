
In Grails 3, health check endpoints are typically implemented using Spring Boot Actuator, 
which provides built-in endpoints for monitoring application health and metrics.

```shell
endpoints:
    enabled: true
    jmx:
        enabled: true

management:
    info:
        git:
            mode: full

```

```
{
  "status": "UP"
}
```

https://blog.mrhaki.com/2015/04/grails-goodness-adding-health-check.html


