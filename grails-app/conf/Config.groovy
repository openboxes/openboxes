// Locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

grails.config.locations = [ 
	//"classpath:${appName}-config.groovy",
	//"classpath:${appName}-config.properties",
	//"file:${userHome}/.grails/${appName}-config.groovy",
	"file:${userHome}/.grails/${appName}-config.properties"
]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails { 
	mail { 
		from = "warehouse@pih.org"
		host = "localhost"
		port = "25"
		//host = "smtp.gmail.com"
		//port = 465
		//username = "justin.miranda@gmail.com"
		//password = "test"
		//props = ["mail.smtp.auth":"true",
		//  "mail.smtp.socketFactory.port":"465",
		//  "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
		//  "mail.smtp.socketFactory.fallback":"false"]
	}
}

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
		xml: ['text/xml', 'application/xml'],
		text: 'text/plain',
		js: 'text/javascript',
		rss: 'application/rss+xml',
		atom: 'application/atom+xml',
		css: 'text/css',
		csv: 'text/csv',
		all: '*/*',
		json: ['application/json','text/json'],
		form: 'application/x-www-form-urlencoded',
		multipartForm: 'multipart/form-data']

// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// not sure what this does
grails.views.javascript.library="jquery"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to true if BootStrap.groovy is failing to add all sample data 
grails.gorm.failOnError = false

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable fo AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// set per-environment serverURL stem for creating absolute links
environments {
	production {  grails.serverURL = "http://www.changeme.com"  }
	development {
		grails.serverURL = "http://localhost:8080/${appName}";
	}
	test {  grails.serverURL = "http://localhost:8080/${appName}"  }
}

// log4j configuration
/*
 log4j = {
 root {
 error()
 additivity = true
 }
 debug 'grails.app'
 }*/

log4j = {
	// Example of changing the log pattern for the default console    
	appenders {
		//console name:'stdout', layout:pattern(conversionPattern: '%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n')		
		console name:'stdout', layout:pattern(conversionPattern: '%p %d{ISO8601} %c{4} %m%n')		
	}
	
	//error	'org.codehaus.groovy.grails.web.servlet',  //  controllers
	//		'org.codehaus.groovy.grails.web.pages', //  GSP
	//		'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	//		'org.codehaus.groovy.grails."web.mapping.filter', // URL mapping
	//		'org.codehaus.groovy.grails."web.mapping', // URL mapping
	//		'org.codehaus.groovy.grails.commons', // core / classloading
	//		'org.codehaus.groovy.grails.plugins', // plugins
	//		'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	//		'org.springframework'
	
	warn	'org.mortbay.log',
		'org.codehaus.groovy.grails.web.servlet',		// controllers
		'org.codehaus.groovy.grails.web.pages',			// GSP
		'org.codehaus.groovy.grails.web.sitemesh',		// layouts
		'org.codehaus.groovy.grails.web.mapping.filter',	// URL mapping
		'org.codehaus.groovy.grails.web.mapping', 		// URL mapping
		'org.codehaus.groovy.grails.commons', 			// core / classloading
		'org.codehaus.groovy.grails.plugins',			// plugins
		'org.codehaus.groovy.grails.orm.hibernate', 		// hibernate integration
		'net.sf.ehcache.hibernate'		
	
	info	'org.liquibase', 	
		'org.springframework',
		'org.hibernate',
		'org.pih.warehouse',
		'grails.app',
		'grails.app.controller',
		'grails.app.bootstrap',
		'grails.app.service',
		'grails.app.task'
		'BootStrap'

	debug	'liquibase',
		'org.apache.ddlutils'
	
	
}
