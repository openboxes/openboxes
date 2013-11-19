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

import grails.test.GrailsUnitTestCase
import org.junit.Before
import org.junit.Test
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category

class SynonymTests extends GrailsUnitTestCase {



    protected void setUp() {
        super.setUp()
        //mockForConstraintsTests(Synonym)
        mockDomain(Synonym)
        mockDomain(Product)
        mockDomain(Category)

        Synonym.metaClass.static.withNewSession = {Closure c -> c.call() }
        //mockDomain(LocationType, [depot, supplier, ward])
        //mockDomain(Location, [location1, location2, location3, location4])
    }



    @Test
    void validate_shouldPassValidation() {
        def synonym = new Synonym(synonym: "a synonym")
        assert synonym.validate()
        assert !synonym.hasErrors()
        println synonym.errors
    }

    @Test
    void validate_shouldFailValidation() {
        def synonym = new Synonym()
        assert !synonym.validate()
        assert synonym.hasErrors()
        assertEquals 1, synonym.errors.errorCount
        println synonym.errors
    }

    @Test
    void addSynonym() {
        def synonym = new Synonym(synonym: "new synonym").save(flush: true)
        def product = new Product(name: "new product", category: new Category(name: "new category")).save(flush: true, failOnError: true)

        product.addToSynonyms(synonym)
        product.save(flush: true)
        assertEquals 1, product.synonyms.size()

        def result = Synonym.findBySynonym("new synonym")
        assertNotNull result
        assertEquals "new synonym", result.synonym
    }


}
