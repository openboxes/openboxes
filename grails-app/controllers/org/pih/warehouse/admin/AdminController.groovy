package org.pih.warehouse.admin

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.concurrent.FutureTask;

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
    def controllerActions = { 
			
		List actionNames = []
		grailsApplication.controllerClasses.sort { it.logicalPropertyName }.each { controller ->
			
			controller.reference.propertyDescriptors.each { pd ->
				def closure = controller.getPropertyOrStaticPropertyOrFieldValue(pd.name, Closure)
				if (closure) {
					if (pd.name != 'beforeInterceptor' && pd.name != 'afterInterceptor') {
						actionNames << controller.logicalPropertyName + "." + pd.name + ".label = " + pd.name
					}
				}
			}
			println "$controller.clazz.simpleName: $actionNames"
		}
		
		[actionNames:actionNames]
	}
	
    def plugins = { } 
    def status = { } 
    
	def static LOCAL_TEMP_WEBARCHIVE_PATH = "warehouse.war"
	
	def showUpgrade = { UpgradeCommand command ->
		log.info "show upgrade " + params
		
		[
			command : session.command
			//remoteFileSize: getRemoteFileSize(command?.remoteWebArchiveUrl),
			//remoteFileLastModifiedDate: new Date(getRemoteFileLastModifiedDate(command?.remoteWebArchiveUrl)) 
		]				
	}
	
	
	def download = { UpgradeCommand command ->
		log.info "download " + params
		if (command?.remoteWebArchiveUrl) {
			session.command = command
			session.command.future = null
			session.command?.localWebArchive = new File("warehouse.war")								
			flash.message = "Attempting to download '" + command?.remoteWebArchiveUrl + "' to '" + command?.localWebArchive?.absolutePath + "'"
			// Requires executor plugin
			//session.command.future = callAsync {			
			//	return doDownloadWar(command?.remoteWebArchiveUrl, command?.localWebArchive)
			//}
		}
		else {
			flash.message = "Please enter valid web archive url";
			
		}
		
		chain(action: "showUpgrade", model: [command : command])
		//redirect (action: "showUpgrade")
	}
	
	
	def deploy = { UpgradeCommand command -> 
		log.info "deploy " + params
		
		session.command.localWebArchivePath = command.localWebArchivePath
		command.localWebArchive = session.command.localWebArchive
	
		def source = session.command.localWebArchive		
		def destination = new File(session.command.localWebArchivePath)		
		def backup = new File(session.command.localWebArchive.absolutePath + ".backup")
		log.info "Copying wbe archive to backup " + source.absolutePath + " to " + backup.absolutePath
		backup.bytes = source.bytes
		
		log.info "Copying web archive to web container " + destination.absolutePath 
		destination.bytes = source.bytes	
		
		chain(action: "showUpgrade", model: [command : command])
		//redirect (view: "showUpgrade", model: [command: command])
	}
	
	def showSettings = { 		
		def externalConfigProperties = []
		grailsApplication.config.grails.config.locations.each { filename ->			
			try { 
				// Hack to remove the file: protocol from the URL string
				filename -= "file:"
				def file = new File(filename)
				def inputStream = new FileInputStream(file)
				def properties = new Properties()
				properties.load(inputStream)
				externalConfigProperties << properties
			} catch (FileNotFoundException e) { 
				log.warn("Properties file not found: " + e.message)
			}
		}
			
		[	
			externalConfigProperties: externalConfigProperties,
			systemProperties : System.properties,
			env: GrailsUtil.environment,
			enabled: Boolean.valueOf(grailsApplication.config.grails.mail.enabled),
			from: "${config.grails.mail.from}",
			host: "${config.grails.mail.host}",
			port: "${config.grails.mail.port}"
		]
	}
		
	
	def downloadWar = { 
		log.info params
		log.info("Updating war file " + params)
		def url = "http://ci.pih-emr.org/downloads/warehouse.war"
		
		// Requires executor plugin
		//def future = callAsync {
		//	return doDownloadWar(url) 
		//}		
		//session.future = future		
		redirect(action: "showSettings")
	}
	
	def cancelUpdateWar = { 
		log.info params
		if (session.future) { 
			session.future.cancel(true)
			new File(LOCAL_TEMP_WEBARCHIVE_PATH).delete()
		}
		redirect(action: "showSettings")
	}
		
	def deployWar = { UpgradeCommand -> 
		log.info params
		def source = session.command.localWebArchive
		
		def destination = new File(session.command.localWebArchivePath) 
		
		def backup = new File(session.command.localWebArchive.absolutePath + ".backup")
		log.info "Backing up " + source.absolutePath + " to " + backup.absolutePath 
		backup.bytes = source.bytes

		//destination.bytes = source.bytes
		
		
		redirect(action: "showSettings")
	}

	Integer doDownloadWar(String remoteUrl, File localFile) { 
		try { 
			log.info("Downloading war file " + remoteUrl + " .... ")
			def outputStream = new BufferedOutputStream(new FileOutputStream(localFile))		
			def url = new URL(remoteUrl)
			outputStream << url.openStream()
			outputStream.close();
			log.info("... done downloading remote file " + remoteUrl + " to " + localFile.absolutePath)
			//return file.absolutePath
			return 0
		} catch (Exception e) { 
			log.error e
			throw e;
		}
	}
	
	
	
	/*
	def reloadWar = {
		log.info("Reloading war file")
		def future = callAsync {
			log.info "Within call async"
			return reloadWar()
		}
		session.future = future
		redirect(action: "showSettings")
	}
	
	def doReloadWar() { 
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
	*/
	
	/*
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
			log.error "test"

		} finally {

			if(connection != null) {
				connection.disconnect();
			}
		}
		return applications 
	}
	*/



}

class UpgradeCommand {
	
	FutureTask future	
	File localWebArchive
	String remoteWebArchiveUrl
	String localWebArchivePath

	static constraints = {
		future(nullable:true)
		localWebArchive(nullable:true)
		remoteWebArchiveUrl(nullable: true)
		localWebArchivePath(nullable: true)
	}

	
	Integer getRemoteFileSize() {
		if (remoteWebArchiveUrl) { 
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) new URL(remoteWebArchiveUrl).openConnection();
				conn.setRequestMethod("HEAD");
				conn.getInputStream();
				return conn.getContentLength();
			} catch (IOException e) {
				return -1;
			} finally {
				if (conn) conn.disconnect();
			}
		}
		return -1;
	}
	
	Date getRemoteFileLastModifiedDate() {
		if (remoteWebArchiveUrl) { 
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) new URL(remoteWebArchiveUrl).openConnection();
				conn.setRequestMethod("HEAD");
				conn.getInputStream();
				return new Date(conn.getLastModified());
			} catch (IOException e) {
				return null;
			} finally {
				if (conn) conn.disconnect();
			}
		}
		return null;		
	}
	
	
	Float getProgressPercentage() { 
		def remoteFileSize = getRemoteFileSize()
		def localFileSize = localWebArchive?.size()
		if (remoteFileSize > 0 && localFileSize > 0) { 
			return (localFileSize / remoteFileSize) * 100
		}
		return -1;
	}
	
}