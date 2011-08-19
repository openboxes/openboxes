package org.pih.warehouse.core

import org.springframework.web.context.request.RequestContextHolder
import org.pih.warehouse.util.LocalizationUtil

class LocalizationService {

	// TODO: do we need to make this read-only?
    boolean transactional = false

	// session-scoped (because it needs access to the user)
	static scope = "session"
	
	// inject the grails application so we can access the default locale
	def grailsApplication
	
	
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
		return (RequestContextHolder.currentRequestAttributes().getSession().user?.locale ?: new Locale(grailsApplication.config.locale.defaultLocale))
	}
}
