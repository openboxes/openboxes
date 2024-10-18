package org.pih.warehouse.smoke.spec

import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationIdentifierService
import org.pih.warehouse.core.OrganizationIdentifierService
import org.pih.warehouse.core.identification.IdentifierGeneratorParams
import org.pih.warehouse.data.ProductSupplierIdentifierService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceIdentifierService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderIdentifierService
import org.pih.warehouse.product.ProductIdentifierService
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.product.ProductTypeCode
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionIdentifierService
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentIdentifierService
import org.pih.warehouse.smoke.spec.base.SmokeSpec

/**
 * The purpose of this test spec is to verify the identifier runtime properties that we have for each of
 * the identifier services such that we can generate ids with each service without error.
 *
 * Because we're testing the configuration itself, we only want the tests to fail if the config is changed to
 * something invalid. As such, we provide as many generator params as possible in the tests to be flexible to
 * any possible valid config.
 */
class IdentifierSpec extends SmokeSpec {

    ShipmentIdentifierService shipmentIdentifierService
    InvoiceIdentifierService invoiceIdentifierService
    LocationIdentifierService locationIdentifierService
    ReceiptIdentifierService receiptIdentifierService
    ProductSupplierIdentifierService productSupplierIdentifierService
    OrderIdentifierService orderIdentifierService
    RequisitionIdentifierService requisitionIdentifierService
    TransactionIdentifierService transactionIdentifierService
    ProductIdentifierService productIdentifierService
    OrganizationIdentifierService organizationIdentifierService

    void 'shipmentIdentifierService can generate identifiers with the current configuration'() {
        given:
        Shipment entity = new Shipment(
                id: '1',
                name: 'name',
                description: 'description',
        )

        when:
        String identifier = shipmentIdentifierService.generate(IdentifierGeneratorParams.builder()
                .templateEntity(entity)
                .build())

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'invoiceIdentifierService can generate identifiers with the current configuration'() {
        given:
        Invoice entity = new Invoice(
                id: '1',
                name: 'name',
                description: 'description',
        )

        when:
        String identifier = invoiceIdentifierService.generate(IdentifierGeneratorParams.builder()
                .templateEntity(entity)
                .build())

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'locationIdentifierService can generate identifiers with the current configuration'() {
        given:
        Location entity = new Location(
                id: '1',
                name: 'name',
                description: 'description',
        )

        when:
        String identifier = locationIdentifierService.generate(IdentifierGeneratorParams.builder()
                .templateEntity(entity)
                .build())

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'receiptIdentifierService can generate identifiers with the current configuration'() {
        given:
        Receipt entity = new Receipt(
                id: '1',
        )

        when:
        String identifier = receiptIdentifierService.generate(IdentifierGeneratorParams.builder()
                .templateEntity(entity)
                .build())

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'productSupplierIdentifierService can generate identifiers with the current configuration'() {
        given:
        ProductSupplier entity = new ProductSupplier(
                id: '1',
                name: 'name',
                description: 'description',
                productCode: 'productCode',
                manufacturerCode: 'manufacturerCode',
                manufacturerName: 'manufacturerName',
                brandName: 'brandName',
                modelNumber: 'modelNumber',
                supplierCode: 'supplierCode',
                supplierName: 'supplierName',
        )

        and:
        Map customKeys = [
                'productCode': 'productCode',
                'organizationCode': 'organizationCode',
        ]

        when:
        String identifier = productSupplierIdentifierService.generate(IdentifierGeneratorParams.builder()
                .templateEntity(entity)
                .customKeys(customKeys)
                .build())

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'orderIdentifierService can generate identifiers with the current configuration'() {
        given:
        Order entity = new Order(
                id: '1',
                name: 'name',
                description: 'description',
        )

        when:
        String identifier = orderIdentifierService.generate(IdentifierGeneratorParams.builder()
                .templateEntity(entity)
                .build())

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'requisitionIdentifierService can generate identifiers with the current configuration'() {
        given:
        Requisition entity = new Requisition(
                id: '1',
                name: 'name',
                description: 'description',
        )

        when:
        String identifier = requisitionIdentifierService.generate(IdentifierGeneratorParams.builder()
                .templateEntity(entity)
                .build())

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'transactionIdentifierService can generate identifiers with the current configuration'() {
        given:
        Transaction entity = new Transaction(
                id: '1',
        )

        when:
        String identifier = transactionIdentifierService.generate(IdentifierGeneratorParams.builder()
                .templateEntity(entity)
                .build())

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'productIdentifierService can generate identifiers with the current configuration'() {
        given:
        ProductType productType = new ProductType(
                id: '1',
                name: 'name',
                productTypeCode: ProductTypeCode.GOOD,
                code: 'code',
                productIdentifierFormat: 'MNNNNN',
                sequenceNumber: 0,
        )

        when:
        String identifier = productIdentifierService.generateForProductType(productType)

        then:
        assert StringUtils.isNotBlank(identifier)
    }

    void 'organizationIdentifierService can generate identifiers with the current configuration'() {
        when:
        String identifier = organizationIdentifierService.generate("Good Organization Name")

        then:
        assert StringUtils.isNotBlank(identifier)
    }
}
