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

import grails.test.GrailsMock
import grails.test.GrailsUnitTestCase
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category
import org.pih.warehouse.util.LocalizationUtil
import org.springframework.context.ApplicationEvent

class SynonymTests extends GrailsUnitTestCase {


    protected void setUp() {
        super.setUp()
        //mockForConstraintsTests(Synonym)
        mockConfig("openboxes.locale.supportedLocales = ['ar','ach','de','en','es','fr','it','pt','fi','zh']")
        def product1 = new Product(name: "new product", category: new Category(name: "new category"))
        def product2 = new Product(name: "product with no synonyms", category: new Category(name: "new category"))
        def synonym1 = new Synonym(name: "new synonym 1", product:product1, synonymTypeCode: SynonymTypeCode.DISPLAY_NAME)
        def synonym2 = new Synonym(name: "new synonym 2", product:product1, synonymTypeCode: SynonymTypeCode.DISPLAY_NAME)
        def category = new Category(name: "new category")

        mockDomain(Synonym, [synonym1,synonym2])
        mockDomain(Product, [product1,product2])
        mockDomain(Category, [category])

        GrailsMock localizationUtilMock = mockFor(LocalizationUtil)
        localizationUtilMock.demand.static.getCurrentLocale { -> new Locale("en") }
        localizationUtilMock.createMock()

        User.metaClass.static.withNewSession = {Closure c -> c.call() }
        Synonym.metaClass.static.withNewSession = {Closure c -> c.call() }
        Product.metaClass.static.withNewSession = {Closure c -> c.call() }
        Product.metaClass.publishEvent = { ApplicationEvent event -> }
        //mockDomain(LocationType, [depot, supplier, ward])
        //mockDomain(Location, [location1, location2, location3, location4])
    }



    @Test
    void validate_shouldPassValidation() {
        def synonym = new Synonym(name: "a synonym", product: new Product(), synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("en"))
        assertTrue synonym.validate()
        println synonym.errors
        assertTrue !synonym.hasErrors()
    }

    @Test
    void validate_shouldFailValidation() {
        def synonym = new Synonym()
        assertTrue !synonym.validate()
        println synonym.errors
        assertTrue synonym.hasErrors()
        assertEquals 4, synonym.errors.errorCount
    }

    @Test
    void addSynonym() {
        def product = Product.findByName("new product")
        assertNotNull(product)
        product.addToSynonyms(new Synonym(name: "new synonym", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME))
        product.save(flush: true)
        assertEquals 1, product.synonyms.size()

    }

    @Ignore
    void deleteSynonym() {
        def product = Product.findByName("new product")
        //product.addToSynonyms(new Synonym(name: "new synonym"))
        //product.save(flush: true)
        assertEquals 2, product.synonyms.size()

        def synonym = Synonym.findByName("new synonym")
        //product.removeFromSynonyms(synonym)
        //product.save(flush:true)
        synonym.delete()

        assertEquals 1, Synonym.list().size()

        //assertNotNull result
        //assertEquals "new synonym", result.name
    }

    @Ignore
    void deleteProduct_shouldCascadeDeleteSynonyms() {
        def product = Product.findByName("new product")
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
        Product product = Product.findByName("new product")
        Synonym synonym = new Synonym(name: "test synonym", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("en"))
        product.addToSynonyms(synonym)
        String displayName = product.displayNames.default
        assertEquals("test synonym", displayName)
    }

    @Test
    void test_shouldReturnDisplayNameWhenAccessingByLocaleKey() {
        Product product = Product.findByName("new product")
        Synonym synonym = new Synonym(name: "test synonym de", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("de"))
        product.addToSynonyms(synonym)
        String displayName = product.displayNames.de
        assertEquals("test synonym de", displayName)
    }

    @Test
    void test_shouldReturnNullWhenNoDisplayNameForGivenLocale() {
        Product product = Product.findByName("new product")
        Synonym synonym = new Synonym(name: "test synonym de", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("de"))
        product.addToSynonyms(synonym)
        String displayName = product.displayNames.ach
        assertNull(displayName)
    }

    @Test
    void test_shouldReturnCorrectPropertiesOfDisplayNamesMapIncludingDefaultKey() {
        Product product = Product.findByName("new product")
        Synonym synonym = new Synonym(name: "test synonym de", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("de"))
        Synonym synonym2 = new Synonym(name: "test synonym en", synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: new Locale("en"))
        product.addToSynonyms(synonym)
        product.addToSynonyms(synonym2)
        Map<String, String> displayNames = product.displayNames
        assertEquals(3, displayNames.size())
        List<String> expectedKeys = ["default", "de", "en"]
        assertTrue(expectedKeys.containsAll(displayNames.keySet()))
    }


}
