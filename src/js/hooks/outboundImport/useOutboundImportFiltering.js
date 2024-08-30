import { useState } from 'react';

const useOutboundImportFiltering = () => {
  const [isFiltered, setIsFiltered] = useState(false);

  const getFilteredTableData = (itemsWithErrors, itemsInOrder) => (isFiltered
    ? (itemsWithErrors ?? [])
    : (itemsInOrder ?? []));

  const getTablePageSize = (itemsWithErrors, itemsInOrder) => (isFiltered
    ? itemsWithErrors?.length
    : itemsInOrder?.length);

  const toggleFiltering = () => {
    setIsFiltered((prev) => !prev);
  };

  return {
    setIsFiltered,
    isFiltered,
    getFilteredTableData,
    getTablePageSize,
    toggleFiltering,
  };
};

export default useOutboundImportFiltering;
