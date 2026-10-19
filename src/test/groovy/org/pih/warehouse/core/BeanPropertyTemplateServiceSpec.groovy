package org.pih.warehouse.core

import grails.testing.services.ServiceUnitTest
import org.springframework.beans.InvalidPropertyException
import org.springframework.beans.NotReadablePropertyException
import org.springframework.beans.NullValueInNestedPathException
import spock.lang.Specification
import spock.lang.Unroll

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

    static class MockCategory {
        String name = "Dressings"
    }

    // a product with optional values of several types, for the default value features
    static class MockLabelProduct {
        String name
        MockCategory category
        Integer quantity
        BigDecimal price
        Boolean coldChain
        List<String> tags
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

    // ------------------------------------------------------------------ default values: ${name.property:-default}

    @Unroll
    void "the default applies when the value is empty (#reason)"() {
        given:
        Map bindings = [product: new MockLabelProduct(name: value)]

        expect:
        service.renderTemplateContents(template, "test", bindings) == expected

        where:
        reason                       | template                    | value || expected
        "no value"                   | '^FD${product.name:-none}^FS' | null  || '^FDnone^FS'
        "empty text"                 | '^FD${product.name:-none}^FS' | ''    || '^FDnone^FS'
        "no value, empty default"    | '^FD${product.name:-}^FS'     | null  || '^FD^FS'
        "empty text, empty default"  | '^FD${product.name:-}^FS'     | ''    || '^FD^FS'
    }

    @Unroll
    void "a value is printed as it is, never the default (#reason)"() {
        given:
        Map bindings = [product: product]

        expect:
        service.renderTemplateContents(template, "test", bindings) == expected

        where:
        reason          | template                         | product                                       || expected
        "text"          | '${product.name:-none}'          | new MockLabelProduct(name: 'Gauze')           || 'Gauze'
        "blank text"    | '${product.name:-none}'          | new MockLabelProduct(name: '  ')              || '  '
        "integer zero"  | '${product.quantity:-none}'      | new MockLabelProduct(quantity: 0)             || '0'
        "decimal zero"  | '${product.price:-none}'         | new MockLabelProduct(price: new BigDecimal('0.0')) || '0.0'
        "false"         | '${product.coldChain:-none}'     | new MockLabelProduct(coldChain: false)        || 'false'
        "a link value"  | '${product.category.name:-none}' | new MockLabelProduct(category: new MockCategory()) || 'Dressings'
    }

    void "without a default an empty value still prints nothing"() {
        given:
        Map bindings = [product: new MockLabelProduct(name: value)]

        expect:
        service.renderTemplateContents('^FD${product.name}^FS', "test", bindings) == '^FD^FS'

        where:
        value << [null, '']
    }

    @Unroll
    void "the default applies when a link in the path is empty (#template)"() {
        given:
        Map bindings = [product: new MockLabelProduct(category: null, tags: null)]

        expect:
        service.renderTemplateContents(template, "test", bindings) == expected

        where:
        template                          || expected
        '${product.category.name:-none}'  || 'none'
        '${product.category.name:-}'      || ''
        '${product.tags[0]:-no tag}'      || 'no tag'
    }

    void "the default applies when the label object was passed without a value"() {
        given: "a product page passes no bin"
        Map bindings = [product: new MockLabelProduct(name: 'Gauze'), location: null]

        expect:
        service.renderTemplateContents('${product.name}|${location.name:-no bin}|${location:-}', "test", bindings) == 'Gauze|no bin|'
    }

    void "the properties of a label object passed without a value cannot be checked"() {
        given: "the object's type is unknown when its value is null"
        Map bindings = [location: null]

        expect:
        service.renderTemplateContents('${location.bogus:-no bin}', "test", bindings) == 'no bin'
    }

    void "without a default an empty label object is reported before a blocked property, as before"() {
        when:
        service.renderTemplateContents('${location.class.name}', "test", [location: null])

        then:
        IllegalArgumentException e = thrown()
        e.message.contains("binding 'location' is null")
    }

    void "a label object the page does not provide stays an error with a default"() {
        given:
        Map bindings = [product: new MockLabelProduct(name: 'Gauze')]

        when:
        service.renderTemplateContents('${location.name:-no bin}', "test", bindings)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('location')
        e.message.contains('not provided')
    }

    @Unroll
    void "a default does not hide a template error (#reason)"() {
        when:
        service.renderTemplateContents(template, "test", bindings)

        then:
        Exception e = thrown()
        expectedType.isInstance(e)
        e.message.contains(expectedMessage)

        where:
        reason                                 | template                                  | bindings                                                  || expectedType                  | expectedMessage
        "a property that does not exist"       | '${inventoryItem.lotNumber.bogus:-x}'     | [inventoryItem: new MockInventoryItem()]                  || NotReadablePropertyException  | 'bogus'
        "an index past the end of a list"      | '${inventoryItem.product.tags[5]:-x}'     | [inventoryItem: new MockInventoryItem()]                  || InvalidPropertyException      | 'tags[5]'
        "a blocked property"                   | '${location.class.name:-x}'               | [location: new MockLocation()]                            || IllegalArgumentException      | 'not allowed'
        "a blocked property on an empty object" | '${location.class.name:-x}'              | [location: null]                                          || IllegalArgumentException      | 'not allowed'
        "a blocked property after an empty link" | '${product.category.metaClass:-x}'      | [product: new MockLabelProduct()]                         || IllegalArgumentException      | 'not allowed'
        "a blocked type"                       | '${location.unexpectedClassProperty:-x}'  | [location: new MockWithClassTypedProperty()]              || IllegalArgumentException      | 'blocked type'
        "a property that does not exist after an empty link" | '${product.category.bogus:-x}' | [product: new MockLabelProduct()]                 || NotReadablePropertyException  | 'bogus'
        "a link that does not exist before an empty link" | '${product.bogus.name:-x}'     | [product: new MockLabelProduct()]                    || NotReadablePropertyException  | 'bogus'
        "a non-numeric index after an empty link" | '${product.tags[x]:-d}'               | [product: new MockLabelProduct()]                        || InvalidPropertyException      | 'tags'
        "a non-numeric index on an empty object" | '${location.tags[x]:-d}'               | [location: null]                                         || IllegalArgumentException      | 'is null'
        "an expression"                        | '${7*7:-x}'                               | [location: new MockLocation()]                            || IllegalArgumentException      | '7*7'
        "safe navigation"                      | '${location?.name:-x}'                    | [location: null]                                          || IllegalArgumentException      | 'location?'
    }

    void "the property path is trimmed and the default is taken as written"() {
        given:
        Map bindings = [product: new MockLabelProduct(name: null)]

        expect:
        service.renderTemplateContents('[${ product.name :-x}][${product.name:- x }]', "test", bindings) == '[x][ x ]'
    }

    void "a default containing :- keeps everything after the first :-"() {
        given:
        Map bindings = [product: new MockLabelProduct(name: null)]

        expect:
        service.renderTemplateContents('${product.name:-x:-y}', "test", bindings) == 'x:-y'
    }

    void "a placeholder started inside a default is printed as written, never resolved"() {
        given: "the placeholder ends at the first }, so the default is 'see \${location.name' and the last } is template text"
        Map bindings = [product: new MockLabelProduct(name: null), location: new MockLocation()]

        expect:
        service.renderTemplateContents('${product.name:-see ${location.name}}', "test", bindings) == 'see ${location.name}'
    }

    void "a default with dollar signs is printed as written"() {
        given:
        Map bindings = [product: new MockLabelProduct(name: null)]

        expect:
        service.renderTemplateContents('${product.name:-$5 $$ ${x}', "test", bindings) == '$5 $$ ${x'
    }

    void "the escape keeps a placeholder with a default literal"() {
        given:
        Map bindings = [product: new MockLabelProduct(name: null)]

        expect:
        service.renderTemplateContents('$${product.name:-x}', "test", bindings) == '${product.name:-x}'
    }

    @Unroll
    void "the bundled #template template carries no default and renders exactly as before"() {
        given:
        String contents = BeanPropertyTemplateServiceSpec.getResource("/templates/zpl-boarcodes-templates/${template}.zpl").text

        expect:
        !contents.contains(':-')
        service.renderTemplateContents(contents, template, [(template): model]) == contents.replace(placeholder, 'V-1')

        where:
        template           | placeholder                  | model
        "product"          | '${product.productCode}'     | new MockProduct(productCode: 'V-1')
        "container"        | '${container.name}'          | new MockLocation(name: 'V-1')
        "internalLocation" | '${internalLocation.name}'   | new MockLocation(name: 'V-1')
    }
}
