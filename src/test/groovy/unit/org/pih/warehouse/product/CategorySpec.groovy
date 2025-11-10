/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package unit.org.pih.warehouse.product

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

import org.pih.warehouse.product.Category

class CategorySpec extends Specification implements DomainUnitTest<Category>{

    void 'getRootCategory should return the category marked as root'() {
        given:
        Category root = new Category(isRoot: true).save(validate: false)

        expect:
        assert Category.getRootCategory() == root
    }

    void 'getRootCategory should return the category with no parent'() {
        given:
        Category root = new Category(isRoot: false, parentCategory: null).save(validate: false)

        expect:
        assert Category.getRootCategory() == root
    }

    void 'getParents should get the full tree of parent categories'() {
        given:
        Category categoryA = new Category(parentCategory: null)
        Category categoryB = new Category(parentCategory: categoryA)
        Category categoryC = new Category(parentCategory: categoryB)
        Category categoryD = new Category(parentCategory: categoryC)

        expect: 'all categories above the given one, excluding itself (categories below it are excluded)'
        assert categoryC.getParents() == [
                categoryA,
                categoryB,
        ]
    }

    void 'getHierarchyAsString should get the full tree of parent categories by name'() {
        given:
        Category categoryA = new Category(name: 'A', parentCategory: null)
        Category categoryB = new Category(name: 'B', parentCategory: categoryA)
        Category categoryC = new Category(name: 'C', parentCategory: categoryB)
        Category categoryD = new Category(name: 'C', parentCategory: categoryB)

        expect: 'all categories above the given one, including itself (categories below it are excluded)'
        assert categoryC.getHierarchyAsString(",") == "A,B,C"
    }

    void 'getChildren should get all direct child categories of the given category'() {
        given:
        Category categoryA = new Category(categories: [])
        Category categoryB = new Category(categories: [])
        Category categoryC = new Category(categories: [categoryA, categoryB])
        Category categoryD = new Category(categories: [categoryC])

        expect: 'all categories below the given one, excluding itself (categories above it are excluded)'
        assert categoryC.getChildren() == [
                categoryA,
                categoryB,
        ]
    }

    void 'toJson returns a JSON representation of the category'() {
        given:
        domain.id = 1
        domain.description = 'description'
        domain.name = 'name'
        domain.version = 0
        domain.dateCreated = new Date(100, 0, 1)
        domain.lastUpdated = new Date(100, 0, 1)
        domain.sortOrder = 0
        domain.deleted = false
        domain.isRoot = true
        domain.categories = []

        when:
        Map json = domain.toJson()

        then:
        assert json.id == '1'
        assert json.description == 'description'
        assert json.name == 'name'
        assert json.version == 0
        assert json.dateCreated == '01/Jan/2000 12:00 AM'
        assert json.lastUpdated == '01/Jan/2000 12:00 AM'
        assert json.sortOrder == 0
        assert json.deleted == false
        assert json.isRoot == true
        assert json.categories.size() == 0
    }

    void 'sort should sort categories by sort order'() {
        given:
        Category category0 = new Category(sortOrder: 0)
        Category category1 = new Category(sortOrder: 1)
        Category category2 = new Category(sortOrder: 2)

        List<Category> categories = [
                category2,
                category0,
                category1,
        ]

        expect:
        categories.sort() == [
                category0,
                category1,
                category2,
        ]
    }
}
