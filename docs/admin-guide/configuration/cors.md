
## CORS Configuration
To enable Cross-Origin Resource Sharing (CORS):
```yaml
grails:
    cors:
        enabled: true
        mappings:
            '/**':
                allowedOrigins: '*'
                allowedMethods: 'GET,POST,PUT,DELETE,OPTIONS'
                allowedHeaders: '*'
```
