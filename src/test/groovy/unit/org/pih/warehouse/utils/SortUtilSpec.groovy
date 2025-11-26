package unit.org.pih.warehouse.utils

import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.sort.SortParam
import org.pih.warehouse.sort.SortParamList
import org.pih.warehouse.sort.SortUtil

@Unroll
class SortUtilSpec extends Specification {

    void 'bindSortParams should return an empty list when given no sort params'() {
        expect:
        SortUtil.bindSortParams(null).sortParams == []
    }

    void 'bindSortParams should successfully bind a params string to a list of sort params'() {
        given:
        String sortParamsString = "-sortOrder,name"

        when:
        List<SortParam> sortParams = SortUtil.bindSortParams(sortParamsString).sortParams

        then:
        assert sortParams.size() == 2
        assert sortParams[0].fieldName == "sortOrder"
        assert !sortParams[0].ascending  // ie descending
        assert sortParams[1].fieldName == "name"
        assert sortParams[1].ascending
    }

    void 'sort should return the original list when given no sort params'() {
        given:
        List<TestObject> objectsUnsorted = [
                new TestObject(0, "Z", 1),
                new TestObject(1, "A", 1),
                new TestObject(2, "B", 0),
        ]

        when:
        List<TestObject> objectsSorted = SortUtil.sort(objectsUnsorted, null)

        then:
        assert objectsSorted == objectsUnsorted
    }


    void 'sort should error when given a param that is not a field of the object'() {
        given:
        List<TestObject> objectsUnsorted = [
                new TestObject(0, "A", 0),
        ]

        and:
        SortParamList sortParams = new SortParamList([
                new SortParam("INVALID!", true),
        ])

        when:
        SortUtil.sort(objectsUnsorted, sortParams)

        then:
        thrown(IllegalArgumentException)
    }

    void 'sort should successfully sort a list of objects by the given sort params'() {
        given:
        List<TestObject> objectsUnsorted = [
                new TestObject(0, "Z", 1),
                new TestObject(1, "A", 1),
                new TestObject(2, "B", 0),
        ]

        and:
        SortParamList sortParams = new SortParamList([
                new SortParam("sortOrder", false),  // descending (ie high to low)
                new SortParam("name", true),        // ascending (ie A-Z)
        ])

        when:
        List<TestObject> objectsSorted = SortUtil.sort(objectsUnsorted, sortParams)

        then:
        assert objectsSorted.size() == 3
        assert objectsSorted[0].id == 1  // sortOrder: 1, name: A
        assert objectsSorted[1].id == 0  // sortOrder: 1, name: Z
        assert objectsSorted[2].id == 2  // sortOrder: 0, name: B
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
