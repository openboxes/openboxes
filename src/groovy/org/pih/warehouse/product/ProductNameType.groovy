package org.pih.warehouse.product;

public enum ProductNameType {

	BRAND('Brand'),
	GENERIC('Generic'),
	BRANDED_GENERIC('Branded Generic');
 
	String name

	ProductNameType(String name) { this.name = name; }

	static list() {
		[ BRAND, GENERIC, BRANDED_GENERIC]
	}
}

