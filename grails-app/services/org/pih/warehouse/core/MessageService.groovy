package org.pih.warehouse.core;

import grails.util.GrailsUtil;
import org.apache.commons.mail.SimpleEmail
import org.apache.commons.mail.HtmlEmail
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class MessageService {

	def grailsApplication

	public getMessage(String messageCode, Object [] args, String defaultMessage, Locale locale) {
		return grailsApplication.getMainContext().getMessage(messageCode, args, defaultMessage, locale)
	}
}
