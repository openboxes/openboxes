package org.pih.warehouse

class DataTagLib {
    def thisYear = {
	out << Calendar.getInstance().get(Calendar.YEAR)
    }
}
