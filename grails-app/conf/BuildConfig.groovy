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
grails.project.docs.output.dir = "web-app/docs"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.plugin.location.spock = 'spock/'
grails.plugin.location.liquibase = 'liquibase/'

// Development configuration property used to enable xrebel features
//grails.tomcat.jvmArgs = ["-javaagent:/home/jmiranda/Desktop/xrebel/xrebel.jar"]

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
        excludes "xml-apis"
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        //grailsRepo "http://grails.org/plugins"
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()

        mavenRepo "http://repo.grails.org/grails/plugins-releases/"
        mavenRepo "http://repo.grails.org/grails/plugins/"
        mavenRepo "http://repo.grails.org/grails/core/"
    }

    dependencies {

        // Required by database connection
        compile 'mysql:mysql-connector-java:5.1.47'

        // Required by database connection pool
        compile 'com.mchange:c3p0:0.9.5.3'

        // Required by docx4j functionality
        compile('org.docx4j:docx4j:2.8.1') {
            excludes 'commons-logging:commons-logging:1.0.4', 'commons-codec', 'commons-io'
        }

        // Required for barcode4j
        compile 'com.google.zxing:javase:2.0'

        // Required by MailService (should replace with grails mail plugin)
        compile 'org.apache.commons:commons-email:1.2'
        compile 'org.apache.commons:commons-text:1.3'
        compile 'commons-lang:commons-lang:2.6'
        compile "org.jadira.usertype:usertype.jodatime:1.9"

        // Required by LDAP
        compile "com.unboundid:unboundid-ldapsdk:2.3.6"

        // Render Zebra labels
        compile "fr.w3blog:zebra-zpl:0.0.3"

        // Required by rendering plugin (NoClassDefFoundError: org/springframework/mock/web/MockHttpServletRequest)
        // and also for the SendStockAlertsJob which needs to use the GSP template engine
        runtime 'org.springframework:spring-test:3.0.5.RELEASE'

        // Required for functional tests
        test('net.sourceforge.htmlunit:htmlunit:2.10') { excludes "xml-apis" }
        test 'org.seleniumhq.selenium:selenium-firefox-driver:2.25.0'
        test('org.seleniumhq.selenium:selenium-htmlunit-driver:2.25.0') { excludes "htmlunit" }
        test 'org.seleniumhq.selenium:selenium-chrome-driver:2.25.0'
        test 'org.seleniumhq.selenium:selenium-ie-driver:2.25.0'
        test 'org.seleniumhq.selenium:selenium-support:2.25.0'

        // Required for Geb support in Grails 1.3.9
        test "org.spockframework:spock-grails-support:0.6-groovy-1.7"
        test("org.codehaus.geb:geb-spock:0.6.3") {
            exclude 'spock'
        }

        // Fake SMTP server
        test 'dumbster:dumbster:1.6'

        // Required for GPars
        compile "org.codehaus.gpars:gpars:0.12"
        compile "org.codehaus.jsr166-mirror:jsr166y:1.7.0"
        compile "org.codehaus.jsr166-mirror:extra166y:1.7.0"

        // Unknown
        build('org.jboss.tattletale:tattletale-ant:1.2.0.Beta2') { excludes "ant", "javassist" }
        compile('org.codehaus.groovy.modules.http-builder:http-builder:0.6') {
            excludes "xercesImpl", "groovy", "commons-lang", "commons-codec"
        }

        // REST client
        compile 'org.apache.httpcomponents:httpclient:4.5.12'


    }
    plugins {

        // Default plugins
        runtime(':tomcat:1.3.9')
        runtime(':hibernate:1.3.9') { excludes 'antlr' }

        // Required by functionality (need to be upgraded or replaced)
        runtime(":jquery:1.7.2")
        runtime(":jquery-ui:1.8.7") { excludes 'jquery' }
        compile ":rendering:0.4.4"
        compile ":raven:0.5.8"
        runtime(':excel-import:0.3') { excludes 'poi-contrib', 'poi-scratchpad' }
        runtime(':external-config-reload:1.4.0') { exclude 'spock-grails-support' }
        runtime(':quartz2:2.1.6.2')
        compile(":csv:0.3.1")


        // Unsure if used
        runtime(':mail:1.0.6') { excludes 'mail', 'spring-test' }
        runtime(':constraints:0.6.0')
        runtime(':jquery-validation:1.9') { // 1.7.3
            excludes 'constraints'
        }
        runtime(':jquery-validation-ui:1.4.7') { // 1.1.1
            excludes 'constraints', 'spock'
        }

        // Can probably be removed after migration
        runtime(":cache-headers:1.1.5")
        runtime(":resources:1.1.6")
        runtime(":zipped-resources:1.0") { excludes 'resources' }
        runtime(":cached-resources:1.0") {
            excludes 'resources', 'cache-headers'
        }

        // Still used but probably can be replaced
        compile(":barcode4j:0.2.1")
        compile(":pretty-time:0.3")
        compile(":console:1.1")
        compile(":image-builder:0.2")
        compile(":joda-time:1.4")
        compile(":springcache:1.3.1")
        compile(":webflow:1.3.8")
        compile(":yui:2.8.2.1")
        compile(":spring-events:1.2")
        //compile(":bubbling:2.1.4")

        // Not critical to application (might require code changes)
        //build(":codenarc:0.17")
        //compile(":dynamic-controller:0.3")
        compile(":google-analytics:1.0")
        //compile(":google-visualization:0.6.2")
        //compile(":grails-ui:1.2.3")
        //compile(":clickstream:0.2.0")
        //compile(":profile-template:0.1")
        //runtime(":runtime-logging:0.4")
        //compile(":ui-performance:1.2.2")

        // Not critical to application (can be removed without changes)
        //compile(":famfamfam:1.0.1")
        //compile(":template-cache:0.1")
        //compile(":ldap:0.8.2")

        // Test dependencies (used but should be replaced with new version)
        //test(":spock:0.6") {
        //    exclude "spock-grails-support"
        //}
        test(name: 'geb', version: '0.6.3') {}
        test ":code-coverage:1.2.5" //2.0.3-3

        // Dependencies that we want to use but cannot due to errors
        //compile ":standalone:1.0"
        //compile ":burning-image:0.5.1"
        //compile ":settings:1.4"
        //compile ":symmetricds:2.4.0"
        //compile ":grails-melody:1.46"

    }
}
