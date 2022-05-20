/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

grails.project.class.dir = "target/classes"
grails.project.docs.output.dir = "web-app/docs"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.plugin.location.spock = 'spock/'
grails.plugin.location.liquibase = 'liquibase/'

grails.project.dependency.resolution = {
    inherits("global") {
        excludes(
                "commons-logging",  // use jcl-over-slf4j instead
                "log4j",  // use reload4j instead
                "slf4j-log4j12",  // use slf4j-reload4j instead
                "xml-apis",  // looks like this conflicts with Grails's internal SAXParserImpl
                "xmlbeans"  // conflicts with Grails: see https://stackoverflow.com/a/6410955
        )
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        inherit false  // some old plugins refer to repos that no longer exist
        mavenRepo "https://repo1.maven.org/maven2/"
        mavenRepo "https://repo.grails.org/grails/plugins-releases/"
    }

    dependencies {
        /*
         * Unfortunately, grails 1.3.9 doesn't play nicely with log4j 2's bridge
         * library (https://logging.apache.org/log4j/2.x/manual/migration.html);
         * it instantiates org.apache.log4j.PatternLayout in a non-compliant way.
         *
         * For the time being, we can get critical log4j security patches from
         * reload4j until we move to a more modern Grails release.
         * https://reload4j.qos.ch/news.html https://www.slf4j.org/legacy.html
         */
        build 'org.slf4j:slf4j-api:1.7.36'  // use 'build' to override Grails' out-of-date version
        compile 'org.slf4j:jcl-over-slf4j:1.7.36'  // use 'compile' to override jxls' out-of-date version
        runtime 'org.slf4j:jul-to-slf4j:1.7.36'
        runtime 'org.slf4j:slf4j-reload4j:1.7.36'

        // Required by database connection
        compile 'mysql:mysql-connector-java:5.1.47'

        // Required by database connection pool
        compile 'com.mchange:c3p0:0.9.5.3'

        // Required by docx4j functionality
        compile 'org.docx4j:docx4j:2.8.1'
        // Required for barcode4j
        compile 'com.google.zxing:javase:2.0'

        compile "org.apache.commons:commons-email:1.5"
        compile "org.apache.commons:commons-text:1.3"  // last Java 7-compatible release
        compile 'commons-lang:commons-lang:2.6'
        compile 'org.jadira.usertype:usertype.jodatime:1.9'  // org.joda.time.* is redundant in Java 8
        compile "org.apache.commons:commons-csv:1.6"  // last Java 7-compatible release

        // Required by LDAP
        compile "com.unboundid:unboundid-ldapsdk:2.3.6"

        compile 'fr.w3blog:zebra-zpl:0.0.3'  // ZebraUtils.printZpl(), Labelary API, etc.

        // Required for functional tests
        test 'net.sourceforge.htmlunit:htmlunit:2.10'
        test 'org.seleniumhq.selenium:selenium-firefox-driver:2.25.0'
        test 'org.seleniumhq.selenium:selenium-htmlunit-driver:2.25.0'
        test 'org.seleniumhq.selenium:selenium-chrome-driver:2.25.0'
        test 'org.seleniumhq.selenium:selenium-ie-driver:2.25.0'
        test 'org.seleniumhq.selenium:selenium-support:2.25.0'

        // Required for Geb support in Grails 1.3.9
        test "org.spockframework:spock-grails-support:0.6-groovy-1.7"
        test 'org.codehaus.geb:geb-spock:0.6.3'

        compile ("fr.opensagres.xdocreport:xdocreport:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.document:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.document.docx:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.document.odt:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.template:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.template.freemarker:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.template.velocity:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.converter:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.converter.odt.odfdom:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.converter.docx.xwpf:1.0.6")
        compile ("fr.opensagres.xdocreport:fr.opensagres.xdocreport.converter.docx.docx4j:1.0.6")
        compile ("fr.opensagres.xdocreport:org.apache.poi.xwpf.converter.pdf:1.0.6")
        compile ("fr.opensagres.xdocreport:org.odftoolkit.odfdom.converter.pdf:1.0.6")
        compile "org.apache.xmlgraphics:batik-util:1.7"

        /*
         * This test SMTP client is the latest release that works with Grails 1,
         * and Java 7, although it depends on a junit release we can't use (yet).
         */
        test('com.icegreen:greenmail:1.5.10') { exclude 'junit' }

        // Required for GPars
        compile "org.codehaus.gpars:gpars:0.12"
        compile "org.codehaus.jsr166-mirror:jsr166y:1.7.0"
        compile "org.codehaus.jsr166-mirror:extra166y:1.7.0"

        // used only in scripts/VerifyClasspath.groovy
        build 'org.jboss.tattletale:tattletale-ant:1.2.0.Beta2'

        // used once in DocumentController.groovy -- refactor to use org.apache.*HttpClient
        compile('org.codehaus.groovy.modules.http-builder:http-builder:0.6') {
            exclude 'groovy'  // otherwise it pulls in a newer groovy than Grails 1.3.9 wants
        }

        // REST client
        compile 'org.apache.httpcomponents:httpclient:4.5.12'

        // for com.google.common
        compile 'com.google.guava:guava:12.0'

        // TODO: This is the last version for java 7. After migration to Java 8 upgrade this to 2.9+
        compile 'org.jxls:jxls:2.8.1'
        /*
         * This jxls-poi release is the last that supports Apache POI v3 (anything
         * above needs v4, which requires Java 8). What's more, if left to its
         * own devices, it will introduce a dependency on a jxls that requires
         * java 8, too. We exclude its jxls requirement to force the one above.
         */
        compile('org.jxls:jxls-poi:1.0.9') { exclude "jxls" }
    }
    plugins {

        // Default plugins
        runtime(':tomcat:1.3.9')
        runtime(':hibernate:1.3.9')

        // enables <jq:*> tags, which we don't use
        runtime(":jquery:1.7.2")
        // enables <jqui:*> tags, which we only use for imports
        runtime ':jquery-ui:1.8.7'

        // enables *RenderingService and renderPdf()
        compile ":rendering:0.4.4"

        /*
         * This dependency has an easy migration to Grails 3,
         * but we may want to investigate using POI directly.
         * (see https://github.com/gpc/grails-excel-import)
         */
        runtime ':excel-import:0.3'

        runtime(':quartz2:2.1.6.2')
        compile(":csv:0.3.1")  // FIXME continue migrating to commons-csv instead

        /*
         * abandonware https://web.archive.org/web/20170705122757/http://grails.org/plugin/jquery-validation
         * abandonware http://limcheekin.github.io/jquery-validation-ui/docs/guide/single.html
         *
         * Replace with <g:renderErrors>? (which we use elsewhere and works with Grails 1-4+)
         *
         * https://grails.github.io/grails2-doc/1.3.9/guide/single.html#7.3%20Validation%20on%20the%20Client
         * https://docs.grails.org/3.1.1/guide/validation.html#validationOnTheClient
         */
        runtime ':jquery-validation:1.9'  // enables <jqval:*>
        runtime ':jquery-validation-ui:1.4.7'  // enables <jqvalui:*>

        // plugins that improve page rendering, may not be needed in Grails 3+
        runtime ':cache-headers:1.1.5'  // not used directly (?)
        runtime ':resources:1.1.6'  // enables <r:*> tags: https://grails-plugins.github.io/grails-resources/guide/
        runtime ':cached-resources:1.0'
        runtime ':zipped-resources:1.0'

        // these plugins "just work" with no code changes
        compile ':console:1.1'  // enables `grails console` command
        compile ':google-analytics:1.0'  // also enables <ga:*> tags
        compile ':raven:0.5.8'  // consider https://github.com/agorapulse/grails-sentry
        test ':code-coverage:1.2.5' // enables `grails test-app -coverage`

        /*
         * Used only once, in ShipmentController: barcode4jService.render()
         * Replace with com.google.zxing.oned.Code128Writer().
         */
        compile(":barcode4j:0.2.1")

        /*
         * Enables prettytime.display(), which we use only three times.
         * Easy migration to Grails 3, see https://github.com/cazacugmihai/grails-pretty-time
         */
        compile(":pretty-time:0.3")

        // FIXME replace with Image.getScaledInstance()
        compile ':image-builder:0.2'  // used once, in DocumentService.groovy

        compile ':joda-time:1.4'  // enables <joda:*> tags, not sure we use this
        compile ':springcache:1.3.1'  // enables import grails.plugin.springcache.*
        compile ':webflow:1.3.8'  // supports Spring Web Flow, but not Grails 3 :-(

        /*
         * Enables publishEvent, but development stopped in Grails 2.
         * Replace with https://docs.grails.org/3.2.3/guide/async.html#events
         */
        compile(":spring-events:1.2")

        /*
         * Enables userAgentIdentService.isMobile() and <browser:is*> tags.
         * Straightforward migration to Grails 3.
         */
        compile(":browser-detection:0.4.3")

        // this plugin has a straightforward migration to Grails 3
        test ':geb:0.6.3'

        /*
         * Pretty sure we don't use this, but it's well maintained through grails 5.
         * https://longwa.github.io/build-test-data/
         */
        compile ":build-test-data:1.1.1"
    }
}
