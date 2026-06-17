package org.pih.warehouse.core

import com.fasterxml.jackson.annotation.JsonProperty
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

import org.pih.warehouse.core.http.ResponseBodyFormattable

/**
 * Provides optional context around how some data might be grouped.
 *
 * This object is intentionally generic, and so is expected to support any grouping structure that is required.
 *
 * The primary use case for this class is providing a suggested display grouping to the client. In such scenarios
 * it is good practice to not nest complex DTOs inside of this group object. Instead, have the grouping option
 * contain id keys that can be used by the client to look up object details.
 *
 * For example, structure your DTO like the following:
 *
 * {
 *     productsById: {
 *         p1: { id: "p1", name: "Product 1", abcClass: "A", ... },
 *         p2: { id: "p2", name: "Product 2", abcClass: "A", ... },
 *         p3: { id: "p3", name: "Product 3", abcClass: "B", ... },
 *     }
 *     groupByAbcClass: {
 *         A: ["p1", "p2"],
 *         B: ["p3"],
 *     }
 * }
 *
 * To process the data, the client can loop each abcClass group in groupByAbcClass and use the product id to fetch
 * more details about each product from productsById.
 *
 * Using a grouping structure like this to normalize your data allows you to avoid complex nested DTO structures
 * and saves you from having data duplicated multiple times in one response.
 *
 * You can group by multiple fields by chaining multiple groups together.
 *
 * For example, if you want to group by pack level 1 then by pack level 2, you might have a DTO like:
 *
 * {
 *     shipmentItemsById: {
 *         si1: { id: "si1", packLevel1: "Crate1", packLevel2: "Box1", ... },
 *         si2: { id: "si2", packLevel1: "Crate1", packLevel2: "Box2", ... },
 *         si3: { id: "si3", packLevel1: "Crate2", packLevel2: "Box1", ... },
 *         si4: { id: "si4", packLevel1: "Crate2", packLevel2: "Box1", ... },
 *     }
 *     groupByPackLevel: {
 *         Crate1: {
 *             Box1: ["si1"],
 *             Box2: ["si2"],
 *         },
 *         Crate2: {
 *             Box1: ["si3", "si4"],
 *         },
 *     }
 * }
 *
 * See {@link OrderedDataGroup} if you also want your groups to be in a particular order.
 *
 * Note that while the class declaration says the value type is Object, in reality only a Collection (which will
 * be converted to a LinkedHashSet) or an additional DataGroup (for chaining groups) is allowed. Attempting to
 * put any other structure (such as a plain Map) will cause an error.
 */
class DataGroup implements ResponseBodyFormattable {

    private HashMap<String, Object> group = [:]

    /**
     * Puts a new object to the group.
     */
    Object put(String key, Object object) {
        switch (object) {
            case Collection:
                putCollection(key, object)
                break
            case DataGroup:
                putDataGroup(key, object)
                break
            case OrderedDataGroup:
                putOrderedDataGroup(key, object)
                break
            case null:
                break
            // Don't allow non-DataGroup maps. We want to standardize to a specific structure.
            case Map:
                throw new IllegalArgumentException("To put a Map into a data group, use another DataGroup.")
            // Groups are collections, so if given a single element, treat it like a singleton.
            default:
                putSingleton(key, object)
        }

        // We never stomp data (we always merge it) so there's nothing to return here.
        return null
    }

    /**
     * Merge the elements of the given group into our group.
     */
    void merge(DataGroup other) {
        other.group.each { put(it.key, it.value) }
    }

    private void putCollection(String key, Collection values) {
        if (!group.containsKey(key)) {
            // Use a LinkedHashSet to ensure that we preserve both order and uniqueness in the group
            group.put(key, values as LinkedHashSet)
            return
        }
        (group.get(key) as LinkedHashSet).addAll(values)
    }

    private void putDataGroup(String key, DataGroup childGroup) {
        if (!group.containsKey(key)) {
            group.put(key, childGroup)
            return
        }
        (group.get(key) as DataGroup).merge(childGroup)
    }

    private void putOrderedDataGroup(String key, OrderedDataGroup childGroup) {
        if (!group.containsKey(key)) {
            group.put(key, childGroup)
            return
        }
        (group.get(key) as OrderedDataGroup).merge(childGroup)
    }

    private void putSingleton(String key, Object object) {
        if (!group.containsKey(key)) {
            group.put(key, [object])
            return
        }
        try {
            (group.get(key) as LinkedHashSet).add(object)
        } catch (GroovyCastException e) {
            throw new IllegalArgumentException("Failed adding element to group. Expected a collection of values but " +
                    "got something else. Make sure you're not mixing and matching structures/types within a group", e)
        }
    }

    @Override
    Map<String, Object> asResponseBody() {
        // We don't want to add another nested key in the JSON for the group so simply render the group map itself.
        return group
    }
}
