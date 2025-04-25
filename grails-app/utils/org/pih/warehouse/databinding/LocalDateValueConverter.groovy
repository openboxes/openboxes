package org.pih.warehouse.databinding

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * As of Java 8, Java.util.Date is functionally replaced with the java.time classes, but Grails 4 and older does not
 * support databinding a datetime String to a LocalDate so we need to add support ourselves.
 * https://github.com/grails/grails-core/issues/11811
 */
@Component
class LocalDateValueConverter extends StringValueConverter<LocalDate> {

    private static final DateTimeFormatter FLEXIBLE_DATE_FORMAT = DateTimeFormatter.ofPattern(
            DataBindingConstants.FLEXIBLE_DATE_PATTERN)

    /**
     * Parse a given String into a LocalDate of the given format.
     *
     * Because LocalDate is time and timezone agnostic, the String only expects day, month, and year elements. To avoid
     * any timezone related confusion, Any additional data provided (such as time and timezone) will trigger an error.
     *
     * @param value "2000/01/01" for example
     */
    @Override
    LocalDate convertString(String value) {
        return StringUtils.isBlank(value) ?
                null :
                LocalDate.parse(value.trim(), FLEXIBLE_DATE_FORMAT)
    }
}
