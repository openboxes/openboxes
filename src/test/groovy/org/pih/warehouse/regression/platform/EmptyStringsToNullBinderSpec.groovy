package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.databinding.SimpleMapDataBindingSource
import spock.lang.Specification
import spock.lang.Unroll

class EmptyStringsToNullBinderSpec extends Specification {

    @Unroll
    void 'bindEmptyStringToNull binds #value to #expected'() {
        expect:
        assert EmptyStringsToNullBinder.bindEmptyStringToNull([field: value], 'field') == expected

        where:
        value  || expected
        ''     || null
        null   || null
        // Only the empty string is nulled; whitespace and other values are kept as they are.
        ' '    || ' '
        'abc'  || 'abc'
        0      || 0
    }

    void 'bindEmptyStringToNull returns null for a missing field'() {
        expect:
        assert EmptyStringsToNullBinder.bindEmptyStringToNull([:], 'field') == null
    }

    void 'getPropertyValue reads the named property from the binding source'() {
        given:
        EmptyStringsToNullBinder<String> binder = new EmptyStringsToNullBinder<>()
        SimpleMapDataBindingSource source = new SimpleMapDataBindingSource([name: 'Aspirin', code: ''])

        expect:
        assert binder.getPropertyValue(new Object(), 'name', source) == 'Aspirin'
        assert binder.getPropertyValue(new Object(), 'code', source) == null
    }
}
