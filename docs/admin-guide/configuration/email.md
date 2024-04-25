# Email 


## Using a transactional email service
Add the following properties if you want to use a service like Mailgun, Sendgrid, or Mandrillapp 
as your SMTP server.

### Configuration 
#### Mandrill 
```
grails.mail.enabled=true
grails.mail.debug=true
grails.mail.from=<from-email>
grails.mail.host=smtp.mandrillapp.com
grails.mail.port=587
grails.mail.username=<username>
grails.mail.password=<password>
```

#### Sendgrid 
```
grails.mail.enabled=true
grails.mail.debug=true
grails.mail.from=<from-email>
grails.mail.host=smtp.sendgrid.net
grails.mail.port=587
grails.mail.username=<username>
grails.mail.password=<password>
```

#### Gmail
```
grails.mail.enabled=true
grails.mail.debug=true
grails.mail.from=<from-email>
grails.mail.host=smtp.gmail.com
grails.mail.port=465
grails.mail.username=<your-username>
grails.mail.password=<password-generated-from-google-accounts>
grails.mail.props=["mail.smtp.auth":"true", "mail.smtp.socketFactory.port":"465", "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory", "mail.smtp.socketFactory.fallback":"false"]
```
NOTE: I have not been able to able to get the Gmail configuration to work, but I'm sure someone with more time and intelligence will have no trouble figuring it out.


## Using local SMTP (not recommended)

### Install postfix
https://www.digitalocean.com/community/tutorials/how-to-install-and-configure-postfix-as-a-send-only-smtp-server-on-ubuntu-14-04

### Configuration
The default email configuration properties.
```
grails.mail.enabled = true			
grails.mail.debug = true
grails.mail.from = info@openboxes.com
grails.mail.prefix = [OpenBoxes]
grails.mail.host = localhost
grails.mail.port = 25
# These are commented out on purpose.
#grails.mail.username = <username>
#grails.mail.password = <password>
```

