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

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.plugin.location.spock='spock/'


grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits( "global" ) {
		// uncomment to disable ehcache
		// excludes 'ehcache'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()

		mavenLocal()
		mavenCentral()

		// Details from docx4j Getting Started Guide at http://dev.plutext.org/svn/docx4j/trunk/docx4j/docs/Docx4j_GettingStarted.html
		//mavenRepo "http://dev.plutext.org/svn/docx4j/trunk/docx4j/m2"

		//mavenRepo "http://dev.plutext.org/trac/docx4j/browser/trunk/docx4j/m2"
		//mavenRepo "https://webdavclient4j.svn.sourceforge.net/svnroot/webdavclient4j/trunk/m2"
		// uncomment the below to enable remote dependency resolution
		// from public Maven repositories
		//mavenLocal()
		//mavenCentral()
		//mavenRepo "http://snapshots.repository.codehaus.org"
		//mavenRepo "http://repository.codehaus.org"
		//mavenRepo "http://download.java.net/maven/2/"
		//mavenRepo "http://repository.jboss.com/maven2/"
	}
	
	dependencies {
		//ivySettings.setVariable("ivy.checksums", "")
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
		//runtime 'org.docx4j:docx4j:2.7.0'
		// runtime 'mysql:mysql-connector-java:5.1.5'

		//runtime ('xerces:xercesImpl:2.8.1'){ excludes "xml-apis" }
		//runtime ('net.sourceforge.nekohtml:nekohtml:1.9.9'){ excludes "xercesImpl" }

		//build ('org.codehaus.groovy:http-builder:0.5.0-SNAPSHOT'){ excludes "junit", "xml-apis", "xercesImpl" }
		//runtime ('xerces:xercesImpl:2.8.1'){ excludes "xml-apis" }

		//runtime ('xerces:xercesImpl:2.8.1'){ excludes "xml-apis" }
		//runtime ('net.sourceforge.nekohtml:nekohtml:1.9.16') { excludes "xercesImpl" }

		test "org.codehaus.geb:geb-spock:0.6.3"
		test 'org.seleniumhq.selenium:selenium-firefox-driver:2.25.0'
		test 'org.seleniumhq.selenium:selenium-chrome-driver:2.25.0'
		test 'org.seleniumhq.selenium:selenium-ie-driver:2.25.0'
	}
	plugins {

        runtime( ':constraints:0.6.0' )
        runtime( ':jquery-validation:1.7.3' ) {
            excludes ([ name: 'constraints'])
        }
        runtime( ':jquery-validation-ui:1.1.1' ) {
            excludes ([ name: 'constraints'])
        }
        /* spock from the grails repo doesn't work with grails 1.3
           we've included our own build of it.
        test(name:'spock', version:'0.6')
        */
        test (name:'geb', version:'0.6.3')

		/*
		 compile ":mail:1.0"
		 compile ":csv:0.3.1"
		 compile ":rest:0.7"
		 compile ":app-info:0.4.3"
		 compile ":barcode4j:0.2.1"
		 compile ":bubbling:2.1.3"
		 compile ":codenarc:0.17"
		 compile ":constraints:0.6.0"
		 compile ":dynamic-controller:0.2.1"
		 compile ":excel-import:0.3"
		 compile ":executor:0.3"
		 compile ":famfamfam:1.0.1"
		 compile ":google-visualization:0.4"
		 compile ":grails-ui:1.2.3"
		 compile ":hibernate:1.3.7"
		 compile ":image-builder:0.2"
		 compile ":jaxrs:0.4"
		 compile ":joda-time:1.4"
		 compile ":jquery:1.4.4.1"
		 compile ":jquery-ui:1.8.7"
		 compile ":jquery-validation:1.9"
		 compile ":jquery-validation-ui:1.3"
		 compile ":json-rest-api:1.0.11"
		 compile ":liquibase:1.9.3.6"
		 compile ":pretty-time:0.3"
		 compile ":springcache:1.1.2"
		 compile ":tomcat:1.3.7"
		 compile ":ui-performance:1.2.2"
		 compile ":webflow:1.3.5"		
		 compile ":yui:2.8.2.1"
		 */

		//test(":spock:0.6")
		//compile (":joda-time:1.1") { exclude "spock" }
		//compile (":webflow:1.3.5") { exclude "spock" }

	}



}
