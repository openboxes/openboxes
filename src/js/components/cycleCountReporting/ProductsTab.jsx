import React, { useEffect } from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import useProductsTab from 'hooks/cycleCount/useProductsTab';

const ProductsTab = ({
  filterParams,
  tablePaginationProps,
  shouldFetch,
  setShouldFetch,
  filtersInitialized,
  defaultFilterValues,
}) => {
  const {
    paginationProps,
    setTotalCount,
    pageSize,
    offset,
    serializedParams,
  } = tablePaginationProps;
  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
  } = useProductsTab({
    paginationProps,
    filterParams,
    pageSize,
    offset,
    shouldFetch,
    setShouldFetch,
    serializedParams,
    filtersInitialized,
    defaultFilterValues,
  });

  useEffect(() => {
    setTotalCount(tableData.totalCount);
  }, [tableData.totalCount]);

  return (
    <div>
      <DataTable
        columns={columns}
        data={tableData.data}
        emptyTableMessage={emptyTableMessage}
        loading={loading}
        totalCount={tableData.totalCount}
        filterParams={filterParams}
        paginationProps={paginationProps}
        tableWithPinnedColumns
      />
    </div>
  );
};

export default ProductsTab;

ProductsTab.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  tablePaginationProps: PropTypes.shape({
    paginationProps: PropTypes.shape({}).isRequired,
    offset: PropTypes.number.isRequired,
    pageSize: PropTypes.number.isRequired,
    setTotalCount: PropTypes.func.isRequired,
    serializedParams: PropTypes.number.isRequired,
  }).isRequired,
  shouldFetch: PropTypes.bool.isRequired,
  setShouldFetch: PropTypes.func.isRequired,
};
