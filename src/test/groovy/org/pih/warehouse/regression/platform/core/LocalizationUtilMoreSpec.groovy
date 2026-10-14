// Companion spec: extra features for upstream's LocalizationUtilSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionType

class LocalizationUtilMoreSpec extends Specification implements GrailsUnitTest {

    @Unroll
    void 'getLocale parses "#localeCode" with either separator'() {
        expect:
        assert LocalizationUtil.getLocale(localeCode) == expected

        where:
        localeCode || expected
        'en'       || Locale.ENGLISH
        'es_MX'    || new Locale('es', 'MX')
        'es-MX'    || new Locale('es', 'MX')
        'en_GB'    || Locale.UK
        'fr-CA'    || Locale.CANADA_FRENCH
    }

    @Unroll
    void 'getLocalizedString returns "#expected" for "#value" in locale #locale'() {
        expect:
        assert LocalizationUtil.getLocalizedString(value, locale) == expected

        where:
        value                                         | locale                 || expected
        null                                          | Locale.FRENCH          || ''
        ''                                            | Locale.FRENCH          || ''
        // Splitting a lone delimiter yields no values at all.
        '|'                                           | Locale.FRENCH          || ''
        'Default'                                     | Locale.FRENCH          || 'Default'
        'Default|fr:Defaut|es:Defecto'                | null                   || 'Default'
        'Default|fr:Defaut|es:Defecto'                | Locale.FRENCH          || 'Defaut'
        'Default|fr:Defaut|es:Defecto'                | new Locale('es')       || 'Defecto'
        // Only the language is compared, so a country variant still finds the language's value.
        'Default|fr:Defaut|es:Defecto'                | Locale.CANADA_FRENCH   || 'Defaut'
        'Default|fr:Defaut|es:Defecto'                | Locale.GERMAN          || 'Default'
        // Malformed entries (no code, or more than one separator) are ignored.
        'Default|Defaut|es:Defecto'                   | Locale.FRENCH          || 'Default'
        'Default|fr:Defaut:extra'                     | Locale.FRENCH          || 'Default'
    }

    void 'getDefaultString returns the first value of a localized string'() {
        expect:
        assert LocalizationUtil.getDefaultString('Default|fr:Defaut|es:Defecto') == 'Default'
        assert LocalizationUtil.getDefaultString('') == ''
    }

    @Unroll
    void 'getLocalizedOrderImportDateFormat is #expected for #locale'() {
        expect:
        assert LocalizationUtil.getLocalizedOrderImportDateFormat(locale) == expected

        where:
        locale                || expected
        new Locale('es', 'MX') || 'dd/MM/yyyy'
        new Locale('es')       || 'MM/dd/yyyy'
        Locale.US              || 'MM/dd/yyyy'
        null                   || 'MM/dd/yyyy'
    }

    void 'the current locale and the single-argument getLocalizedString come from the localization service'() {
        given:
        LocalizationService serviceStub = Stub(LocalizationService) {
            getCurrentLocale() >> Locale.FRENCH
        }
        defineBeans {
            localizationService(InstanceFactoryBean, serviceStub, LocalizationService)
        }

        expect:
        assert LocalizationUtil.localizationService.is(serviceStub)
        assert LocalizationUtil.currentLocale == Locale.FRENCH
        assert LocalizationUtil.getLocalizedString('Default|fr:Defaut') == 'Defaut'
    }

    void 'a transaction is labelled with its formatted type followed by its number, when it has one'() {
        given:
        TransactionType transactionType = new TransactionType(name: 'Transfer Out')
        LocalizationService serviceStub = Stub(LocalizationService) {
            formatMetadata(transactionType) >> 'Transfer Out'
        }
        defineBeans {
            localizationService(InstanceFactoryBean, serviceStub, LocalizationService)
        }

        expect:
        assert LocalizationUtil.getLocalizedString(
                new Transaction(transactionType: transactionType, transactionNumber: 'T-0001')) == 'Transfer Out T-0001'
        assert LocalizationUtil.getLocalizedString(new Transaction(transactionType: transactionType)) == 'Transfer Out'
    }
}
