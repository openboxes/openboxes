# Troubleshooting

## Could not open ServletContext resource \[/WEB-INF/applicationContext.xml\]

### Problem

```text
Caused by: java.io.FileNotFoundException: Could not open ServletContext resource [/WEB-INF/applicationContext.xml]
```

### Solution

Execute the grails upgrade command in order to generate the files nece

```text
$ grails upgrade
```

See the following stackoverflow article: [http://stackoverflow.com/questions/24243027/grails-spring-security-sample-application-not-working](http://stackoverflow.com/questions/24243027/grails-spring-security-sample-application-not-working)

