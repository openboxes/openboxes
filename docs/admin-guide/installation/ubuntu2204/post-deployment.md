
When we first connect to OpenBoxes, there are three glaringly obvious problems with our current
configuration (see screenshot below).

1. Insecure traffic over HTTP (should always use HTTPS instead of HTTP)
2. Ugly IP address in the URL (should be your custom domain name)
3. Port number in the URL (should never display port number)

![img_2.png](img_2.png)


## Solutions and Recommendations
You are free to choose the solution that works best for your situation.

### Remove Port Number from URL
#### Option 1. Install a web server (Nginx, Apache) to forward requests to Tomcat
Follow instructions in the [Configure Reverse Proxy](reverse-proxy.md) guide.

#### Option 2. Configure Tomcat to listen on port 80/443 instead of 8080/8443
Follow the instructions in the [Tomcat 9.0 SSL/TLS Configuration How-To](https://tomcat.apache.org/tomcat-9.0-doc/ssl-howto.html) guide.

#### Option 3. Configure a Load Balancer to forward requests to Tomcat (same as 1, but slightly easier)
Consult documentation provided by your hosting provider.

### Remove IP Address from URL
#### Option 1: Replace IP address with custom domain name [recommended]
1. Register domain name with domain registrar 
2. Create DNS record for new domain 
3. Configure custom domain name in openboxes.yml (see `grails.serverURL` config)
4. Verify traffic is routed to server and URL is rewritten properly 

#### Option 2: Use load balancer service of your hosting provider
1. Consult documentation provided by your hosting provider. 

#### Option 3: Use Dynamic DNS service coupled with port forwarding on your router.
1. Consult documentation provided by your dynamic DNS service.

### Insecure traffic over HTTP

#### Option 1: Use Certbot / Let's Encrypt (free) [recommended]
1. Install Certbot
2. Configure web server (Nginx, Apache) to handle SSL termination 
3. Automate renewal of Certbot certificates

#### Option 2: Purchase an SSL certificate from a trusted certificate authority
1. Configure web server (Nginx, Apache) to handle SSL termination
2. Set up calendar reminders to alert you when an SSL certificate is about to expire
2. Manually renew certification before expiration

## Next Steps
To get started with our recommended best practices, click the Next link below.
