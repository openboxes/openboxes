// Vendored from org.grails.plugins:excel-import:3.0.2 (Apache-2.0); the plugin has no Grails 7 release.
// Source: repo.grails.org excel-import-3.0.2-sources.jar. Unmodified except this header.
package org.grails.plugins.excelimport


public enum ExpectedPropertyType {

	IntType([name: 'number']),
	StringType([name: 'text']),
	DateType([name: 'date']),
	DateJavaType([name: 'date']),
	DoubleType([name: 'number']),
	EmailType([name: 'email']);


	final String userViewableName


	// Groovy 4: enum constructors must be private
	private ExpectedPropertyType(Map parameters = [:]) {
		this.userViewableName = parameters?.name ?: this.name()
	}


}
