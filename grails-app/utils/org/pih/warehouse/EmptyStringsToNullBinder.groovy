package org.pih.warehouse

import grails.databinding.BindingHelper
import grails.databinding.DataBindingSource

class EmptyStringsToNullBinder<T> implements BindingHelper<T> {

    @Override
     T getPropertyValue(Object obj, String propertyName, DataBindingSource source) {
        return source[propertyName] == "" ? null : source[propertyName]
    }
}
