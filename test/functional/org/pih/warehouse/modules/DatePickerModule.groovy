package org.pih.warehouse.modules

import geb.Module


class DatePickerModule extends  Module{
    static content = {
        today(wait: true){$("td.ui-datepicker-today a")}
        yesterday(wait: true){$("td.ui-datepicker-today").previous().find("a")}
        tomorrow(wait: true){$("td.ui-datepicker-today").next().find("a")}
    }
}
