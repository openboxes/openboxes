package org.pih.warehouse.regression.platform.importer

import spock.lang.PendingFeature

import org.pih.warehouse.importer.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierImportCommand

/**
 * Fixtures are saved without validation so that only the import rules under test decide what is valid. The minimum
 * expiration date is pinned through the config so the date checks do not depend on the application defaults.
 */
@Unroll
class ProductSupplierImportDataServiceSpec extends Specification implements ServiceUnitTest<ProductSupplierImportDataService>, DataTest {

    static final Date MIN_DATE = new GregorianCalendar(2000, Calendar.JANUARY, 1).time

    Object originalMinDate

    void setupSpec() {
        mockDomains(ProductSupplier, Product, Organization, PartyRole, PreferenceType, UnitOfMeasure)
    }

    void setup() {
        originalMinDate = config.openboxes.expirationDate.minValue
        config.openboxes.expirationDate.minValue = MIN_DATE

        new Product(productCode: 'P1', name: 'Aspirin').save(validate: false)
        new Organization(code: 'ACME', name: 'Acme', active: true).save(validate: false)
        new Organization(code: 'OLD', name: 'Old Co', active: false).save(validate: false)
        Organization manufacturer = new Organization(code: 'MFG', name: 'Maker', active: true)
        manufacturer.addToRoles(new PartyRole(roleType: RoleType.ROLE_MANUFACTURER))
        manufacturer.save(validate: false)
        new UnitOfMeasure(code: 'BX', name: 'Box').save(validate: false)
        new PreferenceType(name: 'Preferred').save(validate: false)
    }

    void cleanup() {
        config.openboxes.expirationDate.minValue = originalMinDate
    }

    void 'validateData should accept a complete row'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow()])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'validateData should report every missing required field of a row'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [[:]])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == [
                'Row 1: Product Source Name is required',
                'Row 1: Product Code is required',
                'Row 1: Supplier Name is required',
                'Row 1: Default Package Type is required',
                'Row 1: Default Package Size is required',
        ]
    }

    void 'validateData should reject #description'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + overrides])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == expectedErrors

        where:
        description                       | overrides                                       || expectedErrors
        'a product code that is unknown'  | [productCode: 'NOPE']                           || ['Row 1: Product with productCode NOPE does not exist']
        'an active flag that is a string' | [active: 'yes']                                 || ['Row 1: Active field has to be either empty or a boolean value (true/false)']
        'a supplier that is unknown'      | [supplierName: 'Nobody']                        || ["Row 1: Supplier with name 'Nobody' does not exist"]
        'a manufacturer that is unknown'  | [manufacturerName: 'Nobody']                    || ["Row 1: Manufacturer with name 'Nobody' does not exist"]
        'a manufacturer without the role' | [manufacturerName: 'Acme']                      || ["Row 1: Organization 'Acme' is not a manufacturer"]
        'a rating type that is unknown'   | [ratingType: 'EXCELLENT']                       || ["Row 1: Rating Type with value 'EXCELLENT' does not exist"]
        'a preference type that is unknown' | [globalPreferenceTypeName: 'Other']           || ["Row 1: Preference Type with name 'Other' does not exist"]
        'a unit of measure that is unknown' | [defaultProductPackageUomCode: 'XX']          || ['Row 1: Unit of measure XX does not exist']
    }

    void 'validateData should accept a manufacturer that has the manufacturer role and a known rating type'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [manufacturerName: 'Maker', ratingType: 'good']])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    @PendingFeature(reason = 'OBAF applies % to a BigDecimal and throws instead of reporting the error')
    void 'validateData should report a package quantity that is not a whole number'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [defaultProductPackageQuantity: new BigDecimal('1.5')]])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ['Row 1: Unit of measure quantity must be a whole number']
    }

    void 'validateData should require both the package size and a quantity for the unit of measure'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [defaultProductPackageQuantity: null]])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == [
                'Row 1: Default Package Size is required',
                'Row 1: Unit of measure BX requires a quantity',
        ]
    }

    void 'validateData should reject an inactive supplier for a new source'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [supplierName: 'Old Co']])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ["Row 1: Supplier 'Old Co' is no longer active. Choose an active supplier"]
    }

    void 'validateData should #outcome an inactive supplier for an existing source when active is #active'() {
        given:
        ProductSupplier existing = new ProductSupplier(code: 'PS-1', name: 'Existing')
        existing.id = 'ps-1'
        existing.save(validate: false)

        and:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [id: 'ps-1', supplierName: 'Old Co', active: active]])

        when:
        service.validateData(command)

        then:
        command.errors.hasErrors() == rejected

        where: 'only a source that is (or stays) active cannot use an inactive supplier'
        active || rejected | outcome
        true   || true     | 'reject'
        false  || false    | 'accept'
        null   || false    | 'accept'
    }

    void 'validateData should reject dates before the minimum expiration date'() {
        given:
        Date tooEarly = new GregorianCalendar(1999, Calendar.DECEMBER, 31).time
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [
                globalPreferenceTypeValidityStartDate: tooEarly,
                globalPreferenceTypeValidityEndDate  : tooEarly,
                contractPriceValidUntil              : tooEarly,
        ]])

        when:
        service.validateData(command)

        then:
        List<String> codes = command.errors.allErrors*.code
        codes.size() == 3
        codes[0].startsWith('Row 1: Validity start date ')
        codes[1].startsWith('Row 1: Validity end date ')
        codes[2].startsWith('Row 1: Contract Price Valid Until date ')
        codes.every { it.endsWith('Please enter a date after 2000.') }
    }

    void 'validateData should accept dates on or after the minimum expiration date'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [
                globalPreferenceTypeValidityStartDate: MIN_DATE,
                globalPreferenceTypeValidityEndDate  : new GregorianCalendar(2030, Calendar.JUNE, 1).time,
                contractPriceValidUntil              : new GregorianCalendar(2030, Calendar.JUNE, 1).time,
        ]])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'validateData should number the errors by row'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow(), validRow() + [productCode: 'NOPE']])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ['Row 2: Product with productCode NOPE does not exist']
    }

    void 'importData should pass a sanitized command to the product supplier service and save the result'() {
        given:
        ProductSupplierService productSupplierService = Mock()
        service.productSupplierService = productSupplierService
        ProductSupplier productSupplier = Mock()
        Date validUntil = new GregorianCalendar(2030, Calendar.JUNE, 1).time

        and:
        ImportDataCommand command = new ImportDataCommand(data: [[
                id                          : '  ',
                code                        : ' PS-1 ',
                name                        : ' Aspirin from Acme ',
                productCode                 : 'P1',
                supplierName                : 'Acme',
                manufacturerName            : '',
                minOrderQuantity            : 5,
                contractPricePrice          : 2.5,
                contractPriceValidUntil     : validUntil,
                ratingType                  : 'fair',
                globalPreferenceTypeComments: '\t',
                defaultProductPackageUomCode: 'BX',
                defaultProductPackageQuantity: 10,
                defaultProductPackagePrice  : 12,
        ]])

        when:
        service.importData(command)

        then:
        1 * productSupplierService.createOrUpdate({ ProductSupplierImportCommand it ->
            it.active == true &&
                    it.id == null &&
                    it.code == 'PS-1' &&
                    it.name == 'Aspirin from Acme' &&
                    it.productCode == 'P1' &&
                    it.supplierName == 'Acme' &&
                    it.manufacturerName == null &&
                    it.minOrderQuantity == 5 &&
                    it.contractPricePrice == 2.5G &&
                    it.contractPriceValidUntil == validUntil &&
                    it.ratingType == RatingTypeCode.FAIR &&
                    it.globalPreferenceTypeComments == null &&
                    it.defaultProductPackageUomCode == 'BX' &&
                    it.defaultProductPackageQuantity == 10 &&
                    it.defaultProductPackagePrice == 12G &&
                    it.globalPreferenceTypeValidityStartDate == null
        }) >> productSupplier
        1 * productSupplier.save([failOnError: true])
    }

    void 'importData should parse the active flag #active as #expected'() {
        given:
        ProductSupplierService productSupplierService = Mock()
        service.productSupplierService = productSupplierService

        when:
        service.importData(new ImportDataCommand(data: [[active: active]]))

        then:
        1 * productSupplierService.createOrUpdate({ it.active == expected && it.contractPricePrice == null }) >> Mock(ProductSupplier)

        where:
        active  || expected
        null    || true
        ''      || true
        'false' || false
        false   || false
        'TRUE'  || true
    }

    void 'importData should fail on an active flag that is not a boolean'() {
        given:
        service.productSupplierService = Mock(ProductSupplierService)

        when:
        service.importData(new ImportDataCommand(data: [[active: 'maybe']]))

        then:
        thrown(IllegalArgumentException)
    }

    private static Map validRow() {
        return [
                name                         : 'Aspirin from Acme',
                productCode                  : 'P1',
                supplierName                 : 'Acme',
                defaultProductPackageUomCode : 'BX',
                defaultProductPackageQuantity: 10,
        ]
    }
}
