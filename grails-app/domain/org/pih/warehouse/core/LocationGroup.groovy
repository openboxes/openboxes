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


/**
 * Represents a logical grouping of locations (e.g. a site 
 * that has multiple facilities like depots, pharmacies, etc).
 */
class LocationGroup implements Serializable, Comparable<LocationGroup> {

    String id
    String name

    Address address

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
    }

    static hasMany = [locations: Location]


    static constraints = {
        name(nullable: true, maxSize: 255)
        address(nullable: true)
        dateCreated(display: false)
        lastUpdated(display: false)
    }

    String toString() { return "$name" }

    @Override
    int hashCode() {
        if (this.id != null) {
            return this.id.hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof LocationGroup) {
            LocationGroup that = (LocationGroup) o
            return this.id == that.id
        }
        return false
    }

    /**
     * Sort by name
     */
    int compareTo(LocationGroup obj) {
        return name <=> obj.name
    }


}
