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

import grails.validation.ValidationException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.PreferenceType
import java.math.RoundingMode
import java.text.SimpleDateFormat
import org.pih.warehouse.core.ProductPrice

class ProductSupplierController {

    def dataService
    def documentService
    def identifierService

    static allowedMethods = [save: "POST", update: "POST", delete: ["GET", "POST"]]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def productSuppliers = ProductSupplier.createCriteria().list(max: params.max, offset: params.offset) {
            if (params.product?.id) {
                eq('product.id', params.product.id)
            }
            if (params.supplierId) {
                eq('supplier.id', params.supplierId)
            }
            if (params.manufacturerId) {
                eq('manufacturer.id', params.manufacturerId)
            }
            if (params.q) {
                or {
                    ilike("productCode", "%" + params.q + "%")
                    ilike("code", "%" + params.q + "%")
                    ilike("name", "%" + params.q + "%")
                    ilike("supplierCode", "%" + params.q + "%")
                    ilike("supplierName", "%" + params.q + "%")
                    ilike("manufacturerCode", "%" + params.q + "%")
                    ilike("manufacturerName", "%" + params.q + "%")
                }
            }
        }
        [productSupplierInstanceList: productSuppliers, productSupplierInstanceTotal: productSuppliers.totalCount]
    }

    def create = {
        def productSupplierInstance = new ProductSupplier()
        productSupplierInstance.properties = params
        return [productSupplierInstance: productSupplierInstance]
    }

    def save = {
        def productSupplierInstance = new ProductSupplier(params)
        updateAttributes(productSupplierInstance, params)

        if (!productSupplierInstance.code) {
            String prefix = productSupplierInstance?.product?.productCode
            productSupplierInstance.code = identifierService.generateProductSupplierIdentifier(prefix)
        }

        if (params.defaultPreferenceType) {
            PreferenceType preferenceType = PreferenceType.get(params.defaultPreferenceType)
            ProductSupplierPreference defaultPreference = new ProductSupplierPreference()
            defaultPreference.preferenceType = preferenceType
            defaultPreference.productSupplier = productSupplierInstance
            productSupplierInstance.addToProductSupplierPreferences(defaultPreference)
        }

        if (params.preferenceType) {
            PreferenceType preferenceType = PreferenceType.get(params.preferenceType)
            Location location = Location.get(session.warehouse.id)
            ProductSupplierPreference productSupplierPreference = new ProductSupplierPreference()
            productSupplierPreference.preferenceType = preferenceType
            productSupplierPreference.destinationParty = location.organization
            productSupplierPreference.productSupplier = productSupplierInstance
            productSupplierInstance.addToProductSupplierPreferences(productSupplierPreference)
        }

        if (params.price) {
            BigDecimal parsedUnitPrice
            try {
                parsedUnitPrice = new BigDecimal(params.price).setScale(2, RoundingMode.FLOOR)
            } catch (Exception e) {
                log.error("Unable to parse unit price: " + e.message, e)
                flash.message = "Could not parse unit price with value: ${params.price}."
                render(view: "edit", model: [productSupplierInstance: productSupplierInstance])
                return
            }
            if (parsedUnitPrice < 0) {
                log.error("Wrong unit price value: ${parsedUnitPrice}.")
                flash.message = "Wrong unit price value: ${parsedUnitPrice}."
                render(view: "edit", model: [productSupplierInstance: productSupplierInstance])
                return
            }
            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")

            if (productSupplierInstance.contractPrice?.id) {
                productSupplierInstance.contractPrice.price = parsedUnitPrice
                productSupplierInstance.contractPrice.toDate = params.toDate ? dateFormat.parse(params.toDate) : null
            } else {
                ProductPrice productPrice = new ProductPrice()
                productPrice.price = parsedUnitPrice
                productPrice.toDate = params.toDate ? dateFormat.parse(params.toDate) : null
                productSupplierInstance.contractPrice = productPrice
            }
        }

        if (productSupplierInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), productSupplierInstance.id])}"

            if (params.dialog) {
                redirect(controller: "product", action: "edit", id: productSupplierInstance?.product?.id)
            } else {
                redirect(action: "list", id: productSupplierInstance.id)
            }
        } else {
            render(view: "create", model: [productSupplierInstance: productSupplierInstance])
        }
    }

    def show = {
        def productSupplierInstance = ProductSupplier.get(params.id)
        if (!productSupplierInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            redirect(action: "list")
        } else {
            [productSupplierInstance: productSupplierInstance]
        }
    }

    def edit = {
        def productSupplierInstance = ProductSupplier.get(params.id)
        Location location = Location.get(session.warehouse.id)
        ProductSupplierPreference preference = productSupplierInstance?.productSupplierPreferences?.find {it.destinationParty == location.organization }
        if (!productSupplierInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            redirect(action: "list")
        } else {
            return [productSupplierInstance: productSupplierInstance, preferenceType: preference?.preferenceType, defaultPreferenceType: productSupplierInstance?.globalProductSupplierPreference?.preferenceType]
        }
    }

    def update = {
        def productSupplierInstance = ProductSupplier.get(params.id)
        Location location = Location.get(session.warehouse.id)
        ProductSupplierPreference defaultPreference = productSupplierInstance?.globalProductSupplierPreference
        ProductSupplierPreference productSupplierPreference = productSupplierInstance?.productSupplierPreferences?.find {it.destinationParty == location.organization }

        if (productSupplierInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productSupplierInstance.version > version) {

                    productSupplierInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier')] as Object[], "Another user has updated this ProductSupplier while you were editing")
                    render(view: "edit", model: [productSupplierInstance: productSupplierInstance])
                    return
                }
            }
            productSupplierInstance.properties = params

            updateAttributes(productSupplierInstance, params)

            if (!productSupplierInstance.code) {
                String prefix = productSupplierInstance?.product?.productCode
                productSupplierInstance.code = identifierService.generateProductSupplierIdentifier(prefix)
            }

            if (params.defaultPreferenceType) {
                PreferenceType preferenceType = PreferenceType.get(params.defaultPreferenceType)
                if (defaultPreference) {
                    defaultPreference.preferenceType = preferenceType
                } else {
                    defaultPreference = new ProductSupplierPreference()
                    defaultPreference.preferenceType = preferenceType
                    defaultPreference.productSupplier = productSupplierInstance
                    productSupplierInstance.addToProductSupplierPreferences(defaultPreference)
                }
            } else if (defaultPreference) {
                productSupplierInstance.removeFromProductSupplierPreferences(defaultPreference)
                defaultPreference.delete()
            }

            if (params.preferenceType) {
                PreferenceType preferenceType = PreferenceType.get(params.preferenceType)
                if (productSupplierPreference) {
                    productSupplierPreference.preferenceType = preferenceType
                } else {
                    productSupplierPreference = new ProductSupplierPreference()
                    productSupplierPreference.preferenceType = preferenceType
                    productSupplierPreference.destinationParty = location.organization
                    productSupplierPreference.productSupplier = productSupplierInstance
                    productSupplierInstance.addToProductSupplierPreferences(productSupplierPreference)
                }
            } else if (productSupplierPreference) {
                productSupplierInstance.removeFromProductSupplierPreferences(productSupplierPreference)
                productSupplierPreference.delete()
            }

            if (params.price) {
                BigDecimal parsedUnitPrice
                try {
                    parsedUnitPrice = new BigDecimal(params.price).setScale(2, RoundingMode.FLOOR)
                } catch (Exception e) {
                    log.error("Unable to parse unit price: " + e.message, e)
                    flash.message = "Could not parse unit price with value: ${params.price}."
                    render(view: "edit", model: [productSupplierInstance: productSupplierInstance])
                    return
                }
                if (parsedUnitPrice < 0) {
                    log.error("Wrong unit price value: ${parsedUnitPrice}.")
                    flash.message = "Wrong unit price value: ${parsedUnitPrice}."
                    render(view: "edit", model: [productSupplierInstance: productSupplierInstance])
                    return
                }
                def dateFormat = new SimpleDateFormat("MM/dd/yyyy")

                if (productSupplierInstance.contractPrice?.id) {
                    productSupplierInstance.contractPrice.price = parsedUnitPrice
                    productSupplierInstance.contractPrice.toDate = params.toDate ? dateFormat.parse(params.toDate) : null
                } else {
                    ProductPrice productPrice = new ProductPrice()
                    productPrice.price = parsedUnitPrice
                    productPrice.toDate = params.toDate ? dateFormat.parse(params.toDate) : null
                    productSupplierInstance.contractPrice = productPrice
                }
            } else if (productSupplierInstance.contractPrice?.id) {
                productSupplierInstance.contractPrice = null
            }

            if (!productSupplierInstance.hasErrors() && productSupplierInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), productSupplierInstance.id])}"

                if (params.dialog) {
                    redirect(controller: "product", action: "edit", id: productSupplierInstance?.product?.id)
                } else {
                    redirect(action: "list", id: productSupplierInstance.id)
                }

            } else {
                render(view: "edit", model: [productSupplierInstance: productSupplierInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productSupplierInstance = ProductSupplier.get(params.id)
        if (productSupplierInstance) {
            try {
                productSupplierInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error("Unable to delete product supplier: " + e.message, e)
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            }

            if (params.dialog) {
                redirect(controller: "product", action: "edit", id: productSupplierInstance?.product?.id)
            } else {
                redirect(action: "list", id: productSupplierInstance.id)
            }

        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            redirect(action: "list")
        }
    }

    def dialog = {
        log.info "Display dialog " + params
        def product = Product.get(params.product.id)
        def productSupplier = ProductSupplier.get(params.id)
        Location location = Location.get(session.warehouse.id)
        ProductSupplierPreference preference = productSupplier ?
                (productSupplier?.productSupplierPreferences?.find {it.destinationParty == location.organization } ?: productSupplier.globalProductSupplierPreference) : null

        // If not found, initialize a new product supplier
        if (!productSupplier) {
            productSupplier = new ProductSupplier()
            productSupplier.product = product
        }
        render(template: "dialog", model: [productSupplier: productSupplier, preferenceType: preference?.preferenceType])
    }

    def export = {
        def productSuppliers = params.list("productSupplier.id") ?
                ProductSupplier.findAllByIdInList(params.list("productSupplier.id")) : ProductSupplier.list()
        def data = productSuppliers ? dataService.transformObjects(productSuppliers, ProductSupplier.PROPERTIES) : [[:]]

        switch(params.format) {
            case "xls":
                response.contentType = "application/vnd.ms-excel"
                response.setHeader 'Content-disposition',
                        "attachment; filename=\"ProductSuppliers-${new Date().format("yyyyMMdd-hhmmss")}.xls\""
                documentService.generateExcel(response.outputStream, data)
                response.outputStream.flush()
                return;
            default:
                response.contentType = "text/csv"
                response.setHeader("Content-disposition",
                    "attachment; filename=\"ProductSuppliers-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
                render dataService.generateCsv(data)
                response.outputStream.flush()
                return;
        }
    }

    def updateAttributes(ProductSupplier productSupplier, Map params) {
        Map existingAtts = new HashMap()
        productSupplier.attributes.each() {
            existingAtts.put(it.attribute.id, it)
        }

        // Process attributes
        def availableAttributes =
                Attribute.findAll("from Attribute a where :entityTypeCodes in elements(a.entityTypeCodes)", [entityTypeCodes:"PRODUCT_SUPPLIER"])

        log.info "Available attributes: " + availableAttributes
        availableAttributes.each() {

            String value = params["productAttributes." + it.id + ".value"]
            if (value == "_other" || value == null || value == '') {
                value = params["productAttributes." + it.id + ".otherValue"]
            }

            log.info("Process attribute " + it.name + " = " + value + ", required = ${it.required}, active = ${it.active}")
            if (it.active && it.required && !value) {
                productSupplier.errors.rejectValue("attributes", "product.attribute.required",
                        [] as Object[],
                        "Product attribute ${it.name} is required")
                throw new ValidationException("Attribute required", productSupplier.errors)
            }

            ProductAttribute productAttribute = existingAtts.get(it.id)
            if (value) {
                if (!productAttribute) {
                    productAttribute = new ProductAttribute("attribute": it, value: value)
                    productAttribute.productSupplier = productSupplier
                    productSupplier.product.addToAttributes(productAttribute)
                    productSupplier.save()
                } else {
                    productAttribute.value = value
                    productAttribute.productSupplier = productSupplier
                    productAttribute.save()
                }
            } else {
                if (productAttribute?.attribute?.active) {
                    log.info("removing attribute ${productAttribute.attribute.name}")
                    productSupplier.product.removeFromAttributes(productAttribute)
                    productAttribute.delete()
                    productSupplier.product.save()
                }
            }
        }
    }
}
