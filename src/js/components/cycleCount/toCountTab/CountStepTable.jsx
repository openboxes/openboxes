import React from 'react';

import PropTypes from 'prop-types';
import { RiAddCircleLine } from 'react-icons/all';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import AssignCycleCountModal from 'components/cycleCount/AssignCycleCountModal';
import HeaderLabel from 'components/cycleCount/HeaderLabel';
import HeaderSelect from 'components/cycleCount/HeaderSelect';
import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import { DateFormat } from 'consts/timeFormat';
import useCountStepTable from 'hooks/cycleCount/useCountStepTable';
import useTranslate from 'hooks/useTranslate';
import { formatDate } from 'utils/translation-utils';
import CustomTooltip from 'wrappers/CustomTooltip';

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
  isStepEditable,
  countedBy,
  defaultCountedBy,
  refreshFocusCounter,
  triggerValidation,
  isFormDisabled,
  isAssignCountModalOpen,
  closeAssignCountModal,
  assignCountModalData,
}) => {
  const translate = useTranslate();
  const localize = useSelector((state) => state.localize);
  const formatLocalizedDate = formatDate(localize);
  const {
    columns,
    defaultColumn,
    users,
  } = useCountStepTable({
    cycleCountId: id,
    productId: product?.id,
    tableData,
    validationErrors,
    removeRow,
    isStepEditable,
    formatLocalizedDate,
    addEmptyRow,
    triggerValidation,
    refreshFocusCounter,
    isFormDisabled,
  });

  // Default counted by needs to be stored in order to set the default select value correctly
  const defaultCountedByMeta = defaultCountedBy ? {
    id: defaultCountedBy.id,
    value: defaultCountedBy.id,
    label: defaultCountedBy.label ?? `${defaultCountedBy.firstName} ${defaultCountedBy.lastName}`,
    name: `${defaultCountedBy.firstName} ${defaultCountedBy.lastName}`,
  } : undefined;

  const countedByMeta = countedBy ? {
    id: countedBy.id,
    value: countedBy.id,
    label: countedBy.label ?? `${countedBy.firstName} ${countedBy.lastName}`,
    name: `${countedBy.firstName} ${countedBy.lastName}`,
  } : undefined;

  return (
    <>
      {isAssignCountModalOpen && (
      <AssignCycleCountModal
        isOpen={isAssignCountModalOpen}
        closeModal={closeAssignCountModal}
        selectedCycleCounts={assignCountModalData}
        defaultTitleLabel="Assign products to recount"
        titleLabel="react.cycleCount.modal.assignProductsToRecount.title.label"
        assignDataDirectly
        isRecount
        showSkipButton
      />
      )}
      <div className="list-page-list-section">
        <p className="count-step-title pt-4 pl-4">
          {product?.productCode}
          {' '}
          {product?.name}
        </p>
        <div className="pt-3 pl-4 d-flex align-items-center">
          {isStepEditable ? (
            <HeaderSelect
              label={translate('react.cycleCount.dateCounted.label', 'Date counted')}
            >
              <DateField
                className="date-counted-date-picker date-field-input"
                onChangeRaw={setCountedDate}
                value={dateCounted}
                clearable={false}
                customDateFormat={DateFormat.DD_MMM_YYYY}
                disabled={isFormDisabled}
              />
            </HeaderSelect>
          ) : (
            <HeaderLabel
              label={translate('react.cycleCount.dateCounted.label', 'Date counted')}
              value={formatLocalizedDate(dateCounted, DateFormat.DD_MMM_YYYY)}
            />
          )}
          {isStepEditable ? (
            <HeaderSelect
              label={translate('react.cycleCount.countedBy.label', 'Counted by')}
              className="ml-4"
            >
              <CustomTooltip
                content={countedByMeta?.label || translate('react.cycleCount.countedBy.label', 'Counted By')}
              >
                <div className="position-relative">
                  <SelectField
                    placeholder="Select"
                    options={users}
                    onChange={assignCountedBy(id)}
                    className="min-width-250"
                    defaultValue={defaultCountedByMeta}
                    disabled={isFormDisabled}
                  />
                </div>
              </CustomTooltip>
            </HeaderSelect>
          ) : (
            <HeaderLabel
              label={translate('react.cycleCount.countedBy.label', 'Counted by')}
              value={countedByMeta?.label}
              className="ml-4"
            />
          )}
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
        {isStepEditable && (
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
                onClick={() => addEmptyRow(product?.id, id)}
                label="react.cycleCount.addNewRecord.label"
                defaultLabel="Add new record"
                variant="transparent"
                StartIcon={<RiAddCircleLine size={18} />}
                disabled={isFormDisabled}
              />
            </Tooltip>
          </div>
        )}
      </div>
    </>
  );
};

export default CountStepTable;

CountStepTable.propTypes = {
  id: PropTypes.string.isRequired,
  product: PropTypes.shape({
    productCode: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  dateCounted: PropTypes.string.isRequired,
  tableData: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
  tableMeta: PropTypes.shape({
    updateData: PropTypes.func.isRequired,
  }).isRequired,
  addEmptyRow: PropTypes.func.isRequired,
  removeRow: PropTypes.func.isRequired,
  assignCountedBy: PropTypes.func.isRequired,
  validationErrors: PropTypes.shape({}).isRequired,
  setCountedDate: PropTypes.func.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
  countedBy: PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  defaultCountedBy: PropTypes.shape({}).isRequired,
  refreshFocusCounter: PropTypes.number.isRequired,
  triggerValidation: PropTypes.func.isRequired,
  isFormDisabled: PropTypes.bool.isRequired,
  isAssignCountModalOpen: PropTypes.bool.isRequired,
  closeAssignCountModal: PropTypes.func.isRequired,
  assignCountModalData: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
};
