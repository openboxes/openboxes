package org.pih.warehouse.core

import java.text.DateFormat;
import java.text.SimpleDateFormat;


class Constants {
	
	static final String DEFAULT_DATE_FORMAT = "dd/MMM/yyyy"
	static final String DEFAULT_DATE_TIME_FORMAT = "dd/MMM/yyyy HH:mm:ss z"
	static final String DEFAULT_TIME_FORMAT = "HH:mm:ss z"
	static final String DEFAULT_MONTH_YEAR_DATE_FORMAT = "MMM yyyy"
	
	static final DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_FORMAT);	

	static final String DEFAULT_WEIGHT_UNITS = "lbs"
	static final String DEFAULT_VOLUME_UNITS = "ft"
	
	static final ArrayList WEIGHT_UNITS = ["lbs","kg"]
	static final ArrayList VOLUME_UNITS = ["ft","m"]
	         
	static final Float POUNDS_PER_KILOGRAM = 0.45359237;
	static final Float KILOGRAMS_PER_POUND = 2.20462262;
	                        
	static final ArrayList COLORS = ['FFFFFF','FFDFDF','FFBFBF','FF9F9F','FF7F7F','FF5F5F','FF3F3F','FF1F1F','FF0000','DF1F00','C33B00','A75700','8B7300','6F8F00','53AB00','37C700','1BE300','00FF00','00DF1F','00C33B','00A757','008B73','006F8F','0053AB','0037C7','001BE3','0000FF','0000df','0000c3','0000a7','00008b','00006f','000053','000037','00001b','000000' ];
	      
	// these are direct references to transaction types by primary key
	static final int INVENTORY_TRANSACTION_TYPE_ID = 7
	static final int TRANSFER_IN_TRANSACTION_TYPE_ID = 8
	static final int TRANSFER_OUT_TRANSACTION_TYPE_ID = 9
	static final int PRODUCT_INVENTORY_TRANSACTION_TYPE_ID = 11
	
	// direct references to location by primary key
	static final int WAREHOUSE_LOCATION_TYPE_ID = 1
	
}
