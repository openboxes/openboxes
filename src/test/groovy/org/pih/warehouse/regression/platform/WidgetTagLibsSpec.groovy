package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.testing.gorm.DataTest
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification

import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Localization
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.shipping.ShipmentType

/**
 * The small widget tag libraries: they mostly fill in defaults and hand off to a template.
 */
class WidgetTagLibsSpec extends Specification implements TagLibUnitTest<AutoSuggestTagLib>, DataTest {

    void setupSpec() {
        mockDomains(Location, LocationType, Product, Category, Inventory, InventoryLevel, ShipmentType, Localization)
        [AutoSuggestSearchableTagLib, AutoSuggestStringTagLib, ComboBoxTagLib, DecimalNumberFieldTagLib,
         GlobalSearchTagLib, InventoryTagLib, ProductTagLib, SearchProductTagLib, SortTagLib, WordWrapTagLib,
         ShipmentTagLib, GoogleAnalyticsTagLib, CategorySelectTagLib, LocalizationTagLib, MessageTagLib,
         ImageTagLib, org.grails.plugins.web.taglib.ValidationTagLib].each { mockTagLib(it) }
    }

    void cleanupSpec() {
        // Tags called without a namespace from inside a tag library (resource, createLink) get cached on the
        // meta classes, bound to this spec's application context. Drop them so later specs start clean.
        [ShipmentTagLib, ImageTagLib, org.grails.plugins.web.taglib.ApplicationTagLib,
         org.grails.plugins.web.taglib.ValidationTagLib].each { GroovySystem.metaClassRegistry.removeMetaClass(it) }
    }

    void setup() {
        config.openboxes.typeahead.delay = 250
        config.openboxes.typeahead.minLength = 2
        config.openboxes.locale.custom.enabled = false
    }

    private void stubLocale(Locale locale) {
        LocalizationService localizationStub = Stub(LocalizationService) {
            getCurrentLocale() >> locale
        }
        defineBeans {
            localizationService(InstanceFactoryBean, localizationStub, LocalizationService)
        }
    }

    void 'autoSuggest fills in defaults from the typeahead config'() {
        given:
        views['/taglib/_autoSuggest.gsp'] =
                '${attrs.id}|${attrs.width}|${attrs.size}|${attrs.delay}|${attrs.minLength}|${attrs.spanDisplay}|${attrs.suggestDisplay}|${raw(attrs.valueDataBind)}|${raw(attrs.textDataBind)}'

        expect:
        applyTemplate('<g:autoSuggest id="as" name="q"/>') == 'as|100%|30|250|2|none|inline||'
        applyTemplate('<g:autoSuggest id="as" name="q" width="200" size="5" delay="10" minLength="4" valueDataBind="v" textDataBind="t"/>') ==
                "as|200px|5|10|4|none|inline|data-bind='v'|data-bind='t'"
        applyTemplate('<g:autoSuggest name="q"/>').startsWith('autoSuggest_')
    }

    void 'chooseSubstitute and autoSuggestSearchable fill in their defaults'() {
        given:
        views['/taglib/_chooseSubstitute.gsp'] = '${attrs.id}|${attrs.width}|${attrs.minLength}|${attrs.jsonUrl}'
        views['/taglib/_autoSuggestSearchable.gsp'] = '${attrs.id}|${attrs.width}|${attrs.minLength}|${attrs.suggestDisplay}'
        views['/taglib/_autoSuggestString.gsp'] = 'string ${attrs.name}'

        expect:
        applyTemplate('<g:chooseSubstitute id="s" jsonUrl="/json"/>') == 's|300px|1|/json'
        applyTemplate('<g:chooseSubstitute width="10px" minLength="3"/>') =~ /^chooseSubstitute_-?\d+\|10px\|3\|$/
        applyTemplate('<g:autoSuggestSearchable id="s"/>') == 's|200px|1|inline'
        applyTemplate('<g:autoSuggestSearchable width="50px" minLength="5"/>') =~ /^autoSuggest_-?\d+\|50px\|5\|inline$/
        applyTemplate('<g:autoSuggestString name="city"/>') == 'string city'
    }

    void 'globalSearch picks the template depending on the search button'() {
        given:
        views['/taglib/_globalSearch.gsp'] = 'button ${attrs.name}|${attrs.size}|${attrs.display}'
        views['/taglib/_globalSearchStatic.gsp'] = 'static ${attrs.name}|${attrs.size}|${attrs.minLength}'

        expect:
        applyTemplate('<g:globalSearch id="gs" buttonId="go"/>') == 'button gs|150|visible'
        applyTemplate('<g:globalSearch id="gs" name="search" size="20" minLength="3"/>') == 'static search|20|3'
    }

    void 'comboBox and searchProduct render their templates'() {
        given:
        views['/taglib/_comboBox.gsp'] = 'combo ${attrs.name}'
        views['/_taglib/searchProduct.gsp'] = 'search ${attrs.name}'

        expect:
        applyTemplate('<g:comboBox name="c"/>') == 'combo c'
        applyTemplate('<g:searchProduct name="p"/>') == 'search p'
    }

    void 'decimalNumberField converts the value to the decimal separator of the current locale'() {
        given:
        views['/taglib/_decimalFormatField.gsp'] = '${attrs.value}|${decimalSeparator}'

        when:
        stubLocale(Locale.FRANCE)

        then:
        applyTemplate('<g:decimalNumberField name="n" value="${v}"/>', [v: 12.5]) == '12,5|,'

        when:
        stubLocale(Locale.US)

        then:
        applyTemplate('<g:decimalNumberField name="n" value="${v}"/>', [v: 12.5]) == '12.5|.'
        applyTemplate('<g:decimalNumberField name="n"/>') == '|.'
    }

    void 'sort sorts the items in place, optionally with a custom comparator'() {
        given:
        List<Integer> items = [3, 1, 2]
        List<String> words = ['bb', 'a', 'ccc']

        when:
        applyTemplate('<g:sort items="${items}"/>', [items: items])
        applyTemplate('<g:sort items="${items}" sorter="${s}"/>', [items: words, s: { a, b -> b.size() <=> a.size() }])

        then:
        items == [1, 2, 3]
        words == ['ccc', 'bb', 'a']
    }

    void 'wordwrap tags wrap every word in a non-breaking span'() {
        expect:
        applyTemplate('<wordwrap:div class="x">2026-03-05 PO/123</wordwrap:div>') ==
                "<div class='x'><span style='display: inline-block; white-space: nowrap;'>2026-03-05</span> " +
                "<span style='display: inline-block; white-space: nowrap;'>PO/123</span></div>"
        applyTemplate('<wordwrap:td>one</wordwrap:td>') ==
                "<td><span style='display: inline-block; white-space: nowrap;'>one</span></td>"
    }

    void 'shipment type icons depend on the shipment type name'() {
        expect:
        applyTemplate('<g:getShipmentTypeIcon shipmentType="${t}"/>', [t: type ? new ShipmentType(name: type) : null])
                .with { expected == null ? it == '' : it.contains(expected) }

        where:
        type          || expected
        null          || null
        'Air freight' || 'ShipmentTypeAir.png'
        'Sea'         || 'ShipmentTypeSea.png'
        'Land'        || 'ShipmentTypeLand.png'
        'Suitcase'    || 'ShipmentTypeSuitcase.png'
        'Default'     || 'ShipmentTypeDefault.png'
        'Courier'     || "<span class='fade'>Courier</span>"
    }

    void 'renderHandlingIcons shows an icon per handling requirement'() {
        given: 'the icons a cold chain, hazardous product reports (Product.getHandlingIcons is covered in ProductSpec)'
        def product = new HandlingIconsStub(icons: [[icon: 'fa-snowflake', color: '#3bafda'],
                                                    [icon: 'fa-exclamation-triangle', color: '#ffa500']])

        when:
        String html = applyTemplate('<g:renderHandlingIcons product="${p}"/>', [p: product])

        then:
        html == "<i class='fa fa-snowflake' style='color: #3bafda' ></i>&nbsp;" +
                "<i class='fa fa-exclamation-triangle' style='color: #ffa500' ></i>&nbsp;"
        applyTemplate('<g:renderHandlingIcons product="${null}"/>') == ''
    }

    void 'googleSiteTag injects one config call per tracking id'() {
        when:
        config.google.analytics.webPropertyID = 'G-ONE'
        String single = applyTemplate('<g:googleSiteTag/>')

        then:
        single.contains('https://www.googletagmanager.com/gtag/js?id=G-ONE')
        single.contains("gtag('config', 'G-ONE');")

        when:
        config.google.analytics.webPropertyID = ['G-ONE', 'G-TWO']
        String several = applyTemplate('<g:googleSiteTag/>')

        then:
        several.contains('gtag/js?id=G-ONE"')
        several.contains("gtag('config', 'G-ONE');")
        several.contains("gtag('config', 'G-TWO');")

        when:
        config.google.analytics.webPropertyID = ''

        then:
        applyTemplate('<g:googleSiteTag/>') == ''
    }

    void 'inventory tags show stock status and the ABC class of a product'() {
        given:
        Inventory inventory = new Inventory().save(validate: false)
        Location depot = new Location(name: 'Depot', inventory: inventory).save(validate: false, flush: true)
        Product product = new Product(name: 'Gloves', abcClass: 'B').save(validate: false, flush: true)
        Product other = new Product(name: 'Masks').save(validate: false, flush: true)
        InventoryLevel level = new InventoryLevel(product: product, inventory: inventory, abcClass: 'A')
                .save(validate: false, flush: true)
        session.warehouse = depot
        InventoryTagLib inventoryTagLib = applicationContext.getBean(InventoryTagLib)
        inventoryTagLib.inventoryService = Mock(InventoryService)
        views['/taglib/_productStatus.gsp'] = '${product.name}|${totalQuantity}|${inventoryLevel?.abcClass}'

        when:
        String status = applyTemplate('<g:productStatus product="${p}"/>', [p: product])

        then:
        1 * inventoryTagLib.inventoryService.getQuantityOnHand(depot, product) >> 12
        1 * inventoryTagLib.inventoryService.getInventoryLevelByProductAndInventory(product, inventory) >> level
        status == 'Gloves|12|A'

        expect:
        applyTemplate('<g:abcClassification product="${p}"/>', [p: product.id]) == 'A'
        applyTemplate('<g:abcClassification product="${p}" location="${l}"/>', [p: product.id, l: depot.id]) == 'A'

        when:
        level.delete(flush: true)

        then: 'falls back to the product class, then to none'
        applyTemplate('<g:abcClassification product="${p}"/>', [p: product.id]) == 'B'
        applyTemplate('<g:abcClassification product="${p}"/>', [p: other.id]) == 'default.none.label'
    }

    void 'categorySelect and selectCategory_v2 walk the category tree from the root'() {
        given:
        Category root = new Category(name: 'Root').save(validate: false)
        Category drugs = new Category(name: 'Drugs', parentCategory: root).save(validate: false)
        Category pills = new Category(name: 'A very long category name for pills and tablets in many forms and sizes',
                parentCategory: drugs).save(validate: false)
        Category supplies = new Category(name: 'Supplies', parentCategory: root).save(validate: false, flush: true)
        root.categories = [drugs, supplies]
        drugs.categories = [pills]
        CategorySelectTagLib categoryTagLib = applicationContext.getBean(CategorySelectTagLib)
        categoryTagLib.productService = Stub(ProductService) {
            getRootCategory() >> root
        }
        views['/_category/selectOptions.gsp'] = 'options from ${category.name} selected ${selected?.name} level ${level}'

        when:
        String v2 = applyTemplate('<g:selectCategory_v2 name="c" value="${v}"/>', [v: drugs.id])
        List<String> labels = (v2 =~ /<option [^>]*>([^<]*)<\/option>/).collect { it[1] }

        then:
        labels == ['-Choose a category-', 'Root (0)', 'Drugs (0)',
                   'A very long category name for pills and tablets in many forms and sizes (0)', 'Supplies (0)']
        v2.contains("value=\"${drugs.id}\" selected=\"selected\"")

        when:
        String abbreviated = applyTemplate('<g:selectCategory_v2 name="c" abbreviate="true"/>')

        then:
        abbreviated.contains('A very long category name for pills and tablets... (0)')

        when:
        String select = applyTemplate('<g:categorySelect id="cat" name="category" cssClass="big" value="${v}"/>', [v: drugs.id])

        then:
        select == "<select class='big' id='cat' name='category'>options from Root selected Drugs level 0</select>"
    }

    void 'the localization message tag uses the message localizer unless it is resolving an error'() {
        given:
        LocalizationTagLib localizationTagLib = applicationContext.getBean(LocalizationTagLib)
        localizationTagLib.messageLocalizer = Mock(MessageLocalizer)

        when:
        String html = applyTemplate('<UNUSED:message code="hello" args="${[\'Jo\']}" locale="${l}"/>', [l: Locale.FRENCH])

        then:
        1 * localizationTagLib.messageLocalizer.localize('hello', ['Jo'] as Object[], Locale.FRENCH) >> 'Bonjour Jo'
        html == 'Bonjour Jo'

        when:
        messageSource.addMessage('broken', Locale.ENGLISH, 'It broke')
        html = applyTemplate('<UNUSED:message message="${m}" locale="${l}"/>',
                [m: new org.springframework.context.support.DefaultMessageSourceResolvable('broken'), l: Locale.ENGLISH])

        then:
        0 * localizationTagLib.messageLocalizer._
        html == 'It broke'
    }

    void 'the warehouse message tag prefers a database translation for logged in users'() {
        given:
        stubLocale(Locale.ENGLISH)
        messageSource.addMessage('greeting', Locale.ENGLISH, 'Hello {0}')
        new Localization(code: 'greeting', locale: 'en', text: 'Hi {0} <db>').save(validate: false, flush: true)

        expect: 'bundle message while the custom store is off'
        applyTemplate('<warehouse:message code="greeting" args="${[\'Jo\']}"/>') == 'Hello Jo'

        when:
        config.openboxes.locale.custom.enabled = true
        session.user = [id: 'u1']

        then:
        applyTemplate('<warehouse:message code="greeting" args="${[\'Jo\']}"/>') == 'Hi Jo &lt;db&gt;'
        applyTemplate('<warehouse:message code="greeting" args="${[\'Jo\']}" locale="${l}"/>', [l: Locale.FRENCH]) ==
                'greeting'
    }

    void 'displayLogo uses the configured logo unless the location has its own'() {
        given:
        config.openboxes.logo.url = 'https://example.org/logo.png'
        views['/taglib/_displayLogo.gsp'] = '${attrs.logoUrl}|${attrs.locationName}'
        Location plain = new Location(name: 'Plain').save(validate: false, flush: true)
        Location branded = new Location(name: 'Branded', logo: [1, 2, 3] as byte[]).save(validate: false, flush: true)

        when:
        session.warehouse = plain

        then:
        applyTemplate('<g:displayLogo/>') == 'https://example.org/logo.png|Plain'

        when:
        session.warehouse = branded

        then:
        applyTemplate('<g:displayLogo/>') == "/location/viewLogo/${branded.id}|Branded"
    }

    void 'displayBarcode falls back to the configured barcode format and size'() {
        given:
        views['/taglib/_displayBarcode.gsp'] = '${attrs.format}|${attrs.height}|${attrs.width}|${attrs.showData}'

        when:
        config.openboxes.identifier.barcode.format = 'CODE_128'
        config.openboxes.identifier.barcode.height = '40'
        config.openboxes.identifier.barcode.width = '200'

        then:
        applyTemplate('<g:displayBarcode data="x"/>') == 'CODE_128|40|200|true'
        applyTemplate('<g:displayBarcode data="x" format="QR_CODE" height="1" width="2" showData="${false}"/>') ==
                'QR_CODE|1|2|false'

        when: 'unusable defaults are skipped'
        config.openboxes.identifier.barcode.format = 'NOT_A_FORMAT'
        config.openboxes.identifier.barcode.height = 'tall'
        config.openboxes.identifier.barcode.width = 'wide'

        then:
        applyTemplate('<g:displayBarcode data="x"/>') == '|||true'
    }

    static class HandlingIconsStub {
        List<Map> icons

        List<Map> getHandlingIcons() { icons }
    }
}
