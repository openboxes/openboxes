package org.pih.warehouse.admin

import java.net.HttpURLConnection;
import java.net.URLConnection;

import javax.swing.text.html.HTML;

import grails.converters.XML;
import grails.util.GrailsUtil;
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.util.FileUtil;

import sun.misc.BASE64Encoder;

class AdminController {

	def mailService;
	def grailsApplication
	def config = ConfigurationHolder.config
	
    def index = { }
    def plugins = { } 
    def status = { } 
    
	
	def showSettings = { 		
		def externalConfigProperties = []
		grailsApplication.config.grails.config.locations.each { filename ->			
			// Hack to remove the file: protocol from the URL string
			filename -= "file:"
			def file = new File(filename)
			
			def inputStream = new FileInputStream(file)
			def properties = new Properties()
			properties.load(inputStream)
			externalConfigProperties << properties
		}
			
		[	
			applications: getApplications(),
			warFile : new File("/tmp/warehouse.war"),
			warSize: getFileSize(),
			warLastModifiedDate: new Date(getFileLastModifiedDate()),
			externalConfigProperties: externalConfigProperties,
			systemProperties : System.properties,
			env: GrailsUtil.environment,
			enabled: Boolean.valueOf(grailsApplication.config.grails.mail.enabled),
			from: "${config.grails.mail.from}",
			host: "${config.grails.mail.host}",
			port: "${config.grails.mail.port}"
		]
	}
	
	def updateSettings = { 
		
	}
	
	
	def updateWar = { 
		log.info("Updating war file")
		def future = callAsync {
			log.info "Within call async"
			return downloadWar() 
		}		
		session.future = future		
		redirect(action: "showSettings")
	}
	
	
	def reloadWar = { 
		log.info("Reloading war file")
		def future = callAsync {
			log.info "Within call async"
			return reloadWar()
		}
		session.future = future
		redirect(action: "showSettings")

	}
	
	def cancelUpdateWar = { 
		if (session.future) { 
			session.future.cancel(true)
			new File("/tmp/warehouse.war").delete()
		}
		redirect(action: "showSettings")
	}
	
	def checkForUpdates = { 
		
		
	}
	
	def deployWar = { 
		
		//FileUtil.copyFile(new File("/tmp/warehouse.war"),
		//	new File("/var/lib/tomcat6/webapps/warehouse.war"))
		def source = new File("/tmp/warehouse.war");
		
		def destination = new File("/var/lib/tomcat6/webapps/warehouse.war")
		//def destination = new File("/tmp/warehouse2.war")
		destination.bytes = source.bytes
		
		
		redirect(action: "showSettings")
	}
	
	
	def downloadWar = { 
		log.info("Downloading war file .... ")
		def fileOutputStream = new FileOutputStream("/tmp/warehouse.war")
		def outputStream = new BufferedOutputStream(fileOutputStream)
		
		def url = "http://ci.pih-emr.org/downloads/warehouse.war"
		outputStream << new URL(url).openStream()
		outputStream.close();
		
		
		log.info("... done downloading war file!")
		return "100";
	}
	
	def reloadWar() { 
		def connection = null
		try {
			//Create connection
			def url = new URL("http://localhost:8180/manager/reload?path=/warehouse");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
			String userpassword = "tomcat" + ":" + "tomcat";
			String encodedAuthorization = enc.encode( userpassword.getBytes() );
			connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

			return connection.content.text
			
		} catch (Exception e) {
			log.error e
			render e
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
	}
	
	
	def getApplications() { 

		def applications = []		
		def connection = null
		try {
			//Create connection
			def url = new URL("http://localhost:8180/manager/list");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
			String userpassword = "tomcat" + ":" + "tomcat";
			String encodedAuthorization = enc.encode( userpassword.getBytes() );
			connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

			connection.content.text.eachLine { 
				applications << it.tokenize(":")
			}
			
		} catch (Exception e) {
			log.error e

		} finally {

			if(connection != null) {
				connection.disconnect();
			}
		}
		return applications 
	}

	def getFileSize() {
		def contentLength = 0;
		HttpURLConnection conn = null;
		try {
			def url = new URL("http://ci.pih-emr.org/downloads/warehouse.war")
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			return -1;
		} finally {
			conn.disconnect();
		}
	}
	
	def getFileDate() {
		def contentLength = 0;
		HttpURLConnection conn = null;
		try {
			def url = new URL("http://ci.pih-emr.org/downloads/warehouse.war")
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getDate();
		} catch (IOException e) {
			return -1;
		} finally {
			conn.disconnect();
		}

	}

	def getFileLastModifiedDate() {
		def contentLength = 0;
		HttpURLConnection conn = null;
		try {
			def url = new URL("http://ci.pih-emr.org/downloads/warehouse.war")
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getLastModified();
		} catch (IOException e) {
			return -1;
		} finally {
			conn.disconnect();
		}

	}


	
	
	
}
