# Configure HTTPS 

The easiest way to do this is to use Let's Encrypt (Certbot). 
See docs here https://www.digitalocean.com/community/tutorials/how-to-secure-apache-with-let-s-encrypt-on-ubuntu-18-04

## Register your domain name or add an A record for a new 
Instructions are specific to your domain name registrar. The important part is that you need to be 

Let's say you want to use openboxes.example.com. You need to create an A record for this domain so that it 
resolves to your server. It's probably best to have Apache running so you can test that a web page is returned.

### Test with a browser
* Open a web browser
* Enter your domain in the address bar (e.g. openboxes.example.com)
* If you see content like the Apache default page then it's working
![It works](/img/apache-it-works.png "Apache It Works")


### Test with nslookup
If your domain name has been registered properly, execute the following command to make sure there's a valid IP address 
associated with your domain name.
```
$ nslookup openboxes.example.com
Server:		127.0.0.53
Address:	127.0.0.53#53

Non-authoritative answer:
Name:	openboxes.example.com
Address: ###.###.###.###
```

If the domain has not been registered or if the domain name has not been propagated yet, then you'll see a message like this:
```
$ nslookup openboxes.example.com
Server:		127.0.0.53
Address:	127.0.0.53#53

** server can't find openboxes.example.com: NXDOMAIN
```

If you're impatient, you can run the nslookup query against the nameserver where the domain is registered. For example, 
if you've registered with GoDaddy, it would be something like this:
```
$ nslookup openboxes.example.com NS01.DOMAINCONTROL.COM
Server:		NS01.DOMAINCONTROL.COM
Address:	2603:5:2140::1#53

Name:	openboxes.example.com
Address: ###.###.###.###
```



## Install Certbot for Apache 
```
sudo add-apt-repository ppa:certbot/certbot
sudo apt-get update
sudo apt-get install python-certbot-apache
```

## Create new HTTPS certificate
```
sudo certbot --apache -d openboxes.example.com
```

## Answer Questions
```
Saving debug log to /var/log/letsencrypt/letsencrypt.log
Plugins selected: Authenticator apache, Installer apache
Enter email address (used for urgent renewal and security notices) (Enter 'c' to
cancel): email@example.com

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Please read the Terms of Service at
https://letsencrypt.org/documents/LE-SA-v1.2-November-15-2017.pdf. You must
agree in order to register with the ACME server at
https://acme-v02.api.letsencrypt.org/directory
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
(A)gree/(C)ancel: A

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Would you be willing to share your email address with the Electronic Frontier
Foundation, a founding partner of the Let's Encrypt project and the non-profit
organization that develops Certbot? We'd like to send you email about our work
encrypting the web, EFF news, campaigns, and ways to support digital freedom.
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
(Y)es/(N)o: Y
Obtaining a new certificate
Performing the following challenges:
http-01 challenge for openboxes.example.com
Enabled Apache rewrite module
Waiting for verification...
Cleaning up challenges
Created an SSL vhost at /etc/apache2/sites-available/000-default-le-ssl.conf
Enabled Apache socache_shmcb module
Enabled Apache ssl module
Deploying Certificate to VirtualHost /etc/apache2/sites-available/000-default-le-ssl.conf
Enabling available site: /etc/apache2/sites-available/000-default-le-ssl.conf

Please choose whether or not to redirect HTTP traffic to HTTPS, removing HTTP access.
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
1: No redirect - Make no further changes to the webserver configuration.
2: Redirect - Make all requests redirect to secure HTTPS access. Choose this for
new sites, or if you're confident your site works on HTTPS. You can undo this
change by editing your web server's configuration.
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Select the appropriate number [1-2] then [enter] (press 'c' to cancel): 2
Enabled Apache rewrite module
Redirecting vhost in /etc/apache2/sites-enabled/000-default.conf to ssl vhost in /etc/apache2/sites-available/000-default-le-ssl.conf

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Congratulations! You have successfully enabled https://openboxes.example.com

You should test your configuration at:
https://www.ssllabs.com/ssltest/analyze.html?d=openboxes.example.com
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

IMPORTANT NOTES:
 - Congratulations! Your certificate and chain have been saved at:
   /etc/letsencrypt/live/openboxes.example.com/fullchain.pem
   Your key file has been saved at:
   /etc/letsencrypt/live/openboxes.example.com/privkey.pem
   Your cert will expire on 2019-08-05. To obtain a new or tweaked
   version of this certificate in the future, simply run certbot again
   with the "certonly" option. To non-interactively renew *all* of
   your certificates, run "certbot renew"
 - Your account credentials have been saved in your Certbot
   configuration directory at /etc/letsencrypt. You should make a
   secure backup of this folder now. This configuration directory will
   also contain certificates and private keys obtained by Certbot so
   making regular backups of this folder is ideal.
 - If you like Certbot, please consider supporting our work by:

   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
   Donating to EFF:                    https://eff.org/donate-le

```

### Test automatic update of certificate
Certbot should have create a cron job to automatically renew the certificate every 90 days. You can test to make sure 
the renewal process works, by running the following command:
```
sudo certbot renew --dry-run
```


