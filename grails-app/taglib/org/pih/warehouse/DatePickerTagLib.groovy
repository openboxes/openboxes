/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.apache.commons.lang.StringUtils
import org.grails.plugins.web.taglib.FormTagLib

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.session.SessionManager

class DatePickerTagLib {

    SessionManager sessionManager
    ConfigService configService

    /**
     * Simple date + time form field selectors for dates.
     *
     * Prefer using jqueryDatePicker for date-only fields since it's more user friendly and supports java.time
     * classes in a more straightforward way.
     *
     * Will produce a structure of params based on the selected date. For example, if name="startDate", the date
     * picker might produce the following params:
     *
     *   startDate:date.struct
     *   startDate_day:1
     *   startDate_month:1
     *   startDate_year:1980
     *   startDate_hour:00
     *   startDate_minute:00
     *
     * See {@link org.pih.warehouse.databinding.CustomDateBindingEditor} implementations to understand how
     * these params are bound to individual fields.
     *
     * More info: https://docs.grails.org/latest/ref/Tags%20-%20GSP/datePicker.html
     */
    Closure datePicker = { attrs, body ->
        FormTagLib datePickerTagLib = grailsApplication.mainContext.getBean(
                'org.grails.plugins.web.taglib.FormTagLib') as FormTagLib

        // It'd be nice if we could be clever about precision (ie automatically set the precision to 'minute' for
        // Instant and ZonedDateTime fields and 'day' for LocalDate fields) but when the value is null (ie the date
        // picker is blank), we don't know what type we're working with and so we never enter the switch cases below.
        if (StringUtils.isBlank(attrs.precision as String)) {
            attrs.precision = configService.getProperty('grails.tags.datePicker.default.precision') ?: 'minute'
        }

        // We display datetimes in the user's timezone (date-only fields don't have a timezone component) because
        // it makes sense for users to want to pick datetimes in their own timezone. If we don't do this, the date
        // they pick will be in the server timezone.
        ZoneId zoneId = sessionManager.timezone.toZoneId()

        // Grails' date picker only supports java.util.Date and Calendar types. When working with java.time classes
        // we opt to covert to Calendar because it supports zones which makes our logic simpler. We just convert the
        // date to the user's timezone.
        // Note that the Grails datepicker actually ignores the zone set in the Calendar instance (crazy!) so our
        // BindingEditor classes need to manually set the user's zone again when marshalling fields.
        switch (attrs.value) {
            case (Instant):
                ZonedDateTime zonedDateTime = DateUtil.asZonedDateTime(attrs.value as Instant, zoneId)
                attrs.value = GregorianCalendar.from(zonedDateTime)
                break
            case (ZonedDateTime):
                ZonedDateTime zonedDateTime = (attrs.value as ZonedDateTime).withZoneSameInstant(zoneId)
                attrs.value = GregorianCalendar.from(zonedDateTime)
                break
            case (LocalDate):
                LocalDate localDate = attrs.value as LocalDate
                // -1 to month because Calendar is zero-indexed but LocalDate is not.
                attrs.value = new GregorianCalendar(localDate.year, localDate.monthValue - 1, localDate.dayOfMonth)

                if (['hour', 'minute'].contains(attrs.precision)) {
                    throw new IllegalArgumentException(
                            "LocalDate fields don't have a time component and so can't have hour or minute precision")
                }
                break
            // For everything else, (including java.time.Date) we simply pass through to the Grails taglib.
            default:
                break
        }

        // If we don't specify a value or a default then the Grails date picker will default to selecting "new Date()",
        // which uses the server timezone. We want to use the user's timezone instead.
        if (attrs.value == null && attrs.default == null) {
            attrs.value = GregorianCalendar.from(ZonedDateTime.now(zoneId))
        }

        out << datePickerTagLib.datePicker.call(attrs)
    }

    /**
     * A date picker widget for date-only dates.
     * More info: https://jqueryui.com/datepicker/
     *
     * @attr id The id of the datepicker element. If not set, will use the value of name.
     * @attr name Required. The name of the field to bind the date to.
     * @attr value A Date or LocalDate instance to populate the field with initially.
     * @attr autoSize If true, automatically resizes the input field to fit the date string.
     *                Defaults to true if size is null, otherwise defaults to false.
     * @attr size The number of characters wide the input field should be. Defaults to 10.
     * @attr cssClass An optional CSS class to associate with the date picker element.
     * @attr changeMonthAndYear If true, shows month and year as dropdown selectors. Defaults to true.
     * @attr numberOfMonths The number of months to display at one time in the picker. Defaults to 1.
     * @attr readOnly If true, the picker will be disabled. Defaults to false.
     * @attr minDate Date string representing the earliest date that can be selected. Defaults to no limit.
     * @attr maxDate Date string representing the latest date that can be selected. Defaults to no limit.
     * @attr placeholder The text to display in the input field before any date has been selected. Defaults to empty.
     * @attr autocomplete If true, enables autocomplete for the input field. Defaults to false.
     */
    Closure jqueryDatePicker = { attrs, body ->

        def id = attrs.id ? attrs.id : attrs.name
        def name = attrs.name
        def autoSize = attrs.autoSize ?: (attrs.size) ? "false" : "true"
        def size = attrs.size ?: "10"
        def cssClass = attrs.cssClass ?: ""
        def showOn = attrs.showOn ?: "both"
        def showTrigger = Boolean.valueOf(attrs.showTrigger ?: "true")
        def changeMonthAndYear = attrs.changeMonthAndYear ?: true
        def numberOfMonths = attrs.numberOfMonths ?: 1
        def readOnly = attrs.readOnly ?: false
        def minDate = attrs.minDate ? "new Date('${attrs.minDate}')" : null
        def maxDate = attrs.maxDate ? "new Date('${attrs.maxDate}')" : null
        def dataBind = attrs.dataBind ? "data-bind='${attrs.dataBind}'" : ""
        def placeholder = attrs.placeholder ?: ''
        def autocomplete = attrs.autocomplete ?: 'off'
        def dataTestId = attrs['data-testid'] ?: 'date-picker'

        // If a value was provided on page load, stringify it for the date picker to use as a default. Note that the
        // date picker uses the javascript Date type (not java.util.Date), which has a different pattern/format rules
        // and a constructor that defaults to midnight in the server timezone if given date-only (no time or offset).
        // It's unclear why, but the date string must be formatted to match the 'altFormat' format (MM/dd/yyyy).
        // Otherwise we get weird behaviour (the selected dates shift by one day every time the page refreshes).
        String value
        switch (attrs.value) {
            case Date:
                value = Constants.MONTH_DAY_YEAR_DATE_FORMATTER.format(attrs.value)
                break
            case LocalDate:
                value = DateTimeFormatter.ofPattern('MM/dd/yyyy').format(attrs.value as LocalDate)
                break
            case String:
                String stringValue = attrs.value as String
                value = StringUtils.isBlank(stringValue) || stringValue == 'null' ? null : attrs.value
                break
            case null:
                value = null
                break
            default:
                throw new IllegalArgumentException("Can't populate datepicker with value of type: ${attrs.value.class}")
        }

        if (name == null) {
            throw new IllegalArgumentException("name parameter must be specified")
        }

        def html = """
			<input id='${id}' name='${name}' type='hidden' ${dataBind}/>
			<input id='${id}-datepicker' name='${name}-datepicker' autocomplete ='${autocomplete}' type='text'
					data-testid='${dataTestId}' placeholder='${placeholder}' class='${cssClass} text large' size="${size}" ${
            readOnly ? "readonly='readonly'" : ""
        }/>
			<script type=\'text/javascript\'>

				jQuery(document).ready(function() {

					jQuery(".clear-date").click(function() {
						jQuery('#${id}-datepicker').val('');
					});

					jQuery('#${id}-datepicker').datepicker({
                        // We use the alt field so that we can continue to display the date in a user friendly format
                        // while doing all logical operations on the date (including data binding) in another format.
                        // It would have been better to use ISO format (yy-mm-dd) but many usages of this picker
                        // expect the mm/dd/yy format so we're stuck with it. Also note that the format rules for
                        // javascript's 'Date is different from the format for java.util.Date
						altField: '#${id}',
						altFormat: 'mm/dd/yy',
						dateFormat: 'dd/M/yy',
						autoSize: ${autoSize},
						//showOn: '${showOn}',
                        numberOfMonths: ${numberOfMonths},
						changeMonth: ${changeMonthAndYear},
						changeYear: ${changeMonthAndYear},
						//buttonImageOnly: true,
						//buttonImage: '${request.contextPath}/images/icons/silk/calendar.png',
						minDate: ${minDate},
						maxDate: ${maxDate}
						//buttonText: '...',
						//showButtonPanel: true,
						//showOtherMonths: true,
						//selectOtherMonths: true

					}).keyup(function(e) {
                        if(e.keyCode == 8 || e.keyCode == 46) {
                            \$.datepicker._clearDate(this);
                        }
                    });

					// If we reset the date, we need to reset the hidden form field as well.
					jQuery('#${id}-datepicker').change(function() {
					    try {
					        var d = \$.datepicker.parseDate('dd/M/yy', this.value);
                        } catch(err) {
                            jQuery('#${id}-datepicker').val('');
                            jQuery('#${id}').trigger('change');
                        }

					});

					// Set the date value if one was provided
					var dateValue = '${value}';
					if (dateValue && dateValue != 'null') {
						jQuery('#${id}-datepicker').datepicker('setDate', new Date('${value}'));
					}
				});
			</script>"""

        out << html

    }
}
