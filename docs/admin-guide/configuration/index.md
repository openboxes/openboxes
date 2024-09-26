
# Overview

* [ ] Configuration File Locations (legacy vs new)
* [ ] Configuration File Precedence
* [ ] Configuration File formats (yml, json, properties, groovy)
* [ ] Override configuration locations using system variables 
* [ ] Override configuration locations using environment variables
* [ ] List all known configuration properties 
* [ ] Link to other configuration property documentation (Grails, Hibernate, etc)
* [ ] messages.properties?
* [ ] Configure context path (/openboxes, /)

## Configuration Methods

* Default Configuration 

* External Config Files (recommended)

* System Variables (experimental)

* Environment Variables (experimental)


## Configuration Properties

!!! todo
    Should probably be a table but let's see if there are good overviews out there. 





# Configuration Guide
Below is a configuration guide for a Grails application, covering how to set configurations, an overview of configuration properties, and some common settings you might want to configure.

## Introduction
This guide provides detailed instructions for system administrators on configuring OpenBoxes for deployment in a production environment. It covers the use of external configuration files, system properties, and environment variables to ensure a secure and scalable deployment.

The purpose of this guide is to assist system administrators in properly configuring OpenBoxes for production use, ensuring the application is secure, reliable, and performs optimally.

## Prerequisites
- Familiarity with Grails and Groovy.
- Access to the Grails application source code.
- Basic understanding of application deployment environments.

## 2. Overview of Grails Configuration

Grails uses a configuration file called `application.yml` (formerly, Config.groovy) to manage application settings. These configurations include database connections, server settings, environment-specific configurations, and more.

### 2.1 Configuration Files
- **`application.yml`**: The main configuration file located in the `grails-app/conf` directory.
- **Environment-Specific Configurations**: You can define environment-specific configurations within the `application.yml` file or use separate configuration files such as `application-development.yml`, `application-production.yml`, etc.

## 3. Setting Configuration Properties

### 3.1 Editing `application.yml`
The `application.yml` file uses a YAML format, which is hierarchical and structured by indentation.

**Example `application.yml` Structure:**
```yaml
grails:
    profile: web
    codegen:
        defaultPackage: com.example
    gorm:
        failOnError: true

dataSource:
    pooled: true
    jmxExport: true
    driverClassName: org.h2.Driver
    username: sa
    password: ''
    dbCreate: update
    url: jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE

environments:
    development:
        grails:
            serverURL: http://localhost:8080
        dataSource:
            dbCreate: create-drop
            url: jdbc:h2:mem:devDb

    production:
        grails:
            serverURL: http://www.example.com
        dataSource:
            dbCreate: update
            url: jdbc:mysql://localhost/prodDb
            properties:
                jmxEnabled: true
                initialSize: 5
                maxActive: 50
                minIdle: 5
                maxIdle: 25
                maxWait: 10000
                maxAge: 10 * 60000
                timeBetweenEvictionRunsMillis: 5000
                minEvictableIdleTimeMillis: 60000
                validationQuery: SELECT 1
```

### 3.2 Configuring Properties
To set a configuration property, define it under the relevant section in `application.yml`.

**Example: Setting the Server URL**
```yaml
grails:
    serverURL: http://www.example.com
```

### 3.3 Environment-Specific Configuration
Grails supports environment-specific configurations, which allows different settings for `development`, `test`, and `production`.

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

## 4. Common Configuration Properties

### 4.1 Database Configuration
Configure the data source for different environments:
```yaml
dataSource:
    driverClassName: org.postgresql.Driver
    username: dbuser
    password: dbpassword

environments:
    development:
        dataSource:
            dbCreate: update
            url: jdbc:postgresql://localhost:5432/devDb

    production:
        dataSource:
            dbCreate: update
            url: jdbc:postgresql://localhost:5432/prodDb
```

### 4.2 Logging Configuration
Grails uses Logback for logging. You can configure logging in `logback.groovy` or directly in `application.yml`.

**Example: Basic Logging Configuration**
```yaml
logging:
    level:
        org:
            springframework: INFO
            grails: DEBUG
```

### 4.3 Mail Configuration
Configure email settings for sending emails from the application.
```yaml
grails:
    mail:
        host: smtp.example.com
        port: 587
        username: your-email@example.com
        password: your-email-password
        props:
            "mail.smtp.auth": "true"
            "mail.smtp.starttls.enable": "true"
```

### 4.4 Security Configuration
Configure security settings like password policies and authentication mechanisms.

**Example: Spring Security Configuration**
```yaml
grails:
    plugin:
        springsecurity:
            userLookup:
                userDomainClassName: 'com.example.User'
                authorityJoinClassName: 'com.example.UserRole'
            authority:
                className: 'com.example.Role'
            password:
                algorithm: bcrypt
```

### 4.5 GORM Configuration
Configure Grails Object Relational Mapping (GORM) settings.
```yaml
grails:
    gorm:
        failOnError: true
        default:
            constraints:
                '*'(nullable: true)
```

### 4.6 CORS Configuration
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

### 4.7 Internationalization (i18n)
Configure default language and message bundles:
```yaml
grails:
    i18n:
        defaultLocale: en
        messageBundles:
            - 'messages'
```

## 5. Advanced Configuration

### 5.1 Externalized Configuration
You can externalize configuration properties by placing them in an external file and loading them in `application.yml`.

**Example: Externalizing Configurations**
```yaml
grails:
    config:
        locations:
            - classpath:application-external.yml
```

### 5.2 Custom Configuration Properties
You can define custom properties in `application.yml` and access them in your application.

**Example: Custom Properties**
```yaml
app:
    name: My Grails Application
    version: 1.0.0
```

Accessing custom properties in Groovy:
```groovy
String appName = grailsApplication.config.app.name
```

## 6. Conclusion

This guide provides a comprehensive overview of how to configure a Grails application, covering key properties and their configurations. By understanding and using these configurations, you can tailor the behavior of your Grails application to meet specific needs across different environments.

For more detailed information, always refer to the [official Grails documentation](https://grails.org/documentation.html).

---

This guide offers a basic structure for configuring common and advanced settings in a Grails application. Depending on your specific project, you can add more detailed configurations or tailor the examples to suit your needs.

### Core
* Data Source 
* Application


### Email


### Scheduling

### Performance

### Monitoring 
