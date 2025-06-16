package org.pih.warehouse.sort

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
     * @param sortString A string of the form "x,-y,z"
     */
    static SortParamList bindSortParams(String sortString) {
        SortParamList sortParams = new SortParamList()

        if (StringUtils.isBlank(sortString)) {
            return sortParams
        }

        String[] params = sortString.split(PARAM_SEPARATOR)

        for (String param in params) {
            // Params will either be "x" or "-x". The former means sort ascending, and the latter means descending.
            String[] paramX = param.split(DESCENDING_CHAR, 2)
            boolean ascending = paramX.size() == 1

            String fieldName = paramX.size() > 1 ? paramX[1] : param

            sortParams.add(new SortParam(fieldName, ascending))
        }
        return sortParams
    }

    /**
     * Sorts a given list of entities using the provided sort parameters.
     */
    static <T> List<T> sort(List<T> toSort, SortParamList sortParams) {
        if (!toSort || !sortParams) {
            return toSort
        }

        validateClassHasFields(toSort[0].class, sortParams.sortParams.fieldName)

        return toSort.sort{ a, b -> compare(a, b, sortParams) }
    }

    /**
     * We currently only support sorting by fields that are direct properties of the object, so error if given
     * a field that is not present on the object.
     */
    private static void validateClassHasFields(Class clazz, List<String> fieldNames) {
        for (String fieldName in fieldNames) {
            try {
                clazz.getDeclaredField(fieldName)
            }
            catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(
                        "Invalid sort parameter. Class ${clazz} has no property ${fieldName}", e)
            }
        }
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
    private static <T> Integer compare(T a, T b, SortParamList sortParams) {//<T> sortParams) {
        // Shove any nulls to the end of the list.
        if (a == null && b == null) {
            return 0
        }
        if (b == null) {
            return 1
        }
        if (a == null) {
            return -1
        }

        for (SortParam sortParam in sortParams.sortParams) {
            String field = sortParam.fieldName
            Integer comparisonResult = sortParam.ascending ? a[field] <=> b[field] : b[field] <=> a[field]
            if (comparisonResult != 0) {
                return comparisonResult
            }
        }
        return 0
    }
}
