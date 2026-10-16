package org.pih.warehouse.slice.spec.product

import grails.gorm.transactions.Rollback
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.product.AttributeTestBuilder
import org.pih.warehouse.common.util.RandomUtil
import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.product.AttributeService

@Rollback
class AttributeServiceSpec extends IntegrationSpec {

    @Autowired
    AttributeService attributeService

    RandomUtil randomUtil = new RandomUtil()

    private Attribute save(AttributeTestBuilder builder) {
        return builder.build().save(flush: true, failOnError: true)
    }

    void 'searchAttributes should return attributes whose name contains the search term ignoring case'() {
        given:
        String prefix = randomUtil.randomStringFieldValue("Search")
        Attribute matching = save(new AttributeTestBuilder().name("${prefix} Test"))
        Attribute notMatching = save(new AttributeTestBuilder())

        when:
        List<Attribute> attributes = attributeService.searchAttributes(prefix.toLowerCase(), [:])

        then:
        assert attributes*.id == [matching.id]
        assert !attributes*.id.contains(notMatching.id)
    }

    void 'searchAttributes should return all attributes when search term is not provided'() {
        given:
        Attribute first = save(new AttributeTestBuilder())
        Attribute second = save(new AttributeTestBuilder().active(false))

        when:
        List<Attribute> attributes = attributeService.searchAttributes(searchTerm, [:])

        then:
        assert attributes*.id.containsAll([first.id, second.id])

        where:
        searchTerm << [null, ""]
    }

    void 'searchAttributes should sort attributes by the given field and order'() {
        given:
        String prefix = randomUtil.randomStringFieldValue("Sort")
        save(new AttributeTestBuilder().name("${prefix} B"))
        save(new AttributeTestBuilder().name("${prefix} A"))
        save(new AttributeTestBuilder().name("${prefix} C"))

        when:
        List<Attribute> attributes = attributeService.searchAttributes(prefix, [sort: "name", order: order])

        then:
        assert attributes*.name == expectedNames.collect { "${prefix} ${it}" }

        where:
        order  || expectedNames
        "asc"  || ["A", "B", "C"]
        "desc" || ["C", "B", "A"]
    }

    void 'searchAttributes should paginate results when max and offset are provided'() {
        given:
        String prefix = randomUtil.randomStringFieldValue("Page")
        ["A", "B", "C"].each { save(new AttributeTestBuilder().name("${prefix} ${it}")) }

        when:
        List<Attribute> attributes = attributeService.searchAttributes(prefix, [max: 2, offset: 1, sort: "name", order: "asc"])

        then:
        assert attributes*.name == ["${prefix} B", "${prefix} C"]
        assert attributes.totalCount == 3
    }

    void 'list should return only active attributes by default'() {
        given:
        Attribute active = save(new AttributeTestBuilder())
        Attribute inactive = save(new AttributeTestBuilder().active(false))

        when:
        List<Attribute> attributes = attributeService.list(null)

        then:
        assert attributes*.id.contains(active.id)
        assert !attributes*.id.contains(inactive.id)
    }

    void 'list should return only inactive attributes when active is false'() {
        given:
        Attribute active = save(new AttributeTestBuilder())
        Attribute inactive = save(new AttributeTestBuilder().active(false))

        when:
        List<Attribute> attributes = attributeService.list(null, false)

        then:
        assert attributes*.id.contains(inactive.id)
        assert !attributes*.id.contains(active.id)
    }

    void 'list should return attributes regardless of entity type when entity type code is not provided'() {
        given:
        Attribute productAttribute = save(new AttributeTestBuilder().entityTypeCodes(EntityTypeCode.PRODUCT))
        Attribute supplierAttribute = save(new AttributeTestBuilder().entityTypeCodes(EntityTypeCode.PRODUCT_SUPPLIER))
        Attribute attributeWithoutType = save(new AttributeTestBuilder())

        when:
        List<Attribute> attributes = attributeService.list(null)

        then:
        assert attributes*.id.containsAll([productAttribute.id, supplierAttribute.id, attributeWithoutType.id])
    }

    void 'list should return only attributes assigned to the given entity type code'() {
        given:
        Attribute productAttribute = save(new AttributeTestBuilder().entityTypeCodes(EntityTypeCode.PRODUCT))
        Attribute supplierAttribute = save(new AttributeTestBuilder().entityTypeCodes(EntityTypeCode.PRODUCT_SUPPLIER))
        Attribute attributeWithoutType = save(new AttributeTestBuilder())

        when:
        List<Attribute> attributes = attributeService.list(EntityTypeCode.PRODUCT)

        then:
        assert attributes*.id.contains(productAttribute.id)
        assert !attributes*.id.contains(supplierAttribute.id)
        assert !attributes*.id.contains(attributeWithoutType.id)
    }

    void 'list should filter by both entity type code and active status'() {
        given:
        Attribute activeProductAttribute = save(new AttributeTestBuilder()
                .entityTypeCodes(EntityTypeCode.PRODUCT))
        Attribute inactiveProductAttribute = save(new AttributeTestBuilder()
                .entityTypeCodes(EntityTypeCode.PRODUCT)
                .active(false))

        when:
        List<Attribute> attributes = attributeService.list(EntityTypeCode.PRODUCT, false)

        then:
        assert attributes*.id.contains(inactiveProductAttribute.id)
        assert !attributes*.id.contains(activeProductAttribute.id)
    }
}
