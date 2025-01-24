
OpenBoxes uses MySQL database to store data. The database connection is configured in 
application.yml under the dataSource section. These settings can (and should be) overridden 
in a production environment to ensure a secure connection (i.e. change password, enable SSL, etc).

You can find detailed information about data source properties here [Data Source Configuration](https://docs.grails.org/latest/guide/conf.html#dataSource)


## Defaults
```yaml title="/opt/tomcat/.grails/openboxes.yml"
dataSource:
    url: jdbc:mysql://localhost:3306/openboxes?serverTimezone=UTC&useSSL=false
    username: openboxes
    password: openboxes
```
## Properties

| Property              | Required | Description |
|-----------------------| ---- | ---- |
| `dataSource.url`      | Yes | JDBC connection string |
| `dataSource.username` | Yes | JDBC username |
| `dataSource.password` | Yes | JDBC password |


## Considerations
* Always use strong credentials and secure database access.
* Ensure proper indexing and query optimization for performance.
* Enable SSL/TLS in url for secure connections (outside the scope)


