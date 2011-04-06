package org.pih.warehouse.util;

import org.codehaus.groovy.grails.commons.ApplicationHolder

class FileUtil {

	public static String retrieveFile(String filePath) {
		if (ApplicationHolder.application.isWarDeployed()) {
			return ApplicationHolder.application.parentContext.getResource("classpath:$filePath")?.getFile()
		}
		else {
			return new File("grails-app/conf/$filePath")
		}
	}
	
}