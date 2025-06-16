package org.pih.warehouse.databinding

import org.springframework.stereotype.Component

import org.pih.warehouse.sort.SortParamList
import org.pih.warehouse.sort.SortUtil

@Component
class SortParamListValueConverter extends StringValueConverter<SortParamList> {

    @Override
    SortParamList convertString(String value) {
        return SortUtil.bindSortParams(value)
    }
}
