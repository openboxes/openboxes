# Application properties


## General 

### Settings
| Setting | Required | Description |
| ---- | ---- | ---- | ---- |
| openboxes.signup.defaultRoles | No | Used to specify default roles assigned to newly registered users (implies automatic activation).  Should only be used in cases where you either trust your registered users (e.g. app is running on LAN) or you don't care what users are allowed to do (e.g. demo server). |
| openboxes.system.defaultTimezone | No | Not currently supported. | 
| openboxes.fixtures.enabled | No | Only used on local machines when dataSource.url is overriden. When set to true this will trigger the creation of data fixtures used for testing.  | 

### Examples
```
openboxes.fixtures.enabled=false
openboxes.signup.defaultRoles=ROLE_MANAGER,ROLE_ASSISTANT
openboxes.system.defaultTimezone=America/Chicago
```


## Identifier Formats
You can configure all of the identifiers according to your specifications (N = Numeric, L = Letter, A = Alphanumeric). The default configuration looks like the following, but feel free to configure identifiers however you'd like. Once the format has been choosen, values for these identifiers are randomly generated when an item is created. There's also a Quartz process that runs in the background that generates a unique identifier for any object that does not currently have one.
```
openboxes.identifier.order.format = NNNLLL
openboxes.identifier.product.format = LLNN
openboxes.identifier.requisition.format = NNNLLL
openboxes.identifier.shipment.format = NNNLLL
openboxes.identifier.transaction.format = AAA-AAA-AAA
```

