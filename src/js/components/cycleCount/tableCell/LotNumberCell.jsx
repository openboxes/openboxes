import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import { RiErrorWarningLine } from 'react-icons/ri';
import { useDispatch, useSelector } from 'react-redux';
import {
  getLotNumbersByProductId,
  makeGetCycleCountItem,
  makeGetCycleCountProduct,
} from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import { NEW_ROW } from 'consts/cycleCount';
import useCellValidation from 'hooks/cycleCount/useCellValidation';
import useTranslate from 'hooks/useTranslate';
import CustomTooltip from 'wrappers/CustomTooltip';

const LotNumberCell = ({
  id,
  index,
  cycleCountId,
  setDisabledExpirationDateFields,
  isStepEditable,
}) => {
  const dispatch = useDispatch();

  const translate = useTranslate();

  const getCycleCountItem = useMemo(() => makeGetCycleCountItem(), []);
  const item = useSelector(
    (state) => getCycleCountItem(state, cycleCountId, id),
  );
  const { inventoryItem: { lotNumber: initialValue } } = item;

  const value = initialValue
    ? {
      label: initialValue,
      value: initialValue,
      id: initialValue,
      name: initialValue,
    }
    : undefined;

  const tooltipLabel = value?.label
    || translate('react.cycleCount.table.lotNumber.label', 'Serial / Lot Number');

  if (!isStepEditable) {
    return (
      <TableCell
        className="static-cell-count-step d-flex align-items-center"
        tooltipLabel={tooltipLabel}
        customTooltip
      >
        {value?.label}
      </TableCell>
    );
  }

  const getCycleCountProduct = useMemo(makeGetCycleCountProduct, []);

  const {
    onBlurValidationHandler,
    error,
    shouldShowError,
  } = useCellValidation({
    initialValue,
    cycleCountId,
    index,
    fieldName: 'inventoryItem.lotNumber',
  });

  const cycleCountProduct = useSelector(
    (state) => getCycleCountProduct(state, cycleCountId),
  );

  const lotNumbersWithExpiration = useSelector((state) =>
    getLotNumbersByProductId(state, cycleCountProduct?.id));

  const onChange = (selected) => {
    const selectedLot = selected?.value;
    const existingLot = lotNumbersWithExpiration.find((l) => l.lotNumber === selectedLot);
    const lotExists = Boolean(existingLot);

    if (lotExists) {
      setDisabledExpirationDateFields((prev) => ({ ...prev, [id]: existingLot?.expirationDate }));
    }

    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: 'inventoryItem.lotNumber',
        value: selected?.value,
      }),
    );
    onBlurValidationHandler();
  };

  const selectOptions = useMemo(() => lotNumbersWithExpiration.map((lotWithExp) => ({
    id: lotWithExp.lotNumber,
    name: lotWithExp.lotNumber,
    label: lotWithExp.lotNumber,
    value: lotWithExp.lotNumber,
  })), [lotNumbersWithExpiration]);

  const isDisabled = !id?.includes(NEW_ROW);

  const placeholder = isDisabled
    && translate('react.cycleCount.emptyLotNumber.label', 'NO LOT');

  return (
    <TableCell
      className="rt-td rt-td-count-step pb-0"
      tooltipClassname="w-75"
      tooltipLabel={tooltipLabel}
      customTooltip
    >
      <SelectField
        value={value}
        hasErrors={shouldShowError}
        options={selectOptions}
        disabled={isDisabled}
        onChange={onChange}
        placeholder={placeholder}
        className={`m-1 ${shouldShowError ? 'input-has-error' : ''}`}
        hideErrorMessageWrapper
        creatable
      />
      {shouldShowError && (
        <CustomTooltip
          content={error}
          className="tooltip-icon tooltip-icon--error"
          icon={RiErrorWarningLine}
        />
      )}
    </TableCell>
  );
};

export default LotNumberCell;

LotNumberCell.propTypes = {
  id: PropTypes.string.isRequired,
  index: PropTypes.number.isRequired,
  cycleCountId: PropTypes.string.isRequired,
  setDisabledExpirationDateFields: PropTypes.func.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
};
