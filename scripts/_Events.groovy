/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
eventWarStart = {
	println "Copying liquibase changelogs ..."
	Ant.copy(todir:"${basedir}/target/classes", failonerror:true, overwrite:true) {
		fileset(dir:"${basedir}/grails-app/migrations", includes:"**/*.xml")
	}
}

eventRunAppStart = { 	
	println "Setting build date, build number, and revision number ..."
	//def revisionNumber = 'svn info'.execute().in.readLines()[4][10..-1]
	def revisionNumber = 0 
	try { 
		revisionNumber = 'svn info'.execute().in.readLines()[4][10..-1]	
	} catch (Exception e) {
		println 'Error executing svn info ' + e.message
	}
	
	def buildNumber = metadata.'app.buildNumber'
	if (!buildNumber) buildNumber = 1
	
	// Do we want to increment the build number in development mode?
	//else buildNumber = Integer.valueOf(buildNumber) + 1
	
	metadata.'app.revisionNumber' = revisionNumber.toString()
	metadata.'app.buildDate' = new java.text.SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new java.util.Date());
	metadata.'app.buildNumber' = buildNumber.toString()
	//metadata.persist()	
}

eventCreateWarStart = { warName, stagingDir ->
	println "Setting build date, build number, and revision number ..."
	//def revisionNumber = 'svn info'.execute().in.readLines()[4][10..-1]
	def revisionNumber = 0
	try {
		revisionNumber = 'svn info'.execute().in.readLines()[4][10..-1]
	} catch (Exception e) { 
		println 'Error executing svn info ' + e.message
	}

	def buildNumber = System.getProperty("build.number", metadata.'app.buildNumber')
	println("Setting BUILD_NUMBER to " + buildNumber)
	println("Setting SVN revision number " + revisionNumber)
	
	ant.propertyfile(file:"${stagingDir}/WEB-INF/classes/application.properties") {
		entry(key:"app.buildNumber", value:buildNumber)
		entry(key:"app.revisionNumber", value:revisionNumber)
		entry(key:"app.buildDate", value:new java.text.SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new java.util.Date()))
	}	
	

	
}