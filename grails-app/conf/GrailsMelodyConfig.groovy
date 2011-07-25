/*
You can find all detailed parameter usage from
http://code.google.com/p/javamelody/wiki/UserGuide#6._Optional_parameters
Any parameter with 'javamelody.' prefix configured in this file will be add as init-param of java melody MonitoringFilter.
 */




/*
The parameter disabled (false by default) just disables the monitoring.
 */
//javamelody.disabled = false

/*
The parameter system-actions-enabled (false by default) enables some system actions.
 */
//javamelody.'system-actions-enabled' = true


/*
Turn on Grails Service monitoring by adding 'spring' in displayed-counters parameter.
 */
javamelody.'displayed-counters' = 'http,sql,error,log,spring,jsp'




/*
The parameter url-exclude-pattern is a regular expression to exclude some urls from monitoring as written above.
 */
//javamelody.'url-exclude-pattern' = '/static/.*'



/*
Specify jndi name of datasource to monitor in production environment
 */
/*environments {
    production {
        javamelody.datasources = 'java:comp/env/myapp/mydatasource'
    }
}*/
