package org.pih.warehouse.core

import org.pih.warehouse.core.http.ResponseBodyFormattable

/**
 * Wraps a {@link DataGrouping}, adding an order field that suggests how the groupings should be sorted.
 */
class OrderedDataGrouping implements ResponseBodyFormattable {

    DataGrouping groups = [:]
    LinkedHashSet<String> order = []

    /**
     * A null/empty safe method of putting a new value to the group and ordering.
     */
    void put(String key, Object value) {
        groups.put(key, value)

        // We assume the desired sorting order is the order in which the entries are inserted.
        if (!order.contains(key)) {
            order.add(key)
        }
    }

    /**
     * A null/empty safe method of putting a new list of values to the group and ordering.
     */
    void put(String key, Collection<Object> values) {
        groups.put(key, values)

        // We assume the desired sorting order is the order in which the entries are inserted.
        if (!order.contains(key)) {
            order.add(key)
        }
    }

    /**
     * Merge the elements of the given grouping into our grouping.
     */
    void merge(OrderedDataGrouping otherGrouping) {
        groups.putAll(otherGrouping.groups)
        order.addAll(otherGrouping.order)
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                groups: groups,
                order: order,
        ]
    }
}
