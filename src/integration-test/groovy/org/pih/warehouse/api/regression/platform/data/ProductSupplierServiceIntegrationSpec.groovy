package org.pih.warehouse.api.regression.platform.data

import org.pih.warehouse.data.*
import spock.lang.Unroll

import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierFilterCommand
import org.pih.warehouse.product.ProductSupplierImportCommand

/**
 * Product sources (product suppliers): created and updated from import rows with their package, contract price and
 * global preference, looked up by supplier and manufacturer codes, and listed with filters and sort orders.
 */
class ProductSupplierServiceIntegrationSpec extends ServiceIntegrationSpec {

    ProductSupplierService productSupplierService

    Location depot
    Product product
    Organization supplier
    Organization manufacturer
    UnitOfMeasure box
    PreferenceType preferred

    void setup() {
        depot = Location.withTransaction { Location.get('1') }
        product = newProduct()
        committed {
            def partyType = Location.get('1').organization.partyType
            supplier = new Organization(name: uniqueName('Supplier org'), code: uniqueName('SUP'), active: true, partyType: partyType)
                    .save(failOnError: true, flush: true)
            manufacturer = new Organization(name: uniqueName('Maker org'), code: uniqueName('MAK'), active: true, partyType: partyType)
                    .save(failOnError: true, flush: true)
            box = new UnitOfMeasure(name: uniqueName('Box'), code: uniqueName('BX')).save(failOnError: true, flush: true)
            preferred = new PreferenceType(name: uniqueName('Preferred')).save(failOnError: true, flush: true)
        }
    }

    private ProductSupplierImportCommand importRow(Map overrides = [:]) {
        Map values = [
                active                               : true,
                name                                 : 'Imported source',
                productCode                          : product.productCode,
                legacyProductCode                    : 'LEGACY-1',
                supplierName                         : supplier.name,
                supplierCode                         : 'SUP-CODE-1',
                manufacturerName                     : manufacturer.name,
                manufacturerCode                     : 'MAK-CODE-1',
                ratingType                           : RatingTypeCode.GOOD,
                minOrderQuantity                     : 5,
                contractPricePrice                   : 2.5,
                contractPriceValidUntil              : Date.parse('yyyy-MM-dd', '2030-12-31'),
                globalPreferenceTypeName             : preferred.name,
                globalPreferenceTypeComments         : 'First choice',
                globalPreferenceTypeValidityStartDate: Date.parse('yyyy-MM-dd', '2026-01-01'),
                defaultProductPackageUomCode         : box.code,
                defaultProductPackageQuantity        : 10,
                defaultProductPackagePrice           : 20.0,
        ] + overrides
        return new ProductSupplierImportCommand(values)
    }

    private Map describe(String id) {
        return asAdminAt(depot) {
            ProductSupplier source = ProductSupplier.get(id)
            [
                    code              : source.code,
                    product           : source.product?.id,
                    supplier          : source.supplier?.id,
                    manufacturer      : source.manufacturer?.id,
                    manufacturerCode  : source.manufacturerCode,
                    ratingTypeCode    : source.ratingTypeCode,
                    packageQuantity   : source.defaultProductPackage?.quantity,
                    packageUom        : source.defaultProductPackage?.uom?.id,
                    packagePrice      : source.defaultProductPackage?.productPrice?.price,
                    packages          : source.productPackages?.size() ?: 0,
                    contractPrice     : source.contractPrice?.price,
                    contractValidUntil: source.contractPrice?.toDate,
                    preference        : source.globalProductSupplierPreference?.preferenceType?.id,
                    preferenceComments: source.globalProductSupplierPreference?.comments,
            ]
        }
    }

    private String importAndSave(ProductSupplierImportCommand command) {
        return asAdminAt(depot) {
            ProductSupplier source = productSupplierService.createOrUpdate(command)
            source.save(failOnError: true, flush: true)
            source.id
        }
    }

    void 'an import row creates a source with a generated code, its package, contract price and preference'() {
        when:
        String id = importAndSave(importRow())
        Map source = describe(id)

        then:
        source.code
        source.product == product.id
        source.supplier == supplier.id
        source.manufacturer == manufacturer.id
        source.manufacturerCode == 'MAK-CODE-1'
        source.ratingTypeCode == RatingTypeCode.GOOD
        source.packageQuantity == 10
        source.packageUom == box.id
        source.packagePrice == 20.0
        source.packages == 1
        source.contractPrice == 2.5
        source.contractValidUntil == Date.parse('yyyy-MM-dd', '2030-12-31')
        source.preference == preferred.id
        source.preferenceComments == 'First choice'
    }

    void 'importing the row again updates the package price, then clears what the row leaves out'() {
        given:
        String id = importAndSave(importRow())
        String code = describe(id).code

        when: 'the same package gets a new price'
        importAndSave(importRow(id: id, defaultProductPackagePrice: 25.0, globalPreferenceTypeComments: 'Still first'))
        Map repriced = describe(id)

        then:
        repriced.code == code
        repriced.packages == 1
        repriced.packagePrice == 25.0
        repriced.preferenceComments == 'Still first'

        when: 'no package price, no contract price, no manufacturer and no preference'
        importAndSave(importRow(id: id, defaultProductPackagePrice: null, contractPricePrice: null, manufacturerName: null,
                globalPreferenceTypeName: null))
        Map cleared = describe(id)

        then:
        cleared.packages == 1
        cleared.packagePrice == null
        cleared.contractPrice == null
        cleared.manufacturer == null
        cleared.manufacturerCode == null
        cleared.ratingTypeCode == null
        cleared.preference == null

        when: 'a package price is set again on the existing package'
        importAndSave(importRow(id: id, defaultProductPackagePrice: 30.0))

        then:
        describe(id).packagePrice == 30.0
    }

    void 'a row without a known unit of measure or quantity adds no package'() {
        when:
        String id = importAndSave(importRow(code: uniqueName('SRC'), defaultProductPackageUomCode: 'NO-SUCH-UOM'))

        then:
        describe(id).packages == 0
        describe(id).packagePrice == null
    }

    void 'sources are found by supplier and manufacturer codes, or created'() {
        when:
        Map result = asAdminAt(depot) {
            Product saved = Product.get(product.id)
            Organization supplierOrg = Organization.get(supplier.id)
            ProductSupplier bySupplierCode = productSupplierService.getOrCreateNew(
                    [product: saved, supplier: supplierOrg, supplierCode: 'S-1 .2', sourceName: 'Created source'], false)
            ProductSupplier byManufacturer = productSupplierService.getOrCreateNew(
                    [product: saved, supplier: supplierOrg, manufacturer: manufacturer.id, manufacturerCode: 'M-9'], false)
            ProductSupplier byManufacturerOnly = productSupplierService.getOrCreateNew(
                    [product: saved, supplier: supplierOrg, manufacturer: manufacturer.id], false)
            ProductSupplier byManufacturerCodeOnly = productSupplierService.getOrCreateNew(
                    [product: saved, supplier: supplierOrg, manufacturerCode: 'M-7'], false)
            ProductSupplier forced = productSupplierService.getOrCreateNew([product: saved, supplier: supplierOrg], true)
            [
                    bySupplierCode        : bySupplierCode.id,
                    supplierCodeName      : bySupplierCode.name,
                    byManufacturer        : byManufacturer.id,
                    byManufacturerOnly    : byManufacturerOnly.id,
                    byManufacturerCodeOnly: byManufacturerCodeOnly.id,
                    forced                : forced.id,
                    nothing               : productSupplierService.getOrCreateNew([product: saved, supplier: supplierOrg,
                                                                                    productSupplier: 'no-such-source'], false),
            ]
        }
        Map again = asAdminAt(depot) {
            Product saved = Product.get(product.id)
            Organization supplierOrg = Organization.get(supplier.id)
            [
                    bySupplierCode        : productSupplierService.getOrCreateNew([product: saved, supplier: supplierOrg, supplierCode: 'S-1 .2'], false)?.id,
                    byManufacturer        : productSupplierService.getProductSupplier([product: saved, supplier: supplierOrg,
                                                                                       manufacturer: manufacturer.id, manufacturerCode: 'M-9'])?.id,
                    byManufacturerOnly    : productSupplierService.getProductSupplier([product: saved, supplier: supplierOrg, manufacturer: manufacturer.id])?.id,
                    byManufacturerCodeOnly: productSupplierService.getProductSupplier([product: saved, supplier: supplierOrg, manufacturerCode: 'M-7'])?.id,
                    byId                  : productSupplierService.getOrCreateNew([productSupplier: result.forced], false)?.id,
            ]
        }

        then:
        result.supplierCodeName == 'Created source'
        [result.bySupplierCode, result.byManufacturer, result.byManufacturerOnly, result.byManufacturerCodeOnly, result.forced].unique().size() == 5
        result.nothing == null
        again.bySupplierCode == result.bySupplierCode
        again.byManufacturer == result.byManufacturer
        again.byManufacturerOnly == result.byManufacturerOnly
        again.byManufacturerCodeOnly == result.byManufacturerCodeOnly
        again.byId == result.forced
    }

    @Unroll
    void 'the source list filtered by #filter #includes the imported source'() {
        given:
        String id = importAndSave(importRow(code: uniqueName('SRC')))

        when:
        List<String> ids = asAdminAt(depot) {
            Map values = filters.call(Product.get(product.id), Organization.get(supplier.id), PreferenceType.get(preferred.id))
            productSupplierService.getProductSuppliers(new ProductSupplierFilterCommand(values))*.id
        }

        then:
        ids.contains(id) == included

        where:
        filter                          | included | filters
        'product'                       | true     | { p, s, t -> [product: p.id] }
        'product code search'           | true     | { p, s, t -> [searchTerm: p.productCode, product: p.id, supplier: s.id] }
        'supplier'                      | true     | { p, s, t -> [supplier: s.id, product: p.id] }
        'default preference'            | true     | { p, s, t -> [product: p.id, defaultPreferenceTypes: [t.id]] }
        'no default preference'         | false    | { p, s, t -> [product: p.id, defaultPreferenceTypes: [ProductSupplierService.PREFERENCE_TYPE_NONE]] }
        'creation date'                 | true     | { p, s, t -> [product: p.id, createdFrom: new Date() - 1, createdTo: new Date() + 1] }
        'a later creation date'         | false    | { p, s, t -> [product: p.id, createdFrom: new Date() + 1] }
        'product code, sorted'          | true     | { p, s, t -> [product: p.id, sort: 'product.productCode', order: 'desc'] }
        'product name, sorted'          | true     | { p, s, t -> [product: p.id, sort: 'product.name'] }
        'code, sorted'                  | true     | { p, s, t -> [product: p.id, sort: 'code'] }
        'supplier name, sorted'         | true     | { p, s, t -> [product: p.id, sort: 'supplier.displayName', order: 'desc'] }
        'date created, sorted'          | true     | { p, s, t -> [product: p.id, sort: 'dateCreated'] }
        'active, sorted'                | true     | { p, s, t -> [product: p.id, sort: 'active', includeInactive: true] }
        'name, sorted'                  | true     | { p, s, t -> [product: p.id, sort: 'name'] }
        'search and supplier, sorted'   | true     | { p, s, t -> [searchTerm: 'SUP-CODE-1', supplier: s.id, sort: 'product.name'] }

        includes = included ? 'includes' : 'leaves out'
    }

    void 'the export lists the source with its package, contract price and preference'() {
        given:
        String id = importAndSave(importRow(code: uniqueName('SRC')))

        when:
        List<Map> rows = asAdminAt(depot) {
            productSupplierService.getExportData(new ProductSupplierFilterCommand(product: product.id))
        }

        then:
        Map row = rows.find { it.ID == id }
        row['Product Code'] == product.productCode
        row.active == true
    }
}
