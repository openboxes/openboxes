/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.product

import grails.test.GrailsUnitTestCase
import org.junit.Test
// import org.pih.warehouse.inventory.InventoryItem

class CategoryTests extends GrailsUnitTestCase {


    def category1 = new Category(name: "A", parentCategory: null)
    def category2 = new Category(name: "B", parentCategory: category1)
    def category3 = new Category(name: "C", parentCategory: category1)
    def category4 = new Category(name: "D", parentCategory: category3)
    def category5 = new Category(name: "E", parentCategory: category4)
    def category6 = new Category(name: "F", parentCategory: category5)
    def category7 = new Category(name: "G", parentCategory: category6)
    def category8 = new Category(name: "H", parentCategory: category7)



    protected void setUp() {
        super.setUp()

        def product1 = new Product(name: "Product A", category: category8)
        def product2 = new Product(name: "Product B", category: category8)

        mockDomain(Product, [product1, product2])
        mockDomain(Category, [category1,category2,category3,category4,category5,category6,category7,category8])

        category7.addToCategories(category8)
        category6.addToCategories(category7)
        category5.addToCategories(category6)
        category4.addToCategories(category5)
        category3.addToCategories(category4)
        category1.addToCategories(category3)
        category1.addToCategories(category2)
    }

    protected void tearDown() {
        super.tearDown()
    }

    @Test
    void getRootCategory_shouldGetRootCategoryWhereParentCategoryIsNull() {
        def rootCategory = Category.findByName("A")
        assertNotNull rootCategory
        assertEquals rootCategory, Category.getRootCategory()
        assertNull rootCategory.parentCategory
        assertFalse rootCategory.isRoot
    }

    @Test
    void getRootCategory_shouldGetRootCategoryByIsRoot() {
        //def rootCategory = Category.getRootCategory()
        // def newRootCategory = new Category(name: "E", parentCategory: null, isRoot: true).save(failOnError: true)
        def rootCategory = Category.getRootCategory()
        assertNotNull rootCategory
        assertEquals "A", rootCategory.name
        assertTrue rootCategory.isRootCategory()
    }

    @Test
    void getParents_shouldGetAllParents() {
        def category = Category.findByName("H")
        println category.getParents()
        def parents = [category1,category3,category4,category5,category6,category7]
        assertEquals parents, category.getParents()
    }

    @Test
    void getHierarchyAsString_shouldGetHierarchy() {
        def category = Category.findByName("H")
        println category.getHierarchyAsString(",")
        assertEquals "A,C,D,E,F,G,H", category.getHierarchyAsString(",")
    }


    @Test
    void getChildren_shouldReturnAllChildren() {
        def category = Category.findByName("A")
        assertEquals 2, category.categories.size()

        println category.getChildren()
        def expected = [category2, category3,category4,category5,category6,category7,category8].reverse()
        assertEquals expected, category.getChildren()
    }

    @Test
    void getProducts_shouldReturnAllProducts() {
        def category = Category.findByName("H")
        assertEquals 2, category.products.size()
    }


    @Test
    void validate_shouldFailOnNullableError() {
        def category = new Category()
        assertFalse category.validate()
        assertTrue category.hasErrors()
        assertEquals "nullable", category.errors["name"]
    }


    @Test
    void hashCode_shouldReturnID() {
        def category = new Category(id: "A", name: "Kid A", parentCategory: null).save()
        assertNotNull category
        assertEquals "A", category.hashCode()

    }

    @Test
    void equals_shouldReturnTrueWhenCategoriesAreEqual() {
        def category = Category.getRootCategory();
        def categoryA = Category.findByName("A")
        assertEquals category, categoryA
    }


    @Test
    void equals_shouldReturnFalseWhenCategoriesAreNotEqual() {
        def categoryA = Category.findByName("A")
        def categoryB = Category.findByName("B")
        assertNotSame categoryA, categoryB
    }

    @Test
    void toJson() {
        def categoryA = Category.findByName("A")

        def json = categoryA.toJson()
        assertEquals categoryA.id, json.id
        assertEquals categoryA.name, json.name
        assertEquals categoryA.description, json.description
        assertEquals 2, json.categories.size()

    }


    @Test
    void compareTo_shouldSortCategoriesByName() {

        def categories = Category.findAll()
        categories = categories.sort();
        println categories
    }

    @Test
    void compareTo_shouldSortCategoriesBySortOrder() {
        category1.sortOrder = 10
        category2.sortOrder = 20
        category8.sortOrder = 1
        def categories = [category1, category2, category8]
        categories = categories.sort()
        println categories

        assertEquals categories[0], category8
        assertEquals categories[1], category1
        assertEquals categories[2], category2
    }


}
