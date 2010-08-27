eventCleanStart = {
	println "### About to clean"
}
  
eventCleanEnd = {
	println "### Cleaning complete"
}
  

eventWarStart = { 
	println "Copying liquibase changelogs ..."
	Ant.copy(todir:"${basedir}/target/classes", failonerror:true, overwrite:true) {
		fileset(dir:"${basedir}/grails-app/migrations", includes:"**/*.xml")
	}
}