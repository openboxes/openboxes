/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.order.cart

class Cart implements Serializable {

    // Key: item id, value: item count
    def items = new ArrayList<CartItem>()


    boolean isEmpty() {
        items.isEmpty()
    }

    void addItem(CartItem cartItem) {
        items.add(cartItem)
    }

    void removeItem(CartItem cartItem) {
        items.remove(cartItem)
    }

    List<CartItem> getItems() {
        return items
    }
}