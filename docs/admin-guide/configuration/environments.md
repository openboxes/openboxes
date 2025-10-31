## Environment-Specific Configuration
Grails supports environment-specific configurations, which allows different settings for 
`development`, `test`, and `production`.

**Example: Environment-Specific Server URL**
```yaml
environments:
    development:
        grails:
            serverURL: http://localhost:8080

    production:
        grails:
            serverURL: http://www.example.com
```
