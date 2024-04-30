/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest

// import grails.test.GrailsMock
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category
import org.pih.warehouse.LocalizationUtil
import org.springframework.context.ApplicationEvent
import spock.lang.Specification
import static org.junit.Assert.*;

//@Ignore
class SynonymTests extends Specification implements DomainUnitTest<Product> {

    // TODO: fix mocking
    protected void setup() {
        // mockForConstraintsTests(Synonym)
        // mockConfig("openboxes.locale.supportedLocales = ['ar','ach','de','en','es','fr','it','pt','fi','zh']")
        def product1 = new Product(name: "new product", category: new Category(name: "new category"))
        def product2 = new Product(name: "product with no synonyms", category: new Category(name: "new category"))
        def synonym1 = new Synonym(name: "new synonym 1", product:product1, synonymTypeCode: SynonymTypeCode.DISPLAY_NAME)
        def synonym2 = new Synonym(name: "new synonym 2", product:product1, synonymTypeCode: SynonymTypeCode.DISPLAY_NAME)
        def category = new Category(name: "new category")

        mockDomain(Synonym, [synonym1,synonym2])
        mockDomain(Product, [product1,product2])
        mockDomain(Category, [category])

        // GrailsMock localizationUtilMock = mockFor(LocalizationUtil)
        // localizationUtilMock.demand.static.getCurrentLocale { -> new Locale("en") }
        // localizationUtilMock.createMock()

        User.metaClass.static.withNewSession = { Closure c -> c.call() }
        Synonym.metaClass.static.withNewSession = {Closure c -> c.call() }
        Product.metaClass.static.withNewSession = {Closure c -> c.call() }
        Product.metaClass.publishEvent = { ApplicationEvent event -> }
        //mockDomain(LocationType, [depot, supplier, ward])
        //mockDomain(Location, [location1, location2, location3, location4])
    }



    @Test
    void validate_shouldPassValidation() {
        when:
        def synonym = new Synonym(name: "a synonym", product: new Product(), synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("en"))
        then:
        assertTrue synonym.validate()
        println synonym.errors
        assertTrue !synonym.hasErrors()
    }

    @Test
    void validate_shouldFailValidation() {
        when:
        def synonym = new Synonym()
        then:
        assertTrue !synonym.validate()
        println synonym.errors
        assertTrue synonym.hasErrors()
        assertEquals 4, synonym.errors.errorCount
    }

    @Test
    void addSynonym() {
        when:
        def product = Product.findByName("new product")
        then:
        assertNotNull(product)
        product.addToSynonyms(new Synonym(name: "new synonym", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME))
        product.save(flush: true)
        assertEquals 1, product.synonyms.size()

    }

//    @Ignore
    @Test
    void deleteSynonym() {
        when:
        def product = Product.findByName("new product")
        //product.addToSynonyms(new Synonym(name: "new synonym"))
        //product.save(flush: true)
        then:
        assertEquals 2, product.synonyms.size()

        def synonym = Synonym.findByName("new synonym")
        //product.removeFromSynonyms(synonym)
        //product.save(flush:true)
        synonym.delete()

        assertEquals 1, Synonym.list().size()

        //assertNotNull result
        //assertEquals "new synonym", result.name
    }

//    @Ignore
    @Test
    void deleteProduct_shouldCascadeDeleteSynonyms() {
        when:
        def product = Product.findByName("new product")

        then:
        assertEquals 2, product.synonyms.size()

        // Doesn't work for some reason
        assertEquals 2, Synonym.list().size()

        //def result = Synonym.findByName("new synonym")
        //assertNotNull result
        //assertEquals "new synonym", result.name

        product.delete()

        // For some reason the list() method is not working properly
        assertEquals 0, Synonym.list().size()


    }

    @Test
    void test_shouldReturnDisplayNameWhenAccessingByDefaultKey() {
        when:
        Product product = Product.findByName("new product")
        Synonym synonym = new Synonym(name: "test synonym", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("en"))
        product.addToSynonyms(synonym)
        String displayName = product.displayNames.default
        then:
        assertEquals("test synonym", displayName)
    }

    @Test
    void test_shouldReturnDisplayNameWhenAccessingByLocaleKey() {
        when:
        Product product = Product.findByName("new product")
        Synonym synonym = new Synonym(name: "test synonym de", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("de"))
        product.addToSynonyms(synonym)
        String displayName = product.displayNames.de
        then:
        assertEquals("test synonym de", displayName)
    }

    @Test
    void test_shouldReturnNullWhenNoDisplayNameForGivenLocale() {
        when:
        Product product = Product.findByName("new product")
        Synonym synonym = new Synonym(name: "test synonym de", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("de"))
        product.addToSynonyms(synonym)
        String displayName = product.displayNames.ach

        then:
        assertNull(displayName)
    }

    @Test
    void test_shouldReturnCorrectPropertiesOfDisplayNamesMapIncludingDefaultKey() {
        when:
        Product product = Product.findByName("new product")
        Synonym synonym = new Synonym(name: "test synonym de", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("de"))
        Synonym synonym2 = new Synonym(name: "test synonym en", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("en"))
        product.addToSynonyms(synonym)
        product.addToSynonyms(synonym2)
        Map<String, String> displayNames = product.displayNames

        then:
        assertEquals(3, displayNames.size())
        List<String> expectedKeys = ["default", "de", "en"]
        assertTrue(expectedKeys.containsAll(displayNames.keySet()))
    }


}
