/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
//grails.server.port.http = 8081

grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.docs.output.dir = "web-app/docs"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// Inline plugins
//grails.plugin.location.spock='spock/'
//grails.plugin.location.liquibase='liquibase/'


grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: [maxMemory: 2048, minMemory: 2048, debug: false, maxPerm: 1024, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 2048, minMemory: 2048, debug: false, maxPerm: 1024, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 2048, minMemory: 2048, debug: false, maxPerm: 1024, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 2048, minMemory: 2048, debug: false, maxPerm: 1024]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// specify dependency exclusions here; for example, uncomment this to disable ehcache:
		// excludes 'ehcache'
		excludes "xml-apis"
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	checksums true // Whether to verify checksums on resolve
    	legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

	repositories {
		inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
		grailsHome()
		mavenLocal()
		grailsCentral()
		mavenCentral()

		// uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
		//mavenRepo "http://repository.codehaus.org"
		//mavenRepo "http://download.java.net/maven/2/"
		//mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "http://repo.grails.org/grails/plugins/"
        mavenRepo "http://repo.grails.org/grails/plugins-releases/"

	}
	
	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
		runtime 'mysql:mysql-connector-java:5.1.29'
		runtime 'org.postgresql:postgresql:9.3-1101-jdbc41'
		test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"

//		build ('org.jboss.tattletale:tattletale-ant:1.2.0.Beta2')  { excludes "ant", "javassist" }
//
		compile ('org.docx4j:docx4j:2.8.1') { excludes 'commons-logging:commons-logging:1.0.4', 'commons-codec', 'commons-io'}
		compile 'c3p0:c3p0:0.9.1.2'

		compile 'com.google.zxing:javase:2.0'
		compile ('org.codehaus.groovy.modules.http-builder:http-builder:0.6') { excludes "xercesImpl", "groovy",  "commons-lang", "commons-codec" }
		compile 'org.apache.commons:commons-email:1.2'
        compile 'net.sourceforge.openutils:openutils-log4j:2.0.5'
        compile "com.unboundid:unboundid-ldapsdk:2.3.6"
		test 'dumbster:dumbster:1.6'
		// FIXME Had to add this dependency because of a NoClassDefFoundError
		runtime 'jline:jline:2.12'

//		//compile 'org.apache.httpcomponents:httpcore:4.2.1'
//		compile 'commons-lang:commons-lang:2.6'
//		compile "org.jadira.usertype:usertype.jodatime:1.9"
//
//		runtime 'org.springframework:spring-test:3.0.5.RELEASE'
//		test ("org.codehaus.geb:geb-spock:0.6.3") {
//				exclude 'spock'
//		}
//		test 'org.seleniumhq.selenium:selenium-firefox-driver:2.25.0'
//		test ('net.sourceforge.htmlunit:htmlunit:2.10') { excludes "xml-apis" }
//		test ('org.seleniumhq.selenium:selenium-htmlunit-driver:2.25.0')  { excludes "htmlunit" }
//		test 'org.seleniumhq.selenium:selenium-chrome-driver:2.25.0'
//		test 'org.seleniumhq.selenium:selenium-ie-driver:2.25.0'
//		test 'org.seleniumhq.selenium:selenium-support:2.25.0'
//		test 'dumbster:dumbster:1.6'
//		//test "org.spockframework:spock-grails-support:0.6-groovy-1.7"
//		
//        compile 'org.grails:grails-test:2.4.4'

	}
	plugins {

		build ":tomcat:7.0.70" // or ":tomcat:8.0.22"
        	//test ":spock:0.7"
        	test ":build-test-data:2.4.0"

		// plugins for the compile step
		compile ":scaffolding:2.1.2"
		compile ':cache:1.1.8'
		// asset-pipeline 2.0+ requires Java 7, use version 1.9.x with Java 6
		//compile ":asset-pipeline:2.5.7"

		// plugins needed at runtime but not for compilation
		runtime ":hibernate:3.6.10.19" //":hibernate4:4.3.10" // or ":hibernate:3.6.10.18"
		runtime ":database-migration:1.4.1"
		runtime ":jquery:1.11.1"

        compile ":grails-melody:1.59.0"

        compile(":joda-time:1.5")
        compile(':mail:1.0.7') {
            //excludes 'mail', 'spring-test'
        }
		compile(':excel-import:1.1.0.BUILD-SNAPSHOT') {
            //excludes 'poi-contrib', 'poi-scratchpad'
        }
        compile(":pretty-time:2.1.3.Final-1.0.1")
        compile(':quartz2:2.1.6.2')
        compile(':csv:0.3.1')
        compile(':clickstream:0.2.0')
        compile(":barcode4j:0.3")
        compile(":image-builder:0.2")
        //compile ":raven:6.0.0.4"
        //compile ":raven:6.0.0.5-SNAPSHOT"
        //compile ":raven:0.5.8"

        runtime(":yui:2.8.2.1")
        runtime(":bubbling:2.1.4")
        runtime(":webflow:2.1.0")
        runtime(":resources:1.2.14")
        runtime(":cache-headers:1.1.7")
        runtime(":zipped-resources:1.0") { excludes 'resources' }
        runtime(":cached-resources:1.0") { excludes 'resources', 'cache-headers' }


        runtime ":console:1.5.11"

		//runtime(":jquery:1.7.2")
		//runtime(":jquery-ui:1.8.24") { excludes 'jquery' }
		runtime( ':jquery-validation:1.9' ) { // 1.7.3
			excludes 'constraints'
		}
		runtime( ':jquery-validation-ui:1.4.7' ) { // 1.1.1
			excludes 'constraints', 'spock'
		}

        // Uncomment these to enable additional asset-pipeline capabilities
		//compile ":sass-asset-pipeline:1.9.0"
		//compile ":less-asset-pipeline:1.10.0"
		//compile ":coffee-asset-pipeline:1.8.0"
		//compile ":handlebars-asset-pipeline:1.3.0.3"

		// Existing plugins
//		compile ":rendering:1.0.0"
//		compile ":raven:6.0.0.4"
//
//		runtime( ':constraints:0.6.0' )
//		runtime( ':jquery-validation:1.9' ) { // 1.7.3
//			excludes 'constraints'
//		}
//		runtime( ':jquery-validation-ui:1.4.7' ) { // 1.1.1
//			excludes 'constraints', 'spock'
//		}


		/* spock from the grails repo doesn't work with grails 1.3 we've included our own build of it.*/
		//test(name:'spock', version:'0.6')
		
		//runtime(":liquibase:1.9.3.6") { excludes 'data-source' }
//		runtime(':mail:1.0.7') { excludes 'mail', 'spring-test' }
//		runtime(':excel-import:1.0.0') { excludes 'poi-contrib', 'poi-scratchpad' }
		//runtime(':hibernate:1.3.9') { excludes 'antlr' }
		//runtime(':tomcat:1.3.9') 
		//runtime(':external-config-reload:1.4.0') { exclude 'spock-grails-support' }
//		runtime(':quartz2:2.1.6.2')
//		runtime(":jquery:1.7.2")
//		runtime(":jquery-ui:1.8.7") { excludes 'jquery' }

		//test(":spock:0.6") {
		//    exclude "spock-grails-support"
		//}

		//test(name:'geb', version:'0.6.3') { }
//		test ":code-coverage:1.2.5" //2.0.3-3

		// Dependencies that we want to use but cannot due to errors
		//compile ":standalone:1.0"
		//compile ":burning-image:0.5.1"
		//compile ":settings:1.4"
		//compile ":symmetricds:2.4.0"
		//compile ":grails-melody:1.46"

		// plugins.barcode4j=0.2.1
		// plugins.bubbling=2.1.4
		// plugins.clickstream=0.2.0
		// plugins.codenarc=0.17
		// plugins.console=1.1
		// plugins.csv=0.3.1
		// plugins.dynamic-controller=0.3
		// plugins.external-config-reload=1.4.0
		// plugins.famfamfam=1.0.1
		// plugins.google-analytics=1.0
		// plugins.google-visualization=0.6.2
		// plugins.grails-ui=1.2.3
		// plugins.image-builder=0.2
		// plugins.joda-time=1.4
		// plugins.ldap=0.8.2
		// plugins.pretty-time=0.3
		// plugins.profile-template=0.1
		// plugins.runtime-logging=0.4
		// plugins.springcache=1.3.1
		// plugins.template-cache=0.1
		// plugins.ui-performance=1.2.2
		// plugins.webflow=1.3.8
		// plugins.yui=2.8.2.1


	}
}
