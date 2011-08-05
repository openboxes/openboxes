package org.pih.warehouse.util


class DateUtil {

	static Date clearTime(Date date) { 
		Calendar calendar = Calendar.getInstance();
		if (date) { 
			calendar.setTime(date);
			// Set time fields to zero
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			  
			// Put it back in the Date object
			date = calendar.getTime();			
		}
		return date;
	}
	
}
