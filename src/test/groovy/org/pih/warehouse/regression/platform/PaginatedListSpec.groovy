package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import spock.lang.Specification

class PaginatedListSpec extends Specification {

    void 'the total count defaults to the size of the list'() {
        when:
        PaginatedList<String> paginatedList = new PaginatedList<>(['a', 'b', 'c'])

        then:
        assert paginatedList.totalCount == 3
    }

    void 'the total count can exceed the size of the current page'() {
        when:
        PaginatedList<String> paginatedList = new PaginatedList<>(['a', 'b'], 25)

        then:
        assert paginatedList.totalCount == 25
        assert paginatedList.size() == 2
    }

    void 'list operations are delegated to the wrapped page'() {
        given:
        List<String> page = ['a', 'b']
        PaginatedList<String> paginatedList = new PaginatedList<>(page, 10)

        when:
        paginatedList.add('c')

        then:
        assert page == ['a', 'b', 'c']
        assert paginatedList.get(2) == 'c'
        assert paginatedList.contains('b')
        assert paginatedList.collect { it.toUpperCase() } == ['A', 'B', 'C']
        // The total count is not tied to the list once it has been set.
        assert paginatedList.totalCount == 10
    }

    void 'an empty page has a total count of zero by default'() {
        expect:
        assert new PaginatedList<String>([]).totalCount == 0
        assert new PaginatedList<String>([]).isEmpty()
    }
}
