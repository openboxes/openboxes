package org.pih.warehouse

class MessageTagLib {
   
	static namespace = "warehouse"
	
	Locale defaultLocale = new Locale(grailsApplication.config.locale.defaultLocale)
	
	def message = { attrs, body ->		
		def defaultTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib')
		if (session.useDebugLocale) { 
			
			def locales = grailsApplication.config.locale.supportedLocales			
			def localized = [:]
			locales.each { 
				def locale = new Locale(it)
				def messageSource = grailsAttributes.applicationContext.messageSource
				//println messageSource
				def message = messageSource.getMessage(attrs.code, attrs.args == null ? null : attrs.args.toArray(),
					attrs.default, locale)
				localized.put(it, message)
			}
			
			def hasOthers = localized.values().findAll { word -> word != localized['en'] }
			attrs.locale = attrs.locale ?: session?.user?.locale ?: session.locale ?: defaultLocale;
			
			if (!hasOthers) { 
				out << """<span class='localized-string'> 
							${defaultTagLib.message.call(attrs)}
							<span class='text' style='display:none;'>${attrs.code} = ${localized}</span> 
							<img class='copy' src='${createLinkTo(dir:'images/icons/silk',file:'decline.png')}' title='${localized}'/> 
						</span>"""
			} else { 
				out << """<span class='localized-string'> 
							${defaultTagLib.message.call(attrs)}
							<span class='text' style='display:none;'>${attrs.code} = ${localized}</span> 
							<img class='copy' src='${createLinkTo(dir:'images/icons/silk',file:'accept.png')}' title='${localized}'/> 
						</span>"""
			}
		}
		else { 
			attrs.locale = attrs.locale ?: session?.user?.locale ?: session.locale ?: defaultLocale;
			out << defaultTagLib.message.call(attrs)
		}		
	}
}
