package org.pih.warehouse.regression.platform.util

import spock.lang.PendingFeature

import org.pih.warehouse.util.*
import spock.lang.Specification
import util.RequestParamsUtil
import util.StringUtil

import org.pih.warehouse.core.identification.RandomIdentifierGenerator
import org.pih.warehouse.dashboard.ColorNumber
import org.pih.warehouse.dashboard.NumbersIndicator

class StringAndParamsUtilSpec extends Specification {

    void 'mask hides every character'() {
        expect:
        StringUtil.mask('secret') == '******'
        StringUtil.mask('pin', '#') == '###'
        StringUtil.mask('') == ''
        StringUtil.mask(null) == null
    }

    void 'templates and message formats are rendered with their arguments'() {
        expect:
        StringUtil.renderTemplate('Hello ${name}', [name: 'Jo']) == 'Hello Jo'
        StringUtil.format('{0} of {1}', ['3', '5'] as Object[]) == '3 of 5'
        StringUtil.format('PARTIALLY_RECEIVED') == 'Partially received'
    }

    void 'split turns a delimited string into a list'() {
        expect:
        StringUtil.split('a,b,c') == ['a', 'b', 'c']
        StringUtil.split('a|b', '\\|') == ['a', 'b']
        StringUtil.split('   ') == []
        StringUtil.split(null) == null
        StringUtil.split('') == null
    }

    void 'split rejects anything but a string'() {
        when:
        StringUtil.split(42)

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Expected String but got class java.lang.Integer'
    }

    @PendingFeature(reason = 'RequestParamsUtil.asList(null) switches on param.class: NPE')
    void 'request params become a list of strings'() {
        expect:
        RequestParamsUtil.asList(['a', 'b']) == ['a', 'b']
        RequestParamsUtil.asList(['x', 'y'] as String[]) == ['x', 'y']
        RequestParamsUtil.asList('a,b') == ['a', 'b']
        RequestParamsUtil.asList(null) == null
    }

    void 'request params of other types are rejected'() {
        when:
        RequestParamsUtil.asList(5)

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Invalid type in request param: class java.lang.Integer'
    }

    void 'identifiers follow the format, replacing the placeholders with random characters'() {
        given:
        RandomIdentifierGenerator generator = new RandomIdentifierGenerator(
                numericValues: '7', alphabeticValues: 'Q', alphanumericValues: 'Z')

        expect:
        generator.generate('PO-NNDLA') == 'PO-777QZ'
        generator.generate('') == null
        generator.generate(null) == null
    }

    void 'color numbers are colored by thresholds and serialize to json'() {
        expect:
        new ColorNumber(value).with { setConditionalColors(50, 90); it.color } == expected

        where:
        value || expected
        95    || 'success'
        90    || 'success'
        60    || 'warning'
        10    || 'error'
    }

    void 'color numbers and number indicators serialize to json'() {
        given:
        ColorNumber first = new ColorNumber(5, 'Open', '/open', 'error', 7, 1)
        ColorNumber second = new ColorNumber(value: 6, subtitle: 'Late', link: '/late', color: 'warning', value2: 8, order: 2)

        expect:
        first.toJson() == [value: 5, subtitle: 'Open', link: '/open', color: 'error', value2: 7, order: 1]
        second.toJson() == [value: 6, subtitle: 'Late', link: '/late', color: 'warning', value2: 8, order: 2]
        new ColorNumber(3).toJson().subtitle == null
        new NumbersIndicator(first, second, first).toJson() ==
                [first: first.toJson(), second: second.toJson(), third: first.toJson(), fourth: null]
        new NumbersIndicator(first, second, first, second).toJson().fourth == second.toJson()
    }
}
