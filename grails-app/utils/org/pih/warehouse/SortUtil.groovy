package org.pih.warehouse

import org.apache.commons.lang.StringUtils

/**
 * Common utility methods relating to sorting entities.
 */
class SortUtil {

    private static final String PARAM_SEPARATOR = ','
    private static final String DESCENDING_CHAR = '-'

    /**
     * Parses a string matching our sort param pattern into a list of sort params. For use by APIs that accept
     * sort criteria, such as list endpoints. Typically these APIs will accept a "sort" query param like: ?sort=x,-y
     *
     * For example: given a string "x,-y", this signifies the entity should first be sorted ascending by "x",
     * then descending by "y".
     *
     * @param clazz The entity class type that will be sorted using the given parameters.
     * @param sortString A string of the form "x,-y,z"
     */
    static List<SortParam> bindSortParams(Class clazz, String sortString) {
        if (StringUtils.isBlank(sortString)) {
            return []
        }

        String[] params = sortString.split(PARAM_SEPARATOR)

        List<SortParam> sortParams = []
        for (String param in params) {
            // Params will either be "x" or "-x". The former means sort ascending, and the latter descending.
            String[] paramX = param.split(DESCENDING_CHAR, 2)
            boolean ascending = paramX.size() == 1

            // We currently only support sorting by fields that are direct properties of the class.
            String fieldName = paramX.size() > 1 ? paramX[1] : param
            if (!clazz.hasProperty(fieldName)) {
                throw new IllegalArgumentException("Invalid sort parameter. Class ${clazz} has no property ${fieldName}")
            }

            sortParams.add(new SortParam(fieldName, ascending))
        }
        return sortParams
    }

    /**
     * Sorts a given list of entities using the provided sort parameters.
     */
    static <T> List<T> sort(List<T> toSort, List<SortParam> sortParams) {
        return toSort.sort{ a, b -> compare(a, b, sortParams) }
    }

    /**
     * Compares two object instances using the given list of sorting conditions/fields. Will loop through all of the
     * given conditions until we find a difference between the given objects or we run out of conditions (in which
     * case the objects are deemed to be equivalent for the purposes of sorting).
     *
     * For each field/param:
     * If ascending == true,      returns 1 if a > b, 0 if a == b, and -1 if a < b
     * Else (ascending == false), returns 1 if a < b, 0 if a == b, and -1 if a > b (ie the inverse condition)
     */
    private static <T> Integer compare(T a, T b, List<SortParam> sortParams) {
        for (SortParam sortParam in sortParams) {
            String field = sortParam.fieldName
            Integer comparisonResult = sortParam.ascending ? a[field] <=> b[field] : b[field] <=> a[field]
            if (comparisonResult != 0) {
                return comparisonResult
            }
        }
        return 0
    }
}
