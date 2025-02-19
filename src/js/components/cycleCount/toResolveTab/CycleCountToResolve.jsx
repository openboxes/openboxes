import React, { useEffect, useRef } from 'react';

import PropTypes from 'prop-types';
import { RiCalculatorLine, RiDownload2Line, RiPrinterLine } from 'react-icons/ri';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useToResolveTab from 'hooks/cycleCount/useToResolveTab';
import useTablePagination from 'hooks/useTablePagination';
import useTranslate from 'hooks/useTranslate';

const CycleCountToResolve = ({ filterParams }) => {
  const totalCount = useRef(0);
  const translate = useTranslate();

  const {
    paginationProps,
    offset,
    pageSize,
  } = useTablePagination({
    defaultPageSize: 5,
    totalCount: totalCount.current,
    filterParams,
  });

  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
    selectedCheckboxesAmount,
    moveToResolving,
    printResolveForm,
  } = useToResolveTab({
    filterParams,
    offset,
    pageSize,
  });

  // Use effect to avoid circular dependency
  useEffect(() => {
    totalCount.current = tableData.totalCount;
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
            onClick={moveToResolving}
            label="react.cycleCount.table.startResolution.label"
            defaultLabel="Start resolution"
            variant="primary-outline"
            StartIcon={<RiCalculatorLine size={18} />}
            disabled={!selectedCheckboxesAmount}
          />
          <Button
            onClick={printResolveForm}
            defaultLabel="Print resolve form"
            label="react.cycleCount.printResolveForm.label"
            variant="primary-outline"
            StartIcon={<RiPrinterLine size={18} />}
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

export default CycleCountToResolve;

CycleCountToResolve.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
};
