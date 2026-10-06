package org.pih.warehouse.core

import grails.testing.services.ServiceUnitTest
import org.springframework.beans.NotReadablePropertyException
import org.springframework.beans.NullValueInNestedPathException
import spock.lang.Specification

/**
 * Tests that BeanPropertyTemplateService resolves property paths
 * and rejects arbitrary expressions when rendering templates.
 */
class BeanPropertyTemplateServiceSpec extends Specification implements ServiceUnitTest<BeanPropertyTemplateService> {

    static class MockProduct {
        String productCode = "ABC123"
        String name = "Test Product"
        List<String> tags = ["cold-chain", "fragile"]
    }

    static class MockInventoryItem {
        MockProduct product = new MockProduct()
        String lotNumber = "LOT-001"
    }

    static class MockLocation {
        String name = "Main Warehouse"
    }

    // we should block properties of type Class, not just properties named 'class'
    static class MockWithClassTypedProperty {
        Class unexpectedClassProperty = String.class
    }

    private Map createMockBindings() {
        [
            inventoryItem: new MockInventoryItem(),
            location: new MockLocation(),
            document: new Document(name: "test-template"),
        ]
    }

    void "should resolve simple property path"() {
        given:
        Map bindings = createMockBindings()
        String template = '^XA^FO50,50^FD${location.name}^FS^XZ'

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == '^XA^FO50,50^FDMain Warehouse^FS^XZ'
    }

    void "should resolve nested property path"() {
        given:
        Map bindings = createMockBindings()
        String template = '${inventoryItem.product.productCode}'

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == 'ABC123'
    }

    void "should reject safe-navigation operator"() {
        given:
        Map bindings = createMockBindings()
        String template = '${inventoryItem?.product?.productCode}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(Exception)
    }

    void "should throw for null intermediate property"() {
        given:
        Map bindings = [inventoryItem: new MockInventoryItem(product: null)]
        String template = '${inventoryItem.product.productCode}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(NullValueInNestedPathException)
    }

    void "should throw when root binding is null"() {
        given:
        Map bindings = [inventoryItem: null]
        String template = '${inventoryItem.lotNumber}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('inventoryItem')
        e.message.contains('null')
    }

    void "should throw when binding was not provided"() {
        given:
        // location is in ALLOWED_BINDING_NAMES but the caller did not include it
        Map bindings = [inventoryItem: new MockInventoryItem(), document: new Document(name: "test-template")]
        String template = '${location.name}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('location')
        e.message.contains('not provided')
    }

    void "should resolve top-level binding without property path"() {
        given:
        Map bindings = [location: "Walla Walla, Washington"]
        String template = '${location}'

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == 'Walla Walla, Washington'
    }

    void "should not execute Groovy string execute() method"() {
        given:
        Map bindings = createMockBindings()
        String template = 'SNEAKY EXFILTRATION ATTEMPT TO READ ${"cat /etc/passwd".execute().text}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(IllegalArgumentException)
    }

    void "should not execute Runtime.exec()"() {
        given:
        Map bindings = createMockBindings()
        String template = 'SNEAKY EXFILTRATION ATTEMPT TO READ ${Runtime.getRuntime().exec("id")}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(IllegalArgumentException)
    }

    void "should not evaluate arithmetic expressions"() {
        given:
        Map bindings = createMockBindings()
        String template = '${7*7}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(IllegalArgumentException)
    }

    void "should reject GSP scriptlet tags"() {
        given:
        Map bindings = createMockBindings()
        String template = '<% println "hacked" %>normal text'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('GSP tags')
    }

    void "should reject GSP g:tags"() {
        given:
        Map bindings = createMockBindings()
        String template = '<g:each in="items">item</g:each>'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('GSP tags')
    }

    void "should throw for invalid property access"() {
        given:
        Map bindings = createMockBindings()
        // 'lotNumber' is a String, which doesn't have a 'bogus' property accessor
        String template = '${inventoryItem.lotNumber.bogus}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(NotReadablePropertyException)
    }

    void "test that public API actually works"() {
        given:
        Map bindings = createMockBindings()
        Document doc = new Document(
            name: "test-doc",
            fileContents: '^FD${location.name}^FS'.bytes,
        )

        when:
        String result = service.renderTemplate(doc, bindings)

        then:
        result == '^FDMain Warehouse^FS'
    }

    void "should throw for Document with null fileContents"() {
        given:
        Map bindings = createMockBindings()
        Document doc = new Document(name: "empty-doc", fileContents: null)

        when:
        service.renderTemplate(doc, bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('empty-doc')
    }

    void "should not evaluate nested expressions in resolved values"() {
        given:
        // if a property value itself contains ${...}, it must not be re-processed
        Map bindings = [location: new MockLocation(name: '${sneaky.payload}')]
        String template = '${location.name}'

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == '${sneaky.payload}'
    }

    void "dollar sign in property value is preserved literally"() {
        given:
        Map bindings = [location: new MockLocation(name: 'AB$202')]
        String template = '${location.name}'

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == 'AB$202'
    }

    void "exceptions from lookup propagate through StringSubstitutor"() {
        given:
        Map bindings = [:]
        String template = 'before ${nonexistent.key} after'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(IllegalArgumentException)
    }

    void "should render a realistic ZPL label template"() {
        given:
        Map bindings = createMockBindings()
        String template = '''^XA
^FO50,50^ADN,36,20^FD${inventoryItem.product.productCode}^FS
^FO50,100^ADN,36,20^FD${inventoryItem.lotNumber}^FS
^FO50,150^ADN,36,20^FD${location.name}^FS
^XZ'''

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result.contains('^FDABC123^FS')
        result.contains('^FDLOT-001^FS')
        result.contains('^FDMain Warehouse^FS')
        result.startsWith('^XA')
        result.trim().endsWith('^XZ')
    }

    void "dollar-dollar escapes to literal dollar-brace"() {
        given:
        Map bindings = createMockBindings()
        String template = 'price: $${location.name}'

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == 'price: ${location.name}'
    }

    void "backslash does not escape dollar-brace in template content"() {
        given:
        Map bindings = createMockBindings()
        // in a triple quoted string, a double backslash is a literal backslash
        String template = '''path: \\${location.name}'''

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == '''path: \\Main Warehouse'''
    }

    void "should block access to .class property"() {
        given:
        Map bindings = createMockBindings()
        String template = '${location.class}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('class')
        e.message.contains('not allowed')
    }

    void "should block access to attributes of blocked properties (e.g., class.name)"() {
        given:
        Map bindings = createMockBindings()
        String template = '${location.class.name}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('class')
        e.message.contains('not allowed')
    }

    void "should block access to metaClass property"() {
        given:
        Map bindings = createMockBindings()
        String template = '${location.metaClass}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('metaClass')
        e.message.contains('not allowed')
    }

    void "should block access to .properties property"() {
        given:
        Map bindings = createMockBindings()
        String template = '${location.properties}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('properties')
        e.message.contains('not allowed')
    }

    void "should block access to .domainClass property"() {
        given:
        Map bindings = createMockBindings()
        String template = '${location.domainClass}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('domainClass')
        e.message.contains('not allowed')
    }

    void "should block access to .constraints property"() {
        given:
        Map bindings = createMockBindings()
        String template = '${location.constraints}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('constraints')
        e.message.contains('not allowed')
    }

    void "should block bracket-indexed access to blocked properties"() {
        given:
        Map bindings = createMockBindings()
        String template = '${location.constraints[0]}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('constraints')
        e.message.contains('not allowed')
    }

    void "should allow bracket-indexed access to non-blocked properties"() {
        given:
        Map bindings = createMockBindings()
        String template = '${inventoryItem.product.tags[0]}'

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == 'cold-chain'
    }

    void "should block any property that resolves to a Class type regardless of name"() {
        given:
        // the property isn't named 'class' so the name blocklist won't catch it
        Map bindings = [location: new MockWithClassTypedProperty()]
        String template = '${location.unexpectedClassProperty}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('blocked type')
    }

    void "template cannot reference bindings the caller did not provide"() {
        given:
        // caller provides only document -- location is in ALLOWED_BINDING_NAMES but absent here
        Map bindings = [document: new Document(name: "test-template")]
        String template = '${location.name}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('location')
        e.message.contains('not provided')
    }

    void "engine rejects bindings outside ALLOWED_BINDING_NAMES"() {
        given:
        Map bindings = [grailsApplication: new MockProduct()]
        String template = '${grailsApplication.productCode}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('grailsApplication')
        e.message.contains('does not permit')
    }

    void "should reject array indexing syntax"() {
        given:
        Map bindings = createMockBindings()
        String template = '${inventoryItem[0]}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(Exception)
    }

    void "should reject dictionary lookup syntax"() {
        given:
        Map bindings = createMockBindings()
        String template = '${inventoryItem[\'lotNumber\']}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(Exception)
    }

    void "should reject method calls disguised as property access"() {
        given:
        Map bindings = createMockBindings()
        String template = '${inventoryItem.lotNumber.getClass()}'

        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        thrown(Exception)
    }

    void "should accept product and facility bindings"() {
        given:
        Map bindings = [
            product: new MockProduct(),
            facility: new MockLocation(),
        ]
        String template = '${product.productCode} at ${facility.name}'

        when:
        String result = service.renderTemplateContents(template, "test", bindings)

        then:
        result == 'ABC123 at Main Warehouse'
    }
}
