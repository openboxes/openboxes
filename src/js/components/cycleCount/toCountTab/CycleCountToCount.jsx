import React, { useEffect, useRef } from 'react';

import PropTypes from 'prop-types';
import { RiCalculatorLine, RiDownload2Line, RiPrinterLine } from 'react-icons/ri';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import FileFormat from 'consts/fileFormat';
import useToCountTab from 'hooks/cycleCount/useToCountTab';
import useTablePagination from 'hooks/useTablePagination';
import useTranslate from 'hooks/useTranslate';
import Translate from 'utils/Translate';

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
    moveToCounting,
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
            onClick={moveToCounting}
            label="react.cycleCount.table.startCount.label"
            defaultLabel="Start count"
            variant="primary-outline"
            StartIcon={<RiCalculatorLine size={18} />}
            disabled={!selectedCheckboxesAmount}
          />
          <div className="btn-group">
            <Button
              isDropdown
              defaultLabel="Print count form"
              label="react.cycleCount.printCountForm.label"
              variant="primary-outline"
              StartIcon={<RiPrinterLine size={18} />}
              disabled={!selectedCheckboxesAmount}
            />
            <div className="dropdown-menu dropdown-menu-right nav-item padding-8" aria-labelledby="dropdownMenuButton">
              <a href="#" className="dropdown-item" onClick={() => printCountForm(FileFormat.PDF)} role="button">
                <Translate
                  id="react.cycleCount.printCountFormPdf.label"
                  defaultMessage="Print Count form PDF"
                />
              </a>
              <a href="#" className="dropdown-item" onClick={() => printCountForm(FileFormat.XLS)} role="button">
                <Translate
                  id="react.cycleCount.exportCountSheet.label"
                  defaultMessage="Export Count sheet"
                />
              </a>
            </div>
          </div>
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
