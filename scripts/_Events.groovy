/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
import grails.util.Environment
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.test.junit4.JUnit4GrailsTestType
import org.codehaus.groovy.grails.test.support.GrailsTestMode

Logger log = Logger.getLogger("org.pih.warehouse._Events")

/*loadtest setup*/
def testTypeName = "loadtest"
def testDirectory = "loadtest"
def testMode = new GrailsTestMode(autowire: true, wrapInTransaction: true, wrapInRequestEnvironment: true)
def loadtestTestType = new JUnit4GrailsTestType(testTypeName, testDirectory, testMode)
 
loadtestTests = [loadtestTestType]
 
loadtestTestPhasePreparation = {
       integrationTestPhasePreparation()
}
loadtestTestPhaseCleanUp = {
      integrationTestPhaseCleanUp()
}
eventAllTestsStart = {
    if(System.getProperty("grails.env") == "loadtest"){
      phasesToRun << "loadtest"
    }else{
      println "********* loadtest is ignored for environment ${System.getProperty("grails.env")}."
    }
}

def determineGitRevisionNumber = {
    String revisionNumber = 'dev'
    try {
        revisionNumber = 'git describe --match v* --always'.execute().text.trim()
    } catch (Exception e) {
        log.error 'Error executing git describe ', e
    }
    log.info("Setting git revision number " + revisionNumber)
    return revisionNumber
}

def determineGitBranchName = {
    String branchName = 'default'
    try {
        def command = "git branch | grep \\* | cut -d \' \'"
        def process = 'git branch'.execute() | 'grep \\*'.execute()
        log.info "Executing command " + command
        process.waitFor()
        branchName = process.text
        if (branchName) {
            branchName = branchName.replaceFirst("\\*", "").trim()
        }
        log.info "Done executing command:" + branchName
    } catch (Exception e) {
        log.error 'Error executing git status ', e
    }
    log.info("Setting git branch to " + branchName)
    return branchName
}


eventWarStart = {
	//log.info "Copying liquibase changelogs ..."
	//ant.copy(todir:"${basedir}/target/classes", failonerror:true, overwrite:true) {
	//	fileset(dir:"${basedir}/grails-app/migrations", includes:"**/*")
	//}
	
}

eventRunAppStart = {
	log.info "Setting build date, build number, and revision number ..."
  	def revisionNumber = determineGitRevisionNumber()
    def branchName = determineGitBranchName()

	def buildNumber = metadata.'app.buildNumber'
	if (!buildNumber) buildNumber = 1

	metadata.'app.revisionNumber' = revisionNumber
    metadata.'app.branchName' = branchName
	metadata.'app.buildDate' = new java.text.SimpleDateFormat("d MMM yyyy hh:mm:ss a").format(new java.util.Date());
	metadata.'app.buildNumber' = buildNumber.toString()
	//metadata.persist()

    println "Building React frontend"
    def command = """npm run bundle"""
    def proc = command.execute()
    proc.waitFor()
    println "${proc.in.text}"
    if (proc.exitValue() == 1) {
        event("ReactBuildFailed", ["React build FAILED"])
    } else {
        println "React build finished"
    }
}

eventReactBuildFailed = { msg ->
    println msg
    System.exit(1)
}

eventCreateWarStart = { warName, stagingDir ->
	log.info "Copying liquibase changelogs from ${basedir}/grails-app/migrations ..."
	ant.copy(todir:"${stagingDir}/WEB-INF/classes", failonerror:true, overwrite:true) {
		fileset(dir:"${basedir}/grails-app/migrations", includes:"**/*")
	}

	log.info "Setting build date, build number, and revision number ..."
	def revisionNumber = determineGitRevisionNumber()
    def branchName = determineGitBranchName()
	def buildNumber = System.getProperty("build.number", metadata.'app.buildNumber')
	
	log.info("Setting BUILD_NUMBER to " + buildNumber)


	log.info "Setting build properties ${stagingDir}/WEB-INF/classes/application.properties"
    ant.propertyfile(file:"${stagingDir}/WEB-INF/classes/application.properties") {
		entry(key:"app.buildNumber", value:buildNumber)
        entry(key:"app.branchName", value:branchName)
		entry(key:"app.revisionNumber", value:revisionNumber)
		entry(key:"app.buildDate", value:new java.text.SimpleDateFormat("d MMM yyyy hh:mm:ss a").format(new java.util.Date()))
	}
}

eventTestPhaseStart = {name ->
    if (name == "unit") {
        println "Starting React Tests"
        def command = """npm run test"""
        def proc = command.execute()
        proc.waitFor()
        println "${proc.in.text}"
        println "${proc.err.text}"
        if (proc.exitValue() == 1) {
            event("ReactTestsFailed", ["Tests FAILED"])
        } else {
            println "Tests PASSED"
        }
    }
}

eventReactTestsFailed = { msg ->
    println msg
    System.exit(1)
}

eventCompileEnd = {
}