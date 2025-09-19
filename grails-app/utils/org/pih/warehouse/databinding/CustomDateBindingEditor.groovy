package org.pih.warehouse.databinding

import grails.databinding.TypedStructuredBindingEditor
import java.time.Instant
import org.grails.databinding.converters.AbstractStructuredDateBindingEditor
import org.springframework.stereotype.Component

/**
 * As of Java 8, java.util.Date is functionally replaced with the java.time classes, but Grails 4 and older does not
 * support databinding to java.time classes so we need to add support ourselves.
 * https://github.com/grails/grails-core/issues/11811
 *
 * Unlike Value Converters, which bind/remap single field inputs (such as a String) to a specific type or object (such
 * as Instant), Binding Editors are used to bind data structures (ie Maps of params) to a specific type or object.
 *
 * We only use Binding Editors when working with Grails' date <g:datePicker> taglib. When binding date fields during
 * API requests, the Value Converters are used.
 *
 * AbstractStructuredDateBindingEditor is provided by Grails as a way to handle the data structure provided by the
 * <g:datePicker> taglib. For example, if your domain has a field "startDate", the date picker might produce
 * the following params:
 *
 *   startDate:date.struct
 *   startDate_day:1
 *   startDate_month:1
 *   startDate_year:1980
 *   startDate_hour:00
 *   startDate_minute:00
 *
 * And AbstractStructuredDateBindingEditor provides a means to bind those params to a single Calendar object.
 *
 * Implementations of this class extend this databinding further to convert the Calendar object to additional types,
 * mainly the java.time classes. This allows us to use Grails' date picker on java.time classes.
 *
 * More info at: https://github.com/apache/grails-core/blob/7.0.x/grails-doc/src/en/guide/theWebLayer/controllers/dataBinding.adoc#structured-data-binding-editors
 *
 * Copied from https://github.com/apache/grails-core/blob/7.0.x/grails-databinding/src/main/groovy/org/grails/databinding/converters/Jsr310ConvertersConfiguration.groovy
 */
abstract class CustomDateBindingEditor<T> extends AbstractStructuredDateBindingEditor<T> implements TypedStructuredBindingEditor<T> {

}
