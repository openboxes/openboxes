package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.persistence.Entity
import grails.testing.gorm.DataTest
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Location

class CommandUtilsSpec extends Specification implements TagLibUnitTest<ApplicationTagLib>, DataTest {

    void setupSpec() {
        mockDomain(CommandUtilsWidget)
    }

    @Unroll
    void 'getTypePropertyName is "#expected" for #type'() {
        expect:
        assert CommandUtils.getTypePropertyName(type) == expected

        where:
        type                || expected
        String              || 'string'
        Location            || 'location'
        BigDecimal          || 'bigDecimal'
        Map.Entry           || 'map.Entry'
    }

    void 'getAvailableProperties lists the persistent properties of an entity with readable names and types'() {
        when:
        List<Map> properties = CommandUtils.getAvailableProperties(CommandUtilsWidget.name)

        then:
        Map byName = properties.collectEntries { [it.name, it] }
        assert byName.unitPrice == [name: 'unitPrice', naturalName: 'Unit Price', typePropertyName: 'bigDecimal']
        assert byName.name == [name: 'name', naturalName: 'Name', typePropertyName: 'string']
        assert byName.dateCreated == [name: 'dateCreated', naturalName: 'Date Created', typePropertyName: 'date']
    }

    void 'getAvailableProperties returns an empty list for an unknown entity'() {
        expect:
        assert CommandUtils.getAvailableProperties('org.pih.warehouse.NoSuchEntity') == []
    }

    void 'buildErrorsGroupedByField groups localized error messages by field'() {
        given:
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)
        messageSource.addMessage('widget.name.blank', request.locale, 'Name is required')
        messageSource.addMessage('widget.name.tooLong', request.locale, 'Name is too long')

        and:
        Errors errors = new BeanPropertyBindingResult(new CommandUtilsWidget(), 'widget')
        errors.addError(fieldError('name', 'widget.name.blank', null))
        errors.addError(fieldError('name', 'widget.name.tooLong', null))
        errors.addError(fieldError('unitPrice', 'widget.unitPrice.unknownCode', 'Unit price must be positive'))

        when:
        Map<String, List<String>> grouped = CommandUtils.buildErrorsGroupedByField(errors)

        then:
        assert grouped == [
                name     : ['Name is required', 'Name is too long'],
                // Falls back to the default message when no translation exists for the code.
                unitPrice: ['Unit price must be positive'],
        ]
    }

    void 'buildErrorsGroupedByField returns an empty map when there are no errors'() {
        given:
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)

        expect:
        assert CommandUtils.buildErrorsGroupedByField(new BeanPropertyBindingResult(new CommandUtilsWidget(), 'widget')) == [:]
    }

    private static FieldError fieldError(String field, String code, String defaultMessage) {
        return new FieldError('widget', field, null, false, [code] as String[], null, defaultMessage)
    }
}

@Entity
class CommandUtilsWidget {
    String name
    BigDecimal unitPrice
    Date dateCreated
}
