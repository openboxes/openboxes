package org.pih.warehouse.core;

public enum TransactionTypes {

	DEBIT('Debit'),
	CREDIT('Credit'),
	TRANSFER('Transfer'); 

	String name

	TransactionTypes(String name) { this.name = name; }

	static list() {
		[ DEBIT, CREDIT, TRANSFER]
	}
}

