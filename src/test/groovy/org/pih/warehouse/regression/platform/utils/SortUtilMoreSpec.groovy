// Companion spec: extra features for upstream's SortUtilSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.utils

import org.pih.warehouse.utils.*
import org.hibernate.criterion.Order
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.sort.SortParam
import org.pih.warehouse.sort.SortParamList
import org.pih.warehouse.sort.SortUtil

@Unroll
class SortUtilMoreSpec extends Specification {

    void 'bindSortParams should return an empty list when given a blank string: "#sortString"'() {
        expect:
        SortUtil.bindSortParams(sortString).sortParams == []

        where:
        sortString << ['', '   ']
    }

    void 'sort should return an empty list as-is'() {
        given:
        List<TestObject> empty = []

        expect:
        assert SortUtil.sort(empty, new SortParamList([new SortParam("name", true)])).is(empty)
    }

    void 'sort should keep the original order of objects that are equal on every sort param'() {
        given:
        List<TestObject> objectsUnsorted = [
                new TestObject(0, "B", 1),
                new TestObject(1, "A", 1),
                new TestObject(2, "A", 1),
                new TestObject(3, "A", 0),
        ]

        when:
        List<TestObject> objectsSorted = SortUtil.sort(objectsUnsorted, new SortParamList([
                new SortParam("name", true),
                new SortParam("sortOrder", false),
        ]))

        then:
        assert objectsSorted.id == [1, 2, 3, 0]
    }

    /**
     * A simple object to use to test sorting behaviour.
     */
    private class TestObject {
        // To uniquely identify a row so that making asserts easier.
        int id

        // For use when sorting
        String name
        int sortOrder

        TestObject(int id, String name, int sortOrder) {
            this.id = id
            this.name = name
            this.sortOrder = sortOrder
        }
    }
}
