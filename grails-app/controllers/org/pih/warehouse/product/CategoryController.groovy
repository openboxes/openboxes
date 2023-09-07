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

// TODO: Fix CacheFlush
// import grails.plugin.springcache.annotations.CacheFlush

class CategoryController {

    def productService
    CategoryDataService categoryGormService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def tree() {
        long startTime = System.currentTimeMillis()
        def selectedCategory = Category.get(params.id) ?: productService.getRootCategory()

        println "Category tree: " + (System.currentTimeMillis() - startTime) + " ms"
        List<Category> categoriesWithoutParent = productService.getCategoriesWithoutParent()

        [
            selectedCategory:           selectedCategory,
            categoriesWithoutParent:    categoriesWithoutParent,
        ]
    }

    def move() {
        def parent = Category.get(params.newParent)
        def child = Category.get(params.child)
        child.parentCategory = parent

        if (!child.hasErrors()) {
            categoryGormService.save(child)
            flash.message = "${warehouse.message(code: 'default.success.message')}"
        }
        redirect(action: "tree")
    }

    //  @CacheFlush("selectCategoryCache")
    def saveCategory() {
        def categoryInstance = Category.get(params.id)
        if (!categoryInstance) {
            categoryInstance = new Category(params)
        } else {
            categoryInstance.properties = params
        }

        try {
            categoryGormService.save(categoryInstance)
            flash.message = "${warehouse.message(code: 'category.saved.message', arg: [format.category(category: categoryInstance).decodeHTML()])}"
            redirect(action: "tree", model: [rootCategory: productService.getRootCategory()])
        } catch (Exception e) {
            render(view: "edit", model: [categoryInstance: categoryInstance])
        }
    }

    def deleteCategory() {
        Category categoryInstance = categoryGormService.get(params.id)

        if (categoryInstance) {
            try {
                categoryGormService.delete(params.id)
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.error = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
                List<Category> categoriesWithoutParent = productService.getCategoriesWithoutParent()

                return render(view: "tree", model: [
                        selectedCategory:           categoryInstance,
                        categoriesWithoutParent:    categoriesWithoutParent,
                ])
            }
        }

        redirect(action: "tree")
    }


    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [categoryInstanceList: Category.list(params), categoryInstanceTotal: Category.count()]
    }

    def create() {
        def categoryInstance = new Category()
        categoryInstance.properties = params

        return [categoryInstance: categoryInstance, rootCategory: productService.getRootCategory()]
    }

    //  @CacheFlush("selectCategoryCache")
    def save() {
        def categoryInstance = new Category(params)
        try {
            categoryGormService.save(categoryInstance)
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'category.label', default: 'Category'), categoryInstance.id])}"
            redirect(action: "tree", id: categoryInstance.id)
        } catch(Exception e) {
            render(view: "create", model: [categoryInstance: categoryInstance])
        }
    }

    def show() {
        def categoryInstance = Category.get(params.id)
        if (!categoryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        } else {
            [categoryInstance: categoryInstance]
        }
    }

    def edit() {
        def categoryInstance = Category.get(params.id)

        if (!categoryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        } else {
            return [categoryInstance: categoryInstance]
        }
    }

    //  @CacheFlush("selectCategoryCache")
    def update() {
        Category categoryInstance = categoryGormService.get(params.id)
        if (categoryInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (categoryInstance.version > version) {

                    categoryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'category.label', default: 'Category')] as Object[], "Another user has updated this Category while you were editing")
                    render(view: "edit", model: [categoryInstance: categoryInstance])
                    return
                }
            }
            categoryInstance.properties = params
            if (!categoryInstance.hasErrors()) {
                categoryGormService.save(categoryInstance)
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'category.label', default: 'Category'), categoryInstance.id])}"
                redirect(action: "tree", id: categoryInstance.id)
            } else {
                render(view: "edit", model: [categoryInstance: categoryInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        }
    }

    //  @CacheFlush("selectCategoryCache")
    def delete() {
        Category categoryInstance = categoryGormService.get(params.id)
        if (categoryInstance) {
            try {
                categoryGormService.delete(params.id)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
                redirect(action: "tree")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
                redirect(action: "tree", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        }
    }
}
