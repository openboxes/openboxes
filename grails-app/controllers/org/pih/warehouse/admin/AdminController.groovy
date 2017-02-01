/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.admin

import grails.util.GrailsUtil
import grails.util.Holders
import grails.validation.Validateable
import org.pih.warehouse.core.MailService
import org.springframework.web.multipart.MultipartFile
import util.ClickstreamUtil

//import java.net.HttpURLConnection;
//import java.net.URLConnection;
import java.util.concurrent.FutureTask;

// import javax.swing.text.html.HTML;

// import grails.converters.XML;
// import org.pih.warehouse.util.FileUtil;

import sun.misc.BASE64Encoder;

class AdminController {

	def fileService
	MailService mailService;

	def grailsApplication
	def config = Holders.grailsApplication.config
	def sessionFactory // inject Hibernate sessionFactory

	def index() { }


    def controllerActions() {
			
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

    def cache() {
        [cacheStatistics: sessionFactory.getStatistics()]
    }
    def clickstream() {
        if (params.downloadFormat == "csv") {
            def filename = "Clickstream - ${session.user.name}.csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text: ClickstreamUtil.getClickstreamAsCsv(session.clickstream))
            return;
        }
    }

    def plugins() { }
    def status() { }

    def evictDomainCache() {
        def domainClass = grailsApplication.getDomainClass(params.name)
        if (domainClass) {
            sessionFactory.evict(domainClass.clazz)
            flash.message = "Domain cache '${params.name}' was invalidated"
        }
        else {
            flash.message = "Domain cache '${params.name}' does not exist"
        }
        redirect(action: "showSettings")
    }

    def evictQueryCache() {
        if (params.name) {
            sessionFactory.evictQueries(params.name)
            flash.message = "Query cache '${params.name}' was invalidated"
        }
        else {
            sessionFactory.evictQueries()
            flash.message = "All query caches were invalidated"
        }
        redirect(action: "showSettings")
    }


	def sendMail() {

        if (request.method == "POST") {
            try {
                withForm {
                    MultipartFile multipartFile = request.getFile('file')
                    if (!multipartFile.empty) {
                        byte[] bytes = multipartFile.bytes

                        println multipartFile.contentType
                        println multipartFile.originalFilename
                        println multipartFile.name

                        def emailMessageMap = [
                                from: session?.user?.email,
                                to: params.list("to"),
                                cc: [],
                                bcc: [],
                                subject: params["subject"],
                                body: params["message"],
                                attachment: multipartFile?.bytes,
                                attachmentName: multipartFile?.originalFilename,
                                mimeType: multipartFile?.contentType
                        ]
                        mailService.sendHtmlMailWithAttachment(emailMessageMap);
                        flash.message = "Multipart email with subject ${params.subject} and attachment ${multipartFile.originalFilename} has been sent to ${params.to}"
                    } else {
                        if (params.includeHtml) {
                            mailService.sendHtmlMail(params.subject, params.message, params.to)
                            flash.message = "HTML email with subject ${params.subject} has been sent to ${params.to}"
                        } else {
                            mailService.sendMail(params.subject, params.message, params.to)
                            flash.message = "Text email with subject ${params.subject} has been sent to ${params.to}"
                        }
                    }
                }.invalidToken {
                    flash.message = "Invalid token"
                }
            } catch (Exception e) {
                flash.message = "Unable to send email due to error: " + e.message
            }
        }


    }
	
	def showSettings() {
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

}
