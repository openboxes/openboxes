import React from 'react';

import PropTypes from 'prop-types';
import { RiAddCircleLine } from 'react-icons/all';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import HeaderLabel from 'components/cycleCount/HeaderLabel';
import HeaderSelect from 'components/cycleCount/HeaderSelect';
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
  recountedBy,
  setRecountedDate,
  validationErrors,
  shouldHaveRootCause,
  isStepEditable,
}) => {
  const {
    columns,
    defaultColumn,
    users,
  } = useResolveStepTable({
    cycleCountId: id,
    validationErrors,
    tableData,
    removeRow,
    isStepEditable,
    shouldHaveRootCause,
    productCode: product?.productCode,
    addEmptyRow,
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
          <HeaderLabel
            label={translate('react.cycleCount.dateCounted.label', 'Date Counted')}
            value={formatLocalizedDate(dateCounted, DateFormat.DD_MMM_YYYY)}
          />
          {/* TODO: Replace the name with value fetched from the API */}
          <HeaderLabel
            label={translate('react.cycleCount.countedBy.label', 'Counted by')}
            value="John Smith"
            className="pl-4"
          />
        </div>
        <div className="ml-5 pt-3 pl-4 d-flex align-items-center">
          {isStepEditable ? (
            <HeaderSelect
              label={translate('react.cycleCount.dateRecounted.label', 'Date recounted')}
            >
              <DateField
                className="date-counted-date-picker date-field-input"
                onChange={setRecountedDate}
                value={dateRecounted}
                hideErrorMessageWrapper
              />
            </HeaderSelect>
          ) : (
            <HeaderLabel
              label={translate('react.cycleCount.dateRecounted.label', 'Date recounted')}
              value={formatLocalizedDate(dateRecounted, DateFormat.DD_MMM_YYYY)}
            />
          )}
          {isStepEditable ? (
            <HeaderSelect
              label={translate('react.cycleCount.recountedBy.label', 'Recounted by')}
              className="ml-5 count-step-select-counted-by"
            >
              <SelectField
                placeholder="Select"
                options={users}
                onChange={assignRecountedBy(id)}
                hideErrorMessageWrapper
              />
            </HeaderSelect>
          ) : (
            <HeaderLabel
              label={translate('react.cycleCount.recountedBy.label', 'Recounted by')}
              value={recountedBy?.name}
              className="ml-4"
            />
          )}
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
        className={`ml-4 mb-3 d-flex ${isStepEditable ? '' : 'pt-3'}`}
      >
        {isStepEditable && (
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
        )}
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
  shouldHaveRootCause: PropTypes.func.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
};
