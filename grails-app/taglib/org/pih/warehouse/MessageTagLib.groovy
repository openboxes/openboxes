/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse

import java.text.MessageFormat;

import org.pih.warehouse.core.Localization;

class MessageTagLib {
   
	static namespace = "warehouse"
	
	Locale defaultLocale = new Locale(grailsApplication.config.locale.defaultLocale)
	
	def message = { attrs, body ->		
		long startTime = System.currentTimeMillis()
		def defaultTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib')
		
		//if (!flash.localizations) {
		//	flash.localizations = []
		//}
		
		if (session.user) { 
			def localization = Localization.findByCodeAndLocale(attrs.code, session?.user?.locale?.toString()) 
			if (localization) { 
				//println "Arguments: " + attrs?.args + ":" + attrs?.args?.class
				def message = localization.text 
				if (attrs?.args) { 
					message = MessageFormat.format(localization.text, attrs.args.toArray())
				}
				
				if (session.useDebugLocale) { 
					//flash.localizations << ['code':attrs.code, 'text':localization.text]
					
					out << """<span style="border: 3px solid lightgrey; padding: 2px;">
									<img class='show-localization-dialog' 
										data-id="${localization.id}"
										data-code="${localization.code}" 
										data-locale="${localization.locale}" 
										data-text="${localization.text}" 
										data-args="${attrs.args}" 
										data-localized="" 
										src="${createLinkTo(dir:'images/icons/silk',file: 'database.png')}" title=""/>
									${attrs.code} = ${message}
							</span>
							"""
					return;
				}
				else { 
					out << """${message}"""
				}
				return;
			}
			//println "localization: " + localization
		}
		
		// Display message in debug mode
		if (session.useDebugLocale) { 			
			def locales = grailsApplication.config.locale.supportedLocales			
			def localized = [:]
			def message = ""
			locales.each { 
				def locale = new Locale(it)
				def messageSource = grailsAttributes.applicationContext.messageSource
				//println messageSource
				message = messageSource.getMessage(attrs.code, attrs.args == null ? null : attrs.args.toArray(), attrs.default, locale)
				//message = messageSource.getMessage(attrs.code, locale)				
				localized.put(it, message)
			}
			def hasOthers = localized.values().findAll { word -> word != localized['en'] }
			attrs.locale = attrs.locale ?: session?.user?.locale ?: session.locale ?: defaultLocale;
			
			def image = (!hasOthers)?'error':'accept';
			// Has not been localized
			/*
			out << """<span class='localized-string'> 
						<img class='showLocalizationForm' data-id="${attrs.code}" src='${createLinkTo(dir:'images/icons/silk',file: image + '.png')}' title='${localized}'/> 
						${defaultTagLib.message.call(attrs)}							
					</span>
					<div id="${attrs.code}">test</div>
				"""
			*/
			message = "${defaultTagLib.message.call(attrs)}"
			//flash.localizations << ['code':attrs.code, 'text':message]
			out << """
					<span style="border: 3px solid lightgrey; padding: 2px;">
						<img class='show-localization-dialog' data-code="${attrs.code}" 
							data-locale="${attrs.locale}" 
							data-args="${attrs?.args?.join(',')}" 
							data-text="${message}" 
							data-localized="${localized}" 
							src="${createLinkTo(dir:'images/icons/silk',file: image + '.png')}" title="${localized}"/>
						${attrs.code} = ${message}
					</span>
					
				"""
			
		}
		// Display message normally
		else { 
			attrs.locale = attrs.locale ?: session?.user?.locale ?: session.locale ?: defaultLocale;
			out << defaultTagLib.message.call(attrs)
		}		
		//println "MessageTagLib.message() " + (System.currentTimeMillis() - startTime) + " ms"
	}
}
