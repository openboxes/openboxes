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

class Category implements Comparable, Serializable {

    String id
    String name
    String description
    Integer sortOrder = 0
    Category parentCategory
    Date dateCreated
    Date lastUpdated
    Boolean deleted = false
    Boolean isRoot = false

    static hasMany = [categories: Category]
    static mappedBy = [categories: "parentCategory"]
    static belongsTo = [parentCategory: Category]
    static transients = ["parents", "children", "deleted", "products"]
    static mapping = {
        id generator: 'uuid'
        sort name: "desc"
        categories sort: "name", cascade: "all-delete-orphan"
        cache true
    }

    static constraints = {
        name(nullable: false, maxSize: 255)
        description(nullable: true, maxSize: 255)
        sortOrder(nullable: true)
        isRoot(nullable: true)
        deleted(nullable: true)
        // parent category can't be the category itself or any of its children
        parentCategory(nullable: true,
                validator: { value, obj ->
                    value != obj && !(obj.getChildren().find {
                        it == value
                    })
                })
    }

    String toString() { return "$name" }

    /**
     * Sort by sort order, name
     */
    int compareTo(obj) {
        def compare = sortOrder <=> obj.sortOrder
        if (compare == 0) {
            compare = this.getHierarchyAsString(">") <=> obj.getHierarchyAsString(">")
        }
        return compare
    }

    /**
     * @param separator
     * @return a string representation of the category hierarchy
     */
    String getHierarchyAsString(String separator) {
        String hierarchyAsString = ""
        def nodes = getParents()
        nodes.each {
            hierarchyAsString += it.name + separator
        }
        hierarchyAsString += this.name
        return hierarchyAsString
    }

    /**
     * @return true if the category is marked as root or if it's the same object returned by the getRootCategory() method.
     */
    Boolean isRootCategory() {
        return isRoot || this.equals(Category.getRootCategory())
    }

    /**
     * @return the root category
     */
    static Category getRootCategory() {
        def rootCategory = Category.findByIsRoot(true)
        if (!rootCategory)
            rootCategory = Category.findByParentCategoryIsNull()
        return rootCategory
    }

    /**
     * @return
     */
    def getParents() {
        def parents = []
        getParents(this, parents)
        return (parents ? parents.reverse() : [])
    }


    List getParents(Category node, List parents) {
        if (!node?.parentCategory) {
            return parents
        } else {
            parents << node.parentCategory
            return getParents(node.parentCategory, parents)
        }
    }

    /**
     *
     * @return
     */
    def getChildren() {
        return categories ? categories*.children.flatten() + categories : []
    }

    // FIXME We should move this method out of Category.  It's used primarily in the _selectOptions.gsp,
    // but there's probably a better solution.
    def getProducts() {
        try {
            return Product.findAllByActiveAndCategory(true, this, [cache: true])
        } catch (Exception e) {
            return null
        }

    }

    @Override
    int hashCode() {
        if (this.id != null) {
            return this.id.hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof Category) {
            Category that = (Category) o
            return this.id == that.id
        }
        return false
    }

    Map toJson() {
        [
                id         : id,
                description: description,
                name       : name,
                version    : version,
                dateCreated: dateCreated?.format("dd/MMM/yyyy hh:mm a"),
                lastUpdated: lastUpdated?.format("dd/MMM/yyyy hh:mm a"),
                sortOrder  : sortOrder,
                deleted    : deleted,
                isRoot     : isRoot,
                categories : categories?.collect { it.toJson() }
        ]
    }

    static PROPERTIES = [
            "id"              : "id",
            "name"            : "name",
            "parentCategoryId": "parentCategoryId",
    ]
}
