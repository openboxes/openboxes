package org.pih.warehouse.api.regression.platform.spec.core


import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Unroll

import org.pih.warehouse.api.regression.platform.client.core.SelectOptionsApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.GlAccountType
import org.pih.warehouse.core.GlAccountTypeCode
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductGroup

/**
 * The option lists behind the product list page's filters. Each test run adds one catalog, tag, GL account and product
 * family with unique names (tests do not rely on a clean database), so every list has an entry to check.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class SelectOptionsApiSpec extends RegressionApiSpec {

    @Autowired
    SelectOptionsApi regressionSelectOptionsApi

    @Shared
    String suffix

    @Shared
    ProductCatalog catalog

    @Shared
    Tag tag

    @Shared
    GlAccount glAccount

    @Shared
    ProductGroup productGroup

    @Override
    void setupData() {
        if (catalog) {
            return
        }
        suffix = UUID.randomUUID().toString().take(8)
        catalog = new ProductCatalog(code: "CAT-${suffix}", name: "Catalog ${suffix}").save(failOnError: true)
        tag = new Tag(tag: "Tag ${suffix}").save(failOnError: true)
        GlAccountType type = new GlAccountType(code: "TYPE-${suffix}", name: "Type ${suffix}",
                glAccountTypeCode: GlAccountTypeCode.EXPENSE).save(failOnError: true)
        glAccount = new GlAccount(code: "GL-${suffix}", name: "Account ${suffix}", glAccountType: type, active: true)
                .save(failOnError: true)
        productGroup = new ProductGroup(name: "Family ${suffix}").save(failOnError: true)
    }

    @Unroll
    void '#path lists #expectedLabel'() {
        expect:
        regressionSelectOptionsApi.options(path, params, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("data.find { it.id == '${idOf()}' }.label".toString(), Matchers.equalTo(expectedLabel()))
                .build())

        where:
        path                   | params              | idOf                | expectedLabel
        '/catalogOptions'      | [:]                 | { catalog.id }      | { "Catalog ${suffix} (0)".toString() }
        '/catalogOptions'      | [hideNumbers: true] | { catalog.id }      | { "Catalog ${suffix}".toString() }
        '/tagOptions'          | [:]                 | { tag.id }          | { "Tag ${suffix} (0)".toString() }
        '/tagOptions'          | [hideNumbers: true] | { tag.id }          | { "Tag ${suffix}".toString() }
        '/glAccountOptions'    | [active: true]      | { glAccount.id }    | { "GL-${suffix} - Account ${suffix}".toString() }
        '/productGroupOptions' | [:]                 | { productGroup.id } | { "Family ${suffix}".toString() }
    }

    void 'GL account options leave out inactive accounts when asked for active ones'() {
        expect:
        regressionSelectOptionsApi.options('/glAccountOptions', [active: false], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(glAccount.id)))
                .build())
    }

    void 'category options label each category with its path from the root'() {
        expect:
        regressionSelectOptionsApi.options('/categoryOptions', [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("data.find { it.id == '${rootCategory.id}' }.label".toString(),
                        Matchers.equalTo(rootCategory.name))
                .build())
    }

    void 'handling requirement options are the four product flags'() {
        expect:
        regressionSelectOptionsApi.options('/handlingRequirementsOptions', [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.containsInAnyOrder(
                        'COLD_CHAIN', 'CONTROLLED_SUBSTANCE', 'HAZARDOUS_MATERIAL', 'RECONDITIONED'))
                .expectBody('data.value', Matchers.containsInAnyOrder(
                        'COLD_CHAIN', 'CONTROLLED_SUBSTANCE', 'HAZARDOUS_MATERIAL', 'RECONDITIONED'))
                .build())
    }
}
