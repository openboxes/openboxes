package org.pih.warehouse.sort

/**
 * A POJO for holding a sorting condition on an entity.
 */
class SortParam {

    /**
     * The name of the field for the entity to be sorted by.
     */
    String fieldName

    /**
     * True if we should sort the field low to high (ie A to Z, or lower number to higher), false otherwise.
     */
    boolean ascending

    SortParam(String fieldName, boolean ascending) {
        this.fieldName = fieldName
        this.ascending = ascending
    }
}