# Example
 
## openboxes-config.properties
```
# Database connection settings
dataSource.url=jdbc:mysql://localhost:3306/openboxes
dataSource.username=openboxes
dataSource.password=openboxes

# OpenBoxes administrator emails
openboxes.admin.email=justin.miranda@gmail.com,jmiranda@pih.org
 
# Only used on local machines when dataSource.url is overriden
openboxes.fixtures.enabled=false
 
# OpenBoxes Identifier Formats 
# N: Numeric, L: Letter, A: Alphanumeric
# For example, NNNLLL might lead to the following random identifier being generated 123ABC.
openboxes.identifier.order.format = NNNLLL
openboxes.identifier.product.format = LLNN
openboxes.identifier.requisition.format = NNNLLL
openboxes.identifier.shipment.format = NNNLLL
openboxes.identifier.transaction.format = AAA-AAA-AAA
 
# OpenBoxes Identifier Characters
openboxes.identifier.numeric = 0123456789
openboxes.identifier.alphabetic = ABCDEFGHJKMNPQRSTUVXYZ
openboxes.identifier.alphanumeric = 0123456789ABCDEFGHJKMNPQRSTUVWXYZ
 
# Report logo header (not supported yet)
# openboxes.report.header.logo = file:///home/jmiranda/Desktop/images.jpg
openboxes.report.header.logo = http://localhost:8080/openboxes/images/hands.jpg
openboxes.report.header.title = OpenBoxes
 
# OpenBoxes Error Email feature (bug report)
# Use your own address if you want to handle bug reports yourself. Otherwise leave as-is and OpenBoxes
# support these support requests.
openboxes.mail.errors.enabled = true
openboxes.mail.errors.recipients = support@openboxes.com

# OpenBoxes Barcode Scanner detection 
# NOTE: This feature is an experimentation. If enabled you can scan barcode on any page and the app will 
# try to locate an object (product, shipment, etc) that is associated with that barcode. If an object is 
# found, the app just redirects to the details page for that database object. In the future, I'm hoping to 
# improve the barcode scanner to integrate with workflows (e.g. add item to purchase order). 
openboxes.scannerDetection.enabled = true
 
# Used to specify default roles for newly registered users (implies automatic activation)
#openboxes.signup.defaultRoles=ROLE_MANAGER,ROLE_ASSISTANT
  
# General mail settings
grails.mail.enabled=true
grails.mail.host=<smtp server>
grails.mail.port=<smtp port>
grails.mail.username=<username>
grails.mail.password=<password>
grails.mail.from=<from address>
 
# Example of general mail settings 
#grails.mail.default.from=username@gmail.com
#grails.mail.host=smtp.gmail.com
#grails.mail.port=465
#grails.mail.username=username@gmail.com
#grails.mail.password=password
#grails.mail.props = ["mail.smtp.auth":"true", "mail.smtp.socketFactory.port":"465", "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory", "mail.smtp.socketFactory.fallback":"false"]

# Error email properties
mail.error.debug=true
mail.error.server=<smtp server>
mail.error.username=<smtp username>
mail.error.password=<smtp password>
mail.error.port=<smtp port>
mail.error.to=<to address>

# Example of error email properties 
#mail.error.server=localhost
#mail.error.server=smtp.pih.org
#mail.error.username=openboxes
#mail.error.password=password
#mail.error.to=justin.miranda@gmail.com
#Possible values: default|dynamic|alternate
mail.error.appender=dynamic

# OpenBoxes > Choose Location > Required Activities
# The supported activities required in order for a location a location to show up on Choose Location page.
# Possible values: MANAGE_INVENTORY, PLACE_ORDER, PLACE_REQUEST, FULFILL_ORDER, FULFILL_REQUEST, SEND_STOCK, RECEIVE_STOCK, EXTERNAL
openboxes.chooseLocation.requiredActivities = MANAGE_INVENTORY

# Amazon web service (not supported yet)
aws.s3.domain=s3.amazonaws.com
aws.s3.accessKey=0123456789ABCDEFG
aws.s3.secretKey=0123456789ABCDEFGHIJKLMNOPQRS
aws.s3.bucketName=files

# Google Product Search (no longer supported -- Google deprecated API)
# URL: https://www.googleapis.com/shopping/search/v1/public/products?key=${google.productSearch.key}&country=US&q=${q}&alt=scp&crowdBy=brand:1
google.api.key=<no longer supported>

# Hipaaspace.com NDC Lookup (not supported yet)
hipaaspace.api.key=<not supported yet>

# RXNorm (not supported yet)
# URL: http://rxnav.nlm.nih.gov/REST/
rxnorm.api.key=<not supported yet>

# Google analytics
google.analytics.enabled = false 
google.analytics.webPropertyID = <enter your google analytics property ID>
```
