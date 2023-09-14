package org.pih.warehouse

class PaginatedList<T> {
    @Delegate
    List<T> list;
    int totalCount;

    PaginatedList(List<T> list) {
        this.list = list
        this.totalCount = list.size()
    }

    PaginatedList(List<T> list, int totalCount) {
        this.list = list
        this.totalCount = totalCount
    }
}
