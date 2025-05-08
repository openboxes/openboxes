import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { RiCalculatorLine, RiDownload2Line, RiPrinterLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';

import CycleCountDraftInfoBar from 'components/cycleCount/CycleCountDraftInfoBar';
import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import FileFormat from 'consts/fileFormat';
import useToResolveTab from 'hooks/cycleCount/useToResolveTab';
import useTranslate from 'hooks/useTranslate';
import Translate from 'utils/Translate';

const CycleCountToResolve = ({
  filterParams,
  tablePaginationProps,
}) => {
  const translate = useTranslate();
  const {
    paginationProps,
    offset,
    pageSize,
    setTotalCount,
  } = tablePaginationProps;
  const {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
    selectedCheckboxesAmount,
    moveToResolving,
    printResolveForm,
    openCancelCountsModal,
  } = useToResolveTab({
    filterParams,
    offset,
    pageSize,
  });
  const cycleCountIds = useSelector((state) => (state.cycleCount.cycleCounts));

  useEffect(() => {
    setTotalCount(tableData.totalCount);
  }, [tableData.totalCount]);

  return (
    <div>
      {cycleCountIds.length !== 0 && <CycleCountDraftInfoBar tab="resolve" />}
      <div className="d-flex justify-content-sm-between align-items-center">
        <span className="selected-rows-indicator pl-4">
          {selectedCheckboxesAmount}
          {' '}
          {translate('react.default.selected.label', 'selected')}
        </span>
        <div className="d-flex m-2 gap-8">
          <Button
            onClick={openCancelCountsModal}
            label="react.cycleCount.table.cancelCount.label"
            defaultLabel="Cancel count"
            variant="danger-outline"
            disabled={!selectedCheckboxesAmount}
          />
          <Button
            onClick={moveToResolving}
            label="react.cycleCount.table.startResolution.label"
            defaultLabel="Start resolution"
            variant="primary-outline"
            StartIcon={<RiCalculatorLine size={18} />}
            disabled={!selectedCheckboxesAmount}
          />
          <div className="btn-group">
            <Button
              isDropdown
              defaultLabel="Print recount form"
              label="react.cycleCount.printRecountForm.label"
              variant="primary-outline"
              StartIcon={<RiPrinterLine size={18} />}
              disabled={!selectedCheckboxesAmount}
            />
            <div className="dropdown-menu dropdown-menu-right nav-item padding-8" aria-labelledby="dropdownMenuButton">
              <a href="#" className="dropdown-item" onClick={() => printResolveForm(FileFormat.PDF)} role="button">
                <Translate
                  id="react.cycleCount.printRecountFormPdf.label"
                  defaultMessage="Print Recount Form PDF"
                />
              </a>
              <a href="#" className="dropdown-item" onClick={() => printResolveForm(FileFormat.XLS)} role="button">
                <Translate
                  id="react.cycleCount.exportRecountSheet.label"
                  defaultMessage="Export Recount Sheet"
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

export default CycleCountToResolve;

CycleCountToResolve.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  tablePaginationProps: PropTypes.shape({
    paginationProps: PropTypes.shape({}).isRequired,
    offset: PropTypes.number.isRequired,
    pageSize: PropTypes.number.isRequired,
    setTotalCount: PropTypes.func.isRequired,
  }).isRequired,
};
