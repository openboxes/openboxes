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

import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.product.Category
import spock.lang.Specification

@TestFor(CategoryApiController)
@Mock([User, UserService])
@TestMixin(DomainClassUnitTestMixin)
class CategoryApiControllerTests extends Specification {

    Category rootCategory = new Category(name: "ROOT")
    Category childCategory1 = new Category(name: "A category", parentCategory: rootCategory)
    Category childCategory2 = new Category(name: "Another category", parentCategory: rootCategory)
    Category grandChildCategory1 = new Category(name: "The last category", parentCategory: childCategory1)
    List<Category> categoryList = [rootCategory, childCategory1, childCategory2, grandChildCategory1]

    void "test get category list"() {
        when:
        mockDomain(Category.class, categoryList)
        controller.productService = [
                getCategoryTree: { -> return Category.list() }
        ]
        controller.list()

        then:
        JSONObject json = new JSONObject(controller.response.contentAsString)
        controller.response.status == 200
        json.data.size() == 4
        json.data[0].name == "ROOT"
    }

    void "test get category"() {
        when:
        mockDomain(Category.class, categoryList)
        controller.params.id = 1
        controller.read()

        then:
        JSONObject json = new JSONObject(controller.response.contentAsString)
        controller.response.status == 200
        json.name == "ROOT"
    }

    void "test create category"() {
        when:
        mockDomain(Category.class, categoryList)
        controller.request.contentType = "application/json"
        controller.request.method = 'POST'
        controller.request.content = '{ "name" : "a great great grandchild" }'
        controller.save()

        then:
        controller.response.status == 200
        def categories = Category.list()
        categories.size() == 5
    }

}
