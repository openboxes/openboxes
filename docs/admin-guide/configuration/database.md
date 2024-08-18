# Database
[Data Source Configuration](https://docs.grails.org/latest/guide/conf.html#dataSource)

## Properties

| Property              | Required | Description |
|-----------------------| ---- | ---- |
| `dataSource.url`      | Yes | JDBC connection string |
| `dataSource.username` | Yes | JDBC username |
| `dataSource.password` | Yes | JDBC password |


## Example 
```yml title="/var/lib/tomcat9/.grails/openboxes.yml"
dataSource:
    url: jdbc:mysql://localhost:3306/openboxes
    username: openboxes
    password: openboxes
```
