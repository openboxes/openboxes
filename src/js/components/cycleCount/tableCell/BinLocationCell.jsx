import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import { RiErrorWarningLine } from 'react-icons/ri';
import { useDispatch, useSelector } from 'react-redux';
import { getBinLocations, makeGetCycleCountItem } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import { NEW_ROW } from 'consts/cycleCount';
import cycleCountColumn from 'consts/cycleCountColumn';
import useCellValidation from 'hooks/cycleCount/useCellValidation';
import useTranslate from 'hooks/useTranslate';
import { getBinLocationToDisplay, groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';
import CustomTooltip from 'wrappers/CustomTooltip';

const BinLocationCell = ({
  id,
  index,
  cycleCountId,
  showBinLocation,
  isStepEditable,
}) => {
  if (!showBinLocation) {
    return null;
  }

  const translate = useTranslate();

  const getCycleCountItem = useMemo(() => makeGetCycleCountItem(), []);
  const item = useSelector(
    (state) => getCycleCountItem(state, cycleCountId, id),
  );
  const { binLocation: value } = item;

  const tooltipLabel = getBinLocationToDisplay(value)
    || translate('react.cycleCount.table.binLocation.label', 'Bin Location');

  if (!isStepEditable) {
    return (
      <TableCell
        className="static-cell-count-step d-flex align-items-center"
        tooltipLabel={tooltipLabel}
        customTooltip
      >
        {getBinLocationToDisplay(value)}
      </TableCell>
    );
  }

  const binLocations = useSelector(getBinLocations);

  const {
    onBlurValidationHandler,
    error,
    showError,
  } = useCellValidation({
    initialValue: value?.name,
    cycleCountId,
    index,
    fieldName: cycleCountColumn.BIN_LOCATION,
  });

  const dispatch = useDispatch();

  const onChange = (selected) => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: cycleCountColumn.BIN_LOCATION,
        value: { id: selected?.id, name: selected?.name },
      }),
    );
    onBlurValidationHandler();
  };

  const selectOptions = useMemo(() =>
    groupBinLocationsByZone(binLocations, translate), []);

  const isDisabled = !id?.includes(NEW_ROW);

  const selectedValue = value
    ? { ...value, name: getBinLocationToDisplay(value) }
    : undefined;

  return (
    <TableCell
      className="rt-td rt-td-count-step pb-0"
      tooltipClassname="w-75"
      tooltipLabel={tooltipLabel}
      customTooltip
    >
      <SelectField
        value={selectedValue}
        hasErrors={showError}
        labelKey="name"
        options={selectOptions}
        onChange={onChange}
        disabled={isDisabled}
        className={`m-1 ${showError ? 'input-has-error' : ''}`}
        hideErrorMessageWrapper
      />
      {showError && (
        <CustomTooltip
          content={error}
          className="tooltip-icon tooltip-icon--error"
          icon={RiErrorWarningLine}
        />
      )}
    </TableCell>
  );
};

export default BinLocationCell;

BinLocationCell.propTypes = {
  id: PropTypes.string.isRequired,
  index: PropTypes.string.isRequired,
  cycleCountId: PropTypes.string.isRequired,
  showBinLocation: PropTypes.bool.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
};
