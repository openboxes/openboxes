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

class CategoryController {

    def productService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }


    def tree = {
        long startTime = System.currentTimeMillis()
        log.info params
        def categoryInstance = Category.get(params.id)

        println "Category tree: " + (System.currentTimeMillis() - startTime) + " ms"

        [rootCategory: productService.getRootCategory(), categoryInstance: categoryInstance]
    }


    def move = {
        log.info params
        def parent = Category.get(params.newParent)
        def child = Category.get(params.child)
        log.info parent
        log.info child
        child.parentCategory = parent
        if (!child.hasErrors() && child.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.success.message')}"
        }
        redirect(action: "tree")
    }

    def saveCategory = {
        log.info params

        def categoryInstance = Category.get(params.id)
        if (!categoryInstance) {
            categoryInstance = new Category(params)
        } else {
            categoryInstance.properties = params
        }

        if (!categoryInstance.hasErrors() && categoryInstance.save()) {
            flash.message = "${warehouse.message(code: 'category.saved.message', arg: [format.category(category: categoryInstance)])}"
            redirect(action: "tree", model: [rootCategory: productService.getRootCategory()])
        } else {
            render(view: "edit", model: [categoryInstance: categoryInstance])
        }
    }

    def deleteCategory = {
        log.info params
        def categoryInstance = Category.get(params.id)

        if (categoryInstance) {
            try {
                categoryInstance.delete(flush: true)
            } catch (Exception e) {
                //categoryInstance.errors.reject(e.getMessage())
                throw e
            }
        }

        redirect(action: "tree")
    }


    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [categoryInstanceList: Category.list(params), categoryInstanceTotal: Category.count()]
    }

    def create = {
        def categoryInstance = new Category()
        categoryInstance.properties = params

        return [categoryInstance: categoryInstance, rootCategory: productService.getRootCategory()]
    }

    def save = {
        def categoryInstance = new Category(params)

        if (categoryInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'category.label', default: 'Category'), categoryInstance.id])}"
            redirect(action: "tree", id: categoryInstance.id)
        } else {
            render(view: "create", model: [categoryInstance: categoryInstance])
        }
    }

    def show = {
        def categoryInstance = Category.get(params.id)
        if (!categoryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        } else {
            [categoryInstance: categoryInstance]
        }
    }

    def edit = {
        def categoryInstance = Category.get(params.id)

        if (!categoryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'category.label', default: 'Category'), params.id])}"
            redirect(action: "tree")
        } else {
            return [categoryInstance: categoryInstance]
        }
    }

    def update = {
        def categoryInstance = Category.get(params.id)
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
            if (!categoryInstance.hasErrors() && categoryInstance.save(flush: true)) {
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

    def delete = {
        def categoryInstance = Category.get(params.id)
        if (categoryInstance) {
            try {
                categoryInstance.delete(flush: true)
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
