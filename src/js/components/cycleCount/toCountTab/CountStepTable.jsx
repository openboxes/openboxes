import React from 'react';

import { RiAddCircleLine } from 'react-icons/all';
import { Tooltip } from 'react-tippy';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import useCountStepTable from 'hooks/cycleCount/useCountStepTable';
import useTranslate from 'hooks/useTranslate';

import 'components/cycleCount/cycleCount.scss';

const CountStepTable = ({
  id,
  product,
  dateCounted,
  tableData,
  tableMeta,
  addEmptyRow,
  removeRow,
  assignCountedBy,
  validationErrors,
  setCountedDate,
}) => {
  const {
    columns,
    defaultColumn,
    recipients,
  } = useCountStepTable({
    cycleCountId: id,
    tableData,
    validationErrors,
    removeRow,
  });

  const translate = useTranslate();

  return (
    <div className="list-page-list-section">
      <p className="count-step-title pt-4 pl-4">
        {product?.productCode}
        {' '}
        {product?.name}
      </p>
      <div className="pt-3 pl-4 d-flex align-items-center">
        <div className="d-flex date-counted-container">
          <span className="count-step-label count-step-label-date-counted mr-2 mt-2">
            {translate('react.cycleCount.dateCounted.label', 'Date counted')}
          </span>
          {' '}
          <DateField
            className="date-counted-date-picker date-field-input"
            onChange={setCountedDate}
            value={dateCounted}

          />
        </div>
        <div className="d-flex count-step-select-counted-by ml-5 align-items-center">
          <p className="count-step-label mr-2">
            {translate('react.cycleCount.countedBy.label', 'Counted by')}
          </p>
          <SelectField
            placeholder="Select"
            options={recipients}
            onChange={assignCountedBy(product?.productCode)}
          />
        </div>
      </div>
      <div className="mx-4 count-step-table">
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

export default CountStepTable;
