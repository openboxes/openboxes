## Logging Configuration
Grails uses Logback for logging. You can configure logging in `logback.groovy` or directly in 
`application.yml`.

**Example: Basic Logging Configuration**
```yaml
logging:
    level:
        org:
            springframework: INFO
            grails: DEBUG
```
