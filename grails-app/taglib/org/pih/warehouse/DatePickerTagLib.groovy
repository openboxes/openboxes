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

import java.text.SimpleDateFormat

class DatePickerTagLib {


    def datePicker = { attrs, body ->
        def datePickerTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.FormTagLib')
        out << datePickerTagLib.datePicker.call(attrs)
    }

    def jqueryDatePicker = { attrs, body ->

        def id = attrs.id ? attrs.id : attrs.name
        def name = attrs.name
        def autoSize = attrs.autoSize ?: (attrs.size) ? "false" : "true"
        def size = attrs.size ?: "10"
        def cssClass = attrs.cssClass ?: ""
        def showOn = attrs.showOn ?: "both"
        def showTrigger = Boolean.valueOf(attrs.showTrigger ?: "true")
        def changeMonthAndYear = attrs.changeMonthAndYear ?: true
        def value = attrs.value
        def numberOfMonths = attrs.numberOfMonths ?: 1
        def readOnly = attrs.readOnly ?: false
        def minDate = attrs.minDate ? "new Date('${attrs.minDate}')" : null
        def maxDate = attrs.maxDate ? "new Date('${attrs.maxDate}')" : null
        def dataBind = attrs.dataBind ? "data-bind='${attrs.dataBind}'" : ""
        def placeholder = attrs.placeholder ?: ''
        def autocomplete = attrs.autocomplete ?: 'on'

        if (value) {
            if (value instanceof Date) {
                value = (attrs.format && attrs.value) ? new SimpleDateFormat(attrs.format).format(attrs.value) : ""
            }
        }

        if (name == null) {
            throw new IllegalArgumentException("name parameter must be specified")
        }

        def html = """
			<input id='${id}' name='${name}' type='hidden' ${dataBind}/>
			<input id='${id}-datepicker' name='${name}-datepicker' autocomplete ='${autocomplete}' type='text'
					placeholder='${placeholder}' class='${cssClass} text large' size="${size}" ${
            readOnly ? "readonly='readonly'" : ""
        }/>
			<script type=\'text/javascript\'>

				jQuery(document).ready(function() {

					jQuery(".clear-date").click(function() {
						jQuery('#${id}-datepicker').val('');
					});

					jQuery('#${id}-datepicker').datepicker({
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
