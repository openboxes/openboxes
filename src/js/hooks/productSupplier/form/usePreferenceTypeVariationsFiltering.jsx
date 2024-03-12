import { useState } from 'react';

import _ from 'lodash';

const usePreferenceTypeVariationsFiltering = ({ errors, updatedRows }) => {
  const [isFiltered, setIsFiltered] = useState(false);

  // react-table triggers filtering on changing value in filter fields (under column header), when
  // we want to filter after clicking button, we have to trigger changing filters value manually
  const triggerFiltering = () => [{ value: isFiltered }];

  // If row index is equal to index of invalid row and if the row is dirty
  // it means it's invalid
  const isRowInvalid = (rowIndex) => {
    const isRowDirty = _.some(Object.values(updatedRows?.[rowIndex] || {}));
    return errors?.[rowIndex] && isRowDirty;
  };

  const filterForInvalidRows = (filter, row) => isRowInvalid(row._index);

  const getFilterMethod = (filter, row) => (isFiltered ? filterForInvalidRows(filter, row) : true);

  const invalidRowCount = Object.keys(errors).filter(isRowInvalid).length;

  const getTablePageSize = (allRowsCount, invalidRowsCount, isFilteringApplied) => {
    const dataCount = isFilteringApplied ? invalidRowsCount : allRowsCount;
    return dataCount <= 4 ? 4 : dataCount;
  };

  return {
    isFiltered,
    setIsFiltered,
    invalidRowCount,
    tablePageSize: getTablePageSize(updatedRows?.length, invalidRowCount, isFiltered),
    getFilterMethod,
    triggerFiltering,
  };
};

export default usePreferenceTypeVariationsFiltering;
