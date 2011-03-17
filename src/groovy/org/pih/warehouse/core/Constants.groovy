package org.pih.warehouse.core


class Constants {
	
	static final String DEFAULT_WEIGHT_UNITS = "lbs"
	static final String DEFAULT_VOLUME_UNITS = "ft"
	
	static final ArrayList WEIGHT_UNITS = ["lbs","kg"]
	static final ArrayList VOLUME_UNITS = ["ft","m"]
	                                       
	// TODO: these are direct references to transaction types by primary key
	// once we add a enum transaction_code to transaction type, we can remove these
	static final int TRANSFER_IN_TRANSACTION_TYPE_ID = 8
	static final int TRANSFER_OUT_TRANSACTION_TYPE_ID = 9
	
}
