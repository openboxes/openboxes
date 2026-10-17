package org.pih.warehouse.api.regression.platform.pages.organization

import spock.lang.PendingFeature


import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericApi
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericResource
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response

import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.springframework.beans.factory.annotation.Autowired

import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * Deleting an organization that is still in use (a product source names it as supplier): the organization page and
 * the generic API refuse it and keep the organization and its counters, and neither answers with a server error.
 * Deleting an unused one removes it.
 */
class OrganizationDeletePagesSpec extends RegressionApiSpec {

    @Autowired
    RegressionGenericApi regressionGenericApi

    ServerPages pages

    @Override
    void setupData() {
        pages = new ServerPages(serverPort, cookie)
    }

    private String organization(Map<String, String> counters = [:]) {
        return Organization.withNewTransaction {
            Organization organization = new Organization(code: randomUtil.randomStringFieldValue('DEL').take(30),
                    name: randomUtil.randomStringFieldValue('Delete org'), active: true,
                    partyType: PartyType.findByCode('ORG'))
            organization.sequences = counters
            organization.save(failOnError: true, flush: true).id
        }
    }

    /** Puts the organization in use: a product source names it as its supplier. */
    private void inUse(String organizationId) {
        ProductSupplier.withNewTransaction {
            Product product = new ProductTestBuilder().name(randomUtil.randomStringFieldValue('Sourced')).category(rootCategory)
                    .build(true)
            new ProductSupplier(name: 'In use', code: randomUtil.randomStringFieldValue('SRC'), product: product,
                    supplier: Organization.get(organizationId)).save(failOnError: true, flush: true)
        }
    }

    private static boolean exists(String id) {
        return Organization.withNewSession { Organization.exists(id) }
    }

    private static Map counters(String id) {
        return Organization.withNewSession { new HashMap(Organization.get(id).sequences ?: [:]) }
    }

    void 'the organization page refuses to delete an organization in use: back to the edit page, nothing removed'() {
        given:
        String id = organization(['PURCHASE_ORDER_NUMBER': '7'])
        inUse(id)

        when:
        Response deleted = pages.post("/organization/delete", [id: id])

        then:
        deleted.statusCode == 302
        redirectPath(deleted) == "/organization/edit/${id}"
        exists(id)
        counters(id) == ['PURCHASE_ORDER_NUMBER': '7']
    }

    void 'the organization page deletes an unused organization with its counters'() {
        given:
        String id = organization(['PURCHASE_ORDER_NUMBER': '7'])

        when:
        Response deleted = pages.post("/organization/delete", [id: id])

        then:
        deleted.statusCode == 302
        redirectPath(deleted) == '/organization/list'
        !exists(id)
    }

    @PendingFeature(reason = 'deleting a referenced organization answers 500 on MariaDB')
    void 'the generic API refuses to delete an organization in use with a client error, nothing removed'() {
        given:
        String id = organization(['PURCHASE_ORDER_NUMBER': '7'])
        inUse(id)

        when:
        Response deleted = regressionGenericApi.delete(RegressionGenericResource.ORGANIZATION, id, new ResponseSpecBuilder().build())

        then:
        deleted.statusCode >= 400
        deleted.statusCode < 500
        exists(id)
        counters(id) == ['PURCHASE_ORDER_NUMBER': '7']
    }
}
