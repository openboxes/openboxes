package org.pih.warehouse.core;

/**
 * http://en.wikipedia.org/wiki/Category:Units_of_measure
 */
public enum UnitOfMeasureType {

	AREA('Area'),
	CURRENCY('Currency'),	
	LENGTH('Length'),
	MASS('Mass'),
	PACKAGE('Package'),
	QUANTITY('Quantity'),
	VOLUME('Volume');
	
	String name

	UnitOfMeasureType(String name) { this.name = name; }

	static list() {
		[ AREA, CURRENCY, LENGTH, MASS, PACKAGE, QUANTITY, VOLUME]
	}
}

