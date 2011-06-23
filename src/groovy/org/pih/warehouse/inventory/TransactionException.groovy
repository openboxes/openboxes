package org.pih.warehouse.inventory

class TransactionException extends RuntimeException {
	String message
	Transaction transaction
}
