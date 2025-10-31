
Configure SMTP settings for sending outbound emails from the application.

## Configuration

Outbound email is disabled by default. 

```
grails:
    mail:
        from: info@openboxes.com
        prefix: "[OpenBoxes]"
        host: localhost
        port: 25

        # SMTP is disabled by default 
        enabled: false

        # Authentication disabled by default
        username: null
        password: null

        # Disable debug mode by default
        debug: false
```

## Customization

There are many ways to configure an SMTP server. We haven't tested all of these approaches 
recently but we use a transactional email service in 

### Using a transactional email service

Add the following properties if you want to use a service like Mailgun, Sendgrid, or Amazon SES. 
as your SMTP server.

```yaml
grails:
    mail:
        enabled: true
        debug: false
        from: <default-from-email-address>
        host: smtp.example.com
        port: 587
        username: your-email@example.com
        password: your-email-password
```

### Using Gmail

```yaml
grails:
    mail:
        enabled: true
        debug: true
        from: <from-email>
        host: smtp.gmail.com
        port: 465
        username: <your-username>
        password: <password-generated-from-google-accounts>
        props:
            mail:
                smtp: 
                    auth: true
                    socketFactory: 
                        port: 465
                        class: javax.net.ssl.SSLSocketFactory
                        fallback: false
```

!!! note

    I have never been able to able to get the Gmail configuration to work, but I'm sure someone 
    with more time and intelligence will have no trouble figuring it out.

### Using local insecure SMTP server (not recommended)

This approach requires you to manage an SMTP server on your virtual machine. This is not 
recommended due to the amount of overhead required to manage an SMTP server (security
SPF, DKIM, DMARC)

To configure an SMTP server on Ubuntu 22.04, you typically install Postfix, a widely used mail 
transfer agent (MTA).

```yaml
grails:
    mail:
        enabled: true
        host: localhost
        port: 25
```

!!! danger 
    Running an open SMTP server can make you a target for spam and abuse. Misconfigurations can 
    lead to an open relay, allowing spammers to use your server.


## Advanced

### Using SMTP Authentication / StartTLS
This configuration is used to demonstrate the ability to set additional properties to enable
security features. Refer to your email provider for more details on what security features
it supports. 

```yaml
grails:
    mail:
        enabled: true
        debug: true
        host: smtp.example.com
        port: 587
        username: your-email@example.com
        password: your-email-password
        props:
            mail.smtp.auth: true
            mail.smtp.starttls.enable: true
            mail.smtp.starttls.required: true
```

* Use `mail.smtp.starttls.enable: true` for TLS (port 587) 
* Use `mail.smtp.ssl.enable: true` for SSL (port 465).
