package org.pih.warehouse

class SortTagLib {

    def sort = { attrs ->

        // A closure that does the sorting can be passed as an attribute to the tag.
        // If it is not provided the default sort order is used instead
        def sorter = attrs.sorter ?: {item1, item2 -> item1 <=> item2}
        sorter = sorter as Comparator        

        // The collection to be sorted should be passed into the tag as a parameter
        Collections.sort(attrs.items, sorter)
    }
}

