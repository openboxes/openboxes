/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
import liquibase.diff.Diff
import liquibase.diff.DiffResult
import java.sql.Driver
import liquibase.database.DatabaseFactory
import java.sql.Connection
import liquibase.database.Database
import liquibase.Liquibase
import liquibase.database.DatabaseFactory

includeTargets << new File("${liquibasePluginDir}/scripts/LiquibaseSetup.groovy")
includeTargets << new File("${liquibasePluginDir}/scripts/Migrate.groovy")

target('dbDiffIncremental': '''Generates change log required to make Test DB match Development''') {
	depends(setup)

	try {
		def baseDatabase = getDatabase(config)
		ConfigObject testConfig = loadTestConfig(classLoader, servletVersion, basedir, userHome, grailsAppVersion, grailsAppName, grailsHome)

		def targetLiquibase = getDiffDbLiquibase(testConfig)

		targetLiquibase.update(null)
		
		def targetDatabase = getDatabase(testConfig)
		Diff diff = new Diff(baseDatabase, targetDatabase);
		diff.setDiffTypes("tables, columns, primaryKeys, foreignKeys, indexes, uniqueConstraints, sequences")
		
		DiffResult diffResult = diff.compare();
		diffResult.printChangeLog(System.out, targetDatabase);
	}
	catch (Exception e) {
		e.printStackTrace()
		event("StatusFinal", ["Failed to diff database ${grailsEnv} with test"])
		exit(1)
	} finally {
		liquibase.getDatabase().getConnection().close();
	}
}

private ConfigObject loadTestConfig(classLoader, servletVersion, basedir, userHome, grailsAppVersion, grailsAppName, grailsHome) {
	def testConfigSlurper = new ConfigSlurper('diff')
	testConfigSlurper.setBinding(grailsHome: grailsHome, appName: grailsAppName, appVersion: grailsAppVersion, userHome: userHome, basedir: basedir, servletVersion: servletVersion)

	def myClassLoader = new URLClassLoader([ classesDir.toURI().toURL()] as URL[], rootLoader)
	def testConfig = testConfigSlurper.parse(myClassLoader.loadClass("DataSource"))
	return testConfig
}

private Database getDatabase(config) {
	Properties p = config.toProperties()

	def driverClassName = config.dataSource.driverClassName
	def username = config.dataSource.username
	def password = config.dataSource.password
	def url = config.dataSource.url

	Driver driver = getDriver(p, classLoader, driverClassName, url)
	Connection connection = getConnection(url, driver, password, username, driverClassName)

	return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
}

private Liquibase getDiffDbLiquibase(def config) {
	Properties p = config.toProperties()

	def driverClassName = config.dataSource.driverClassName
	def username = config.dataSource.username
	def password = config.dataSource.password
	def url = config.dataSource.url

	Driver driver = getDriver(p, classLoader, driverClassName, url)

	def connection = getConnection(url, driver, password, username, driverClassName)
	def fileOpener = classLoader.loadClass("org.liquibase.grails.GrailsFileOpener").getConstructor().newInstance()
	return new Liquibase("changelog.xml", fileOpener, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection));
}

private Connection getConnection(url, Driver driver, password, username, driverClassName) {
	Properties info = new Properties()
	info.put("user", username)
	if (password != null) {
		info.put("password", password);
	}

	println "Base database is URL: ${url}"
	connection = driver.connect(url, info);
	if (connection == null) {
		throw new RuntimeException("Connection could not be created to ${url} with driver ${driverClassName}.  Possibly the wrong driver for the given database URL");
	}
	return connection
}

private Driver getDriver(Properties p, classLoader, driverClassName, url) {
	Driver driver
	if (p.driver == null) {
		p.driver = DatabaseFactory.getInstance().findDefaultDriver(url)
	}
	if (p.driver == null) {
		throw new RuntimeException("Driver class was not specified and could not be determined from the url")
	}
	driver = (Driver) Class.forName(driverClassName, true, classLoader).newInstance()
	return driver
}

setDefaultTarget("dbDiffIncremental")
