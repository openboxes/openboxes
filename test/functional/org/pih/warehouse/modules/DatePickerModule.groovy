package org.pih.warehouse.modules

import geb.Module


class DatePickerModule extends  Module{
    static content = {
        today(wait: true){$("td.ui-datepicker-today a")}
        yesterday(wait: true){$("td.ui-datepicker-today").previous().find("a")}
        tomorrow(wait: true){$("td.ui-datepicker-today").next().find("a")}
    }

    def pickDate(date){

        def year = date.format("yyyy")
        waitFor{ $("select.ui-datepicker-year")}.value(year)

        def day = (date.format("dd").toInteger() - 1).toString()
        def start = new Date()
        def end = date
        def monthDiff = (end[Calendar.MONTH]-start[Calendar.MONTH])+((end[Calendar.YEAR]-start[Calendar.YEAR])*12)
        println "monthDiff: ${monthDiff}"
        def nextOrPrev = monthDiff > 0 ?  ".ui-datepicker-next" : ".ui-datepicker-prev"
        for(int i=0; i< monthDiff.abs(); i++){
               waitFor{$(nextOrPrev)}.click()
               println "${nextOrPrev} once"
        }

        waitFor{ $("table.ui-datepicker-calendar a")}.find{ it.text() == day}.click()
    }

}
