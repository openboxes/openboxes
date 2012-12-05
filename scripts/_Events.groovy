/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

import org.apache.log4j.Logger

Logger log = Logger.getLogger("org.pih.warehouse._Events")

private determineGitRevisionNumber = {
    String revisionNumber = 'dev'
    try {
        revisionNumber = 'git describe --match v*'.execute().text.trim()
    } catch (Exception e) {
        log.error 'Error executing git describe ', e
    }
    log.info("Setting git revision number " + revisionNumber)
    return revisionNumber
}

eventWarStart = {
	log.info "Copying liquibase changelogs ..."
	ant.copy(todir:"${basedir}/target/classes", failonerror:true, overwrite:true) {
		fileset(dir:"${basedir}/grails-app/migrations", includes:"**/*")
	}
}

eventRunAppStart = {
    log.info "Setting build date, build number, and revision number ..."
	String revisionNumber = determineGitRevisionNumber()
	
	def buildNumber = metadata.'app.buildNumber'
	if (!buildNumber) buildNumber = 1


	metadata.'app.revisionNumber' = revisionNumber
	metadata.'app.buildDate' = new java.text.SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new java.util.Date());
	metadata.'app.buildNumber' = buildNumber.toString()
	//metadata.persist()	
}

eventCreateWarStart = { warName, stagingDir ->
	log.info "Setting build date, build number, and revision number ..."

	def buildNumber = System.getProperty("build.number", metadata.'app.buildNumber')
	log.info("Setting BUILD_NUMBER to " + buildNumber)

    String revisionNumber = determineGitRevisionNumber()

    ant.propertyfile(file:"${stagingDir}/WEB-INF/classes/application.properties") {
		entry(key:"app.buildNumber", value:buildNumber)
		entry(key:"app.revisionNumber", value:revisionNumber)
		entry(key:"app.buildDate", value:new java.text.SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new java.util.Date()))
	}
}

eventCompileStart = {
    ant.delete(dir:"${basedir}/target/geb-reports")
}

eventTestPhaseStart = {name ->
    if (name == "unit") {
        println "Starting Jasmine Tests"
        def command = """phantomjs spec/lib/run_jasmine_test.coffee spec/TestRunner.html"""
        def proc = command.execute()
        proc.waitFor()
        println "${proc.in.text}"
        if (proc.exitValue() == 1) {
            event("JasminFailed", ["Tests FAILED"])
        }
    }
}

eventJasminFailed = { msg ->
    println msg
    System.exit(0)
}
