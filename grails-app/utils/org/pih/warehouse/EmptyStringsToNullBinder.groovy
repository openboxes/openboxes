package org.pih.warehouse

import grails.databinding.BindingHelper
import grails.databinding.DataBindingSource

class EmptyStringsToNullBinder<T> implements BindingHelper<T> {

    static final T bindEmptyStringToNull(Object source, String fieldName) {
        return source[fieldName] == "" ? null : source[fieldName]
    }

    @Override
    T getPropertyValue(Object obj, String propertyName, DataBindingSource source) {
        return bindEmptyStringToNull(source, propertyName)
    }
}
