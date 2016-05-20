/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core

import org.springframework.web.context.request.RequestContextHolder
import org.pih.warehouse.util.LocalizationUtil

class LocalizationService {

	// TODO: do we need to make this read-only?
    boolean transactional = false

	// session-scoped (because it needs access to the user)
	//static scope = "session"
	
	// inject the grails application so we can access the default locale
	def grailsApplication
	
	
	String formatMetadata(Object object) {
		def format = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')	
		return format.metadata(obj: object)
	}
	
	String formatDate(Date date) { 
		def format = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
		return format.date(obj: date)
	}
	
	/**
	 * Localizes the passed string value based on the current locale
	 */
	String getLocalizedString(String value) {
	
		// null check
		if (!value) {
			return value
		}
		
		return LocalizationUtil.getLocalizedString(value, getCurrentLocale())
	}			

	/**
	 * Gets the current locale
	 */
	Locale getCurrentLocale() {
		// fetch the locale of the current user; if there isn't one, use the default locale
		return (RequestContextHolder.currentRequestAttributes().getSession().user?.locale ?: new Locale(grailsApplication.config.openboxes.locale.defaultLocale))
	}
}
