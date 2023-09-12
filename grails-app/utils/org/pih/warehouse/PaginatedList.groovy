package org.pih.warehouse

class PaginatedList {
    @Delegate
    List list;
    int totalCount;

    PaginatedList(List list) {
        this.list = list
        this.totalCount = list.size()
    }

    PaginatedList(List list, int totalCount) {
        this.list = list
        this.totalCount = totalCount
    }
}
