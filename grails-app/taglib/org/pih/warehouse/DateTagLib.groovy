package org.pih.warehouse

class DateTagLib {
    def thisYear = {
    	out << Calendar.getInstance().get(Calendar.YEAR)
    }
}
