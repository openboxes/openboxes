/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

class SortTagLib {

    def sort = { attrs ->

        // A closure that does the sorting can be passed as an attribute to the tag.
        // If it is not provided the default sort order is used instead
        def sorter = attrs.sorter ?: { item1, item2 -> item1 <=> item2 }
        sorter = sorter as Comparator

        // The collection to be sorted should be passed into the tag as a parameter
        Collections.sort(attrs.items, sorter)
    }
}

