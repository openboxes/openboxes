package org.pih.warehouse.inventory

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class TransactionTypeSpec  extends Specification implements DomainUnitTest<TransactionType> {

    @Unroll
    def 'should return #expected with error code #expectedErrorCode when validating transaction type given name #value'() {
        given: "the name is #value"
        domain.name = value

        when: "the transaction type is validated"
        boolean actual = domain.validate(["name"])

        then: "the name property should be valid or invalid with the expected error code"
        actual == expected
        domain.errors["name"]?.code == expectedErrorCode

        where:
        value        || expected | expectedErrorCode
        null         || false    | 'nullable'
        'a' * 256    || false    | 'maxSize.exceeded'
        'b' * 255    || true     | null
        'Dummy Name' || true     | null
    }

    @Unroll
    void 'should return #expected with error code #expectedErrorCode when validating transaction type given name #value'() {
        when:
        domain.description = value

        then:
        assert domain.validate(['description']) == expected
        assert domain.errors.getFieldError("description")?.getCode() == expectedErrorCode

        where:
        value               || expected | expectedErrorCode
        null                || true     | null
        'a' * 256           || false    | 'maxSize.exceeded'
        'Dummy description' || true     | null
    }

    void 'should expect two validation errors when validating given an empty transaction type'() {
        given:
        domain

        when:
        boolean isValid = domain.validate()

        then:
        assert !isValid
        assert domain.errors.errorCount == 2
    }

    void 'should expect the names to be the same'() {
        given: "a valid transaction type with multiple"
        TransactionType transactionType =
                new TransactionType(name: "Debit|sp:Débito|fr:Débit", transactionCode: TransactionCode.DEBIT)

        when: "the name is compared with debit"
        boolean nameIsSame = transactionType.compareName("Debit")

        then: "the name is the same"
        assert nameIsSame
    }

    @Unroll
    void 'should return #expected with error code #expectedErrorCode when validating transaction type given name #value'() {
        given:
        domain.transactionCode = value

        when:
        boolean actual = domain.validate(['transactionCode'])

        then:
        actual == expected
        domain.errors.getFieldError("transactionCode")?.code == expectedErrorCode

        where:
        value                 || expected | expectedErrorCode
        null                  || false    | 'nullable'
        TransactionCode.DEBIT || true     | null
    }

    @Unroll
    void 'should return #expected when given name #value'() {
        given:
        domain.name = value

        when:
        boolean isAdjustment = domain.isAdjustment()

        then:
        isAdjustment == expected

        where:
        value                                      || expected
        'Adjustment|fr:Ajustement|es:Ajustamiento' || true
        'NO_Adjustment'                            || false
        'Adjustment'                               || true
    }
}
