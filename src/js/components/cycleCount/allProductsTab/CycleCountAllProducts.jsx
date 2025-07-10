import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { RiCalculatorLine, RiDownload2Line } from 'react-icons/ri';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useAllProductsTab from 'hooks/cycleCount/useAllProductsTab';
import useTranslate from 'hooks/useTranslate';

const CycleCountAllProducts = ({
  filterParams,
  switchTab,
  resetForm,
  setToCountCheckedCheckboxes,
  tablePaginationProps,
}) => {
  const {
    paginationProps,
    offset,
    pageSize,
    setTotalCount,
    serializedParams,
  } = tablePaginationProps;

  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
    selectedCheckboxesAmount,
    countSelected,
  } = useAllProductsTab({
    filterParams,
    switchTab,
    offset,
    pageSize,
    resetForm,
    setToCountCheckedCheckboxes,
    serializedParams,
  });

  const translate = useTranslate();

  useEffect(() => {
    setTotalCount(tableData.totalCount);
  }, [tableData.totalCount]);

  return (
    <div>
      <div className="d-flex justify-content-sm-between align-items-center">
        <span className="selected-rows-indicator pl-4">
          {selectedCheckboxesAmount}
          {' '}
          {translate('react.default.selected.label', 'selected')}
        </span>
        <div className="d-flex m-2 gap-8">
          <Button
            label="react.cycleCount.table.markAsToCount.label"
            defaultLabel="Mark as To Count"
            variant="primary-outline"
            StartIcon={<RiCalculatorLine size={18} />}
            onClick={countSelected}
            disabled={!selectedCheckboxesAmount}
          />
          <Button
            onClick={exportTableData}
            defaultLabel="Export"
            label="react.default.button.export.label"
            variant="secondary"
            EndIcon={<RiDownload2Line />}
          />
        </div>
      </div>
      <DataTable
        columns={columns}
        data={tableData.data}
        emptyTableMessage={emptyTableMessage}
        loading={loading}
        totalCount={tableData.totalCount}
        filterParams={filterParams}
        paginationProps={paginationProps}
      />
    </div>
  );
};

export default CycleCountAllProducts;

CycleCountAllProducts.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  switchTab: PropTypes.func.isRequired,
  resetForm: PropTypes.func.isRequired,
  setToCountCheckedCheckboxes: PropTypes.func.isRequired,
  tablePaginationProps: PropTypes.shape({
    paginationProps: PropTypes.shape({}).isRequired,
    offset: PropTypes.number.isRequired,
    pageSize: PropTypes.number.isRequired,
    setTotalCount: PropTypes.func.isRequired,
    serializedParams: PropTypes.number.isRequired,
  }).isRequired,
};
