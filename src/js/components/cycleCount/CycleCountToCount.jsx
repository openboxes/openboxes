import React from 'react';

import { RiCalculatorLine, RiDownload2Line } from 'react-icons/ri';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import useToCountTab from 'hooks/cycleCount/useToCountTab';
import useTranslate from 'hooks/useTranslate';

const CycleCountToCount = ({ filterParams }) => {
  const translate = useTranslate();
  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
    setOffset,
    setPageSize,
    selectedCheckboxesAmount,
  } = useToCountTab({
    filterParams,
  });

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
            label="react.cycleCount.table.startCount.label"
            defaultLabel="Start count"
            variant="primary-outline"
            StartIcon={<RiCalculatorLine size={18} />}
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
        setOffset={setOffset}
        setPageSize={setPageSize}
        totalCount={tableData.totalCount}
        filterParams={filterParams}
      />
    </div>
  );
};

export default CycleCountToCount;
