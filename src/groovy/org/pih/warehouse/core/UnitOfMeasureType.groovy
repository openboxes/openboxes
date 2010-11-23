package org.pih.warehouse.core;

/**
 * http://en.wikipedia.org/wiki/Category:Units_of_measure
 */
public enum UnitOfMeasureType {

	AREA('Area'),
	CURRENCY('Currency'),	
	LENGTH('Length'),
	MASS('Mass'),
	QUANTITY('Quantity'),
	VOLUME('Volume');
	
	String name

	UnitOfMeasureType(String name) { this.name = name; }

	static list() {
		[ AREA, CURRENCY, LENGTH, MASS, QUANTITY, VOLUME]
	}
}

