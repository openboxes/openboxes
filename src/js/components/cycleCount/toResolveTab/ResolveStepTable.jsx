import React from 'react';

import PropTypes from 'prop-types';
import { RiAddCircleLine } from 'react-icons/all';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import { DateFormat } from 'consts/timeFormat';
import useResolveStepTable from 'hooks/cycleCount/useResolveStepTable';
import useTranslate from 'hooks/useTranslate';
import { formatDate } from 'utils/translation-utils';

import 'components/cycleCount/cycleCount.scss';

const ResolveStepTable = ({
  id,
  product,
  dateCounted,
  dateRecounted,
  tableData,
  tableMeta,
  addEmptyRow,
  removeRow,
  assignRecountedBy,
  setRecountedDate,
}) => {
  const {
    columns,
    defaultColumn,
    users,
  } = useResolveStepTable({
    cycleCountId: id,
    tableData,
    removeRow,
  });

  const translate = useTranslate();

  const {
    formatLocalizedDate,
  } = useSelector((state) => ({
    formatLocalizedDate: formatDate(state.localize),
  }));

  return (
    <div className="list-page-list-section">
      <p className="count-step-title pt-4 pl-4">
        {product?.productCode}
        {' '}
        {product?.name}
      </p>
      <div className="d-flex">
        <div className="pt-3 pl-4 d-flex align-items-center">
          <div className="d-flex date-counted-container mb-2">
            <p className="count-step-label count-step-label-date-counted mr-2 mt-2">
              {translate('react.cycleCount.dateCounted.label', 'Date Counted')}
              <span className="count-step-value ml-1">
                {formatLocalizedDate(dateCounted, DateFormat.DD_MMM_YYYY)}
              </span>
            </p>
          </div>
          <div className="d-flex count-step-select-counted-by ml-5 mt-2 align-items-center mb-2">
            <p className="count-step-label mr-2">
              {translate('react.cycleCount.countedBy.label', 'Counted by')}
              {/* TODO: Replace the name with value fetched from the API */}
              <span className="count-step-value ml-1">
                John Smith
              </span>
            </p>
          </div>
        </div>
        <div className="ml-5 pt-3 pl-4 d-flex align-items-center">
          <div className="d-flex date-counted-container">
            <span className="count-step-label count-step-label-date-counted mr-2 mt-2">
              {translate('react.cycleCount.dateRecounted.label', 'Date recounted')}
            </span>
            {' '}
            <DateField
              className="date-counted-date-picker date-field-input"
              onChange={setRecountedDate}
              value={dateRecounted}
            />
          </div>
          <div className="d-flex count-step-select-counted-by ml-5 align-items-center">
            <p className="count-step-label mr-2">
              {translate('react.cycleCount.recountedBy.label', 'Recounted by')}
            </p>
            <SelectField
              placeholder="Select"
              options={users}
              onChange={assignRecountedBy(product?.productCode)}
            />
          </div>
        </div>
      </div>
      <div className="mx-4 count-step-table resolve-step-table">
        <DataTable
          columns={columns}
          data={tableData}
          totalCount={tableData.length}
          defaultColumn={defaultColumn}
          meta={tableMeta}
          filterParams={{}}
          disablePagination
        />
      </div>
      <div
        className="ml-4 mb-3 d-flex"
      >
        <Tooltip
          className="d-flex align-items-center"
          html={(
            <span className="p-1">
              {translate('react.cycleCount.addNewRecord.tooltip', 'Use this button to change lot number or bin location.')}
            </span>
          )}
        >
          <Button
            onClick={() => addEmptyRow(product?.productCode, id)}
            label="react.cycleCount.addNewRecord.label"
            defaultLabel="Add new record"
            variant="transparent"
            StartIcon={<RiAddCircleLine size={18} />}
          />
        </Tooltip>
      </div>
    </div>
  );
};

export default ResolveStepTable;

ResolveStepTable.propTypes = {
  id: PropTypes.string.isRequired,
  product: PropTypes.shape({
    productCode: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  dateRecounted: PropTypes.string.isRequired,
  dateCounted: PropTypes.string.isRequired,
  tableData: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
  tableMeta: PropTypes.shape({
    updateData: PropTypes.func.isRequired,
  }).isRequired,
  addEmptyRow: PropTypes.func.isRequired,
  removeRow: PropTypes.func.isRequired,
  assignRecountedBy: PropTypes.func.isRequired,
  validationErrors: PropTypes.shape({}).isRequired,
  setRecountedDate: PropTypes.func.isRequired,
};
