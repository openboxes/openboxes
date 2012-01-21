eventWarStart = {
	println "Copying liquibase changelogs ..."
	Ant.copy(todir:"${basedir}/target/classes", failonerror:true, overwrite:true) {
		fileset(dir:"${basedir}/grails-app/migrations", includes:"**/*.xml")
	}
}

eventRunAppStart = { 	
	def revisionNumber = 'svn info'.execute().in.readLines()[4][10..-1]	
	def buildNumber = metadata.'app.buildNumber'
	if (!buildNumber) buildNumber = 1
	
	// Do we want to increment the build number in development mode?
	//else buildNumber = Integer.valueOf(buildNumber) + 1
	
	metadata.'app.revisionNumber' = revisionNumber.toString()
	metadata.'app.buildDate' = new java.text.SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new java.util.Date());
	metadata.'app.buildNumber' = buildNumber.toString()
	metadata.persist()	
}

eventCreateWarStart = { warName, stagingDir ->
	def revisionNumber = 'svn info'.execute().in.readLines()[4][10..-1]
	def buildNumber = System.getProperty("build.number", metadata.'app.buildNumber')
	println("Setting BUILD_NUMBER to " + buildNumber)
	println("Setting SVN revision number " + revisionNumber)
	
	ant.propertyfile(file:"${stagingDir}/WEB-INF/classes/application.properties") {
		entry(key:"app.buildNumber", value:buildNumber)
		entry(key:"app.revisionNumber", value:revisionNumber)
		entry(key:"app.buildDate", value:new java.text.SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new java.util.Date()))
	}	
	

	
}