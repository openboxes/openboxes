package org.pih.warehouse.inventory

import spock.lang.Unroll

import grails.testing.gorm.DomainUnitTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

class TransactionTypeSpec  extends Specification implements DomainUnitTest<TransactionType> {


    @Test
    @Unroll('TransactionType.validate() with name: #value should return #expected with errorCode: #expectedErrorCode')
    void 'name field validation'() {
        when:
        domain.name = value

        then:
        assert domain.validate(['name']) == expected
        assert domain.errors.getFieldError("name")?.getCode() == expectedErrorCode

        where:
        value                                             || expected | expectedErrorCode
        null                                              || false    | 'nullable'
        'this_is_big_string_to_test_max_length_255' * 255 || false    | 'maxSize.exceeded'
        'Dummy Name'                                      || true     | null
    }

    @Test
    @Unroll('TransactionType.validate() with description: #value should return #expected with errorCode: #expectedErrorCode')
    void 'description field validation'() {
        when:
        domain.description = value

        then:
        assert domain.validate(['description']) == expected
        assert domain.errors.getFieldError("description")?.getCode() == expectedErrorCode

        where:
        value                                             || expected | expectedErrorCode
        null                                              || true     | null
        'this_is_big_string_to_test_max_length_255' * 255 || false    | 'maxSize.exceeded'
        'Dummy description'                               || true     | null
    }

    @Test
    void 'all fields validation'() {
        when:
        domain

        then:
        assert !domain.validate()
        assert domain.errors.allErrors.size() == 2
    }

    @Test
    @Unroll('TransactionType.validate() with transactionCode: #value should return #expected with errorCode: #expectedErrorCode')
    void 'transactionCode field validation'() {
        when:
        domain.transactionCode = value

        then:
        assert domain.validate(['transactionCode']) == expected
        assert domain.errors.getFieldError("transactionCode")?.getCode() == expectedErrorCode

        where:
        value                 || expected | expectedErrorCode
        null                  || false    | 'nullable'
        TransactionCode.DEBIT || true     | null
    }

    @Test
    @Unroll('TransactionType.isAdjustment() with isAdjustment: #value should return #expected')
    void 'isAdjustment field validation'() {
        when:
        domain.name = value

        then:
        domain.isAdjustment() == expected

        where:
        value           || expected
        'NO_Adjustment' || false
        'Adjustment'    || true
    }


}
