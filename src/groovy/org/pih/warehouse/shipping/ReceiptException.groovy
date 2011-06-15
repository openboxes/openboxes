package org.pih.warehouse.shipping

import org.pih.warehouse.receiving.Receipt;

class ReceiptException extends RuntimeException {
	String message
	Receipt receipt		
}
