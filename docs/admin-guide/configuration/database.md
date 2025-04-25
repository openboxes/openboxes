
OpenBoxes supports MySQL 8 as its primary database (we also support MariaDB 10.x). The database 
connection is configured in `application.yml` under the `dataSource` section. The default settings 
should be modified in any production environment to ensure a secure connection. You should follow 
best practices regarding security when configuring your database (i.e. change password, enable SSL, etc).

!!! tip
    You can find detailed information about all available data source properties here [Data Source Configuration](https://docs.grails.org/latest/guide/conf.html#dataSource)

## Configuration
```yaml title="openboxes.yml"
dataSource:
    url: jdbc:mysql://localhost:3306/openboxes?serverTimezone=UTC&useSSL=false
    username: openboxes
    password: openboxes
```

## Properties

| Property              | Description                      | Example Value |
|-----------------------|--------------------------------|--------------|
| `dataSource.url`      | Database connection string      | `jdbc:mysql://localhost:3306/openboxes` |
| `dataSource.username` | Database username              | `openboxes` |
| `dataSource.password` | Database password              | `secret` |

!!! tip
    For more details on the format for the `dataSource.url`, please refer to the 
    [MySQL JDBC Connector docs](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-jdbc-url-format.html).


## Customization
There are many more configuration properties under the `dataSource` configuration object that are not
mentioned here. You can read more about these properties in our 
[Connection Pool configuration](connection-pool.md) section. 

