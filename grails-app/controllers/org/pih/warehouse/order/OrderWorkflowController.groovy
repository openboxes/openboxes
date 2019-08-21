/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.order

import org.pih.warehouse.core.Address
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class OrderWorkflowController {

    def index = { redirect(action: "order") }
    def orderFlow = {
        getProducts {
            action {
                def productList = []
                if (params.searchTerms) {
                    def categoryList = Category.findByNameLike("%" + params.searchTerms + "%")
                    log.info params.searchTerms
                    if (categoryList) {
                        productList = Product.findByNameLikeOrCategory("%" + params.searchTerms + "%", categoryList)
                    } else {
                        productList = Product.findByNameLike("%" + params.searchTerms + "%")
                    }
                }
                if (!productList) {
                    productList = Product.list(params)
                }

                [productList: productList]
            }
            on("success").to "showProducts"
            on(Exception).to "handleError"
        }
        showProducts {
            on("chooseProduct") {
                log.info "choose a product"
                if (!params.id) return error()
                def items = flow.cartItems
                if (!items) items = [] as HashSet
                items << Product.get(params.id)
                flow.cartItems = items
            }.to "showCart"
            on("searchProducts").to "getProducts"
            on("showCart").to "showCart"
        }
        showCart {
            on("checkout").to "enterPersonalDetails"
            on("continueShopping").to "showProducts"
        }
        enterPersonalDetails {
            on("submit") {
                def p = new Person(params)
                flow.person = p
                if (p.hasErrors() || !p.validate()) return error()
            }.to "enterShipping"
            on("return").to "showCart"
            on(Exception).to "handleError"
        }
        enterShipping {
            on("back").to "enterPersonalDetails"
            on("submit") {
                def a = new Address(params)
                flow.address = a
                if (a.hasErrors() || !a.validate()) return error()
            }.to "confirmPurchase"
        }
        confirmPurchase {
            on("back").to "enterShipping"
            on("confirm").to "processPurchaseOrder"
        }
        processPurchaseOrder {
            action {
                def a = flow.address
                def cartItems = flow.cartItems
                def order = new Order()
                order.orderNumber = new Random().nextInt(9999999)
                cartItems.each {
                    def orderItem = new OrderItem(product: it)
                    order.addToOrderItems(orderItem)
                }
                if (a.hasErrors() || !a.validate()) return error()
                else {
                    [order: order]
                    if (!order.save()) return error()
                }
            }
            on("error").to "confirmPurchase"
            on(Exception).to "confirmPurchase"
            on("success").to "displayInvoice"
        }
        displayInvoice()
        handleError()
    }
}
