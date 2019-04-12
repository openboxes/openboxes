/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.api

import grails.converters.JSON
import grails.test.ControllerUnitTestCase
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.runtime.GroovyCategorySupport
import org.pih.warehouse.product.Category

class CategoryApiControllerTests extends ControllerUnitTestCase {

    Category rootCategory = new Category(name: "ROOT")
    Category childCategory1 = new Category(name: "A category", parentCategory: rootCategory)
    Category childCategory2 = new Category(name: "Another category", parentCategory: rootCategory)
    Category grandChildCategory1 = new Category(name: "The last category", parentCategory: childCategory1)
    List<Category> categoryList = [rootCategory, childCategory1, childCategory2, grandChildCategory1]

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGetCategoryList() {
        mockDomain(Category.class, categoryList)
        controller.productService = [
                getCategoryTree: { -> return Category.list() }
        ]
        controller.list()
        println controller.response.contentAsString
        JSONObject json = new JSONObject(controller.response.contentAsString)
        assertEquals(200, controller.response.status)
        assertEquals(4, json.data.size())
        assertEquals("ROOT", json.data[0].name)
    }

    void testGetCategory() {
        mockDomain(Category.class, categoryList)
        controller.params.id = 1
        controller.read()
        println controller.response.contentAsString
        JSONObject json = new JSONObject(controller.response.contentAsString)
        assertEquals(200, controller.response.status)
        assertEquals("ROOT", json.name)
    }

    void testCreateCategory() {
        mockDomain(Category.class, categoryList)
        controller.request.contentType = "application/json"
        //controller.request.json = '{ "name" : "a great great grandchild" }'
        controller.request.method = 'POST'
        controller.request.content = '{ "name" : "a great great grandchild" }'
        controller.save()
        println controller.response.contentAsString

        assertEquals(200, controller.response.status)
        def categories = Category.list()
        assertEquals(5, categories.size())
    }

}
