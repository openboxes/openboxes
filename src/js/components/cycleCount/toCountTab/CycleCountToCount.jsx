import React, { useEffect, useRef } from 'react';

import PropTypes from 'prop-types';
import { RiCalculatorLine, RiDownload2Line, RiPrinterLine } from 'react-icons/ri';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useToCountTab from 'hooks/cycleCount/useToCountTab';
import useTablePagination from 'hooks/useTablePagination';
import useTranslate from 'hooks/useTranslate';

const CycleCountToCount = ({ filterParams }) => {
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
    startCount,
    printCountForm,
  } = useToCountTab({
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
            onClick={startCount}
            label="react.cycleCount.table.startCount.label"
            defaultLabel="Start count"
            variant="primary-outline"
            StartIcon={<RiCalculatorLine size={18} />}
            disabled={!selectedCheckboxesAmount}
          />
          <Button
            onClick={printCountForm}
            defaultLabel="Print count form"
            label="react.cycleCount.printCountForm.label"
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

export default CycleCountToCount;

CycleCountToCount.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
};
