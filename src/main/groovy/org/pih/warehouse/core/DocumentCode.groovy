/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

enum DocumentCode {

    IMAGE,
    THUMBNAIL,
    PRODUCT_MANUAL,
    PURCHASE_ORDER_TEMPLATE,
    SHIPPING_DOCUMENT,
    SHIPPING_TEMPLATE,
    ZEBRA_TEMPLATE,
    EMAIL_TEMPLATE,
    DATA_EXPORT,
    INVOICE_TEMPLATE,
    REQUISITION_TEMPLATE,

    static list() {
        [
                THUMBNAIL,
                PRODUCT_MANUAL,
                PURCHASE_ORDER_TEMPLATE,
                SHIPPING_DOCUMENT,
                SHIPPING_TEMPLATE,
                ZEBRA_TEMPLATE,
                EMAIL_TEMPLATE,
                DATA_EXPORT,
                INVOICE_TEMPLATE,
                REQUISITION_TEMPLATE,
        ]
    }

    static templateList() {
        [
                PURCHASE_ORDER_TEMPLATE,
                SHIPPING_TEMPLATE,
                ZEBRA_TEMPLATE,
                EMAIL_TEMPLATE,
                INVOICE_TEMPLATE,
                REQUISITION_TEMPLATE,
        ]
    }

}

