package org.pih.warehouse.sort

/**
 * A POJO for holding a list of sorting conditions on an entity.
 */
class SortParamList {

    /**
     * A list of parameters to use when sorting a list of objects. Order matters since it determines the priority
     * order of the fields that the objects will be sorted by.
     */
    List<SortParam> sortParams = []

    SortParamList() {

    }

    SortParamList(List<SortParam> sortParams) {
        this.sortParams = sortParams
    }

    boolean add(SortParam sortParam) {
        return sortParams.add(sortParam)
    }

    SortParam get(int index) {
        return sortParams[index]
    }
}