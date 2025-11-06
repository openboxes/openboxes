import React, { useMemo, useState } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { makeGetCycleCountProduct, makeGetLotNumbersByProductId } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import useTranslate from 'hooks/useTranslate';

const LotNumberCell = ({
  id,
  cycleCountId,
  initialValue,
  setDisabledExpirationDateFields,
  isStepEditable,
}) => {
  const dispatch = useDispatch();

  const translate = useTranslate();

  const [value, setValue] = useState(initialValue ? {
    label: initialValue,
    value: initialValue,
    id: initialValue,
    name: initialValue,
  } : undefined);

  const tooltipLabel = value
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

  const cycleCountProduct = useSelector(
    (state) => getCycleCountProduct(state, id),
  );

  const getLotNumbersByProductId = useMemo(() => makeGetLotNumbersByProductId(), []);

  const lotNumbersWithExpiration = useSelector(
    (state) => getLotNumbersByProductId(state, cycleCountProduct?.id),
  );

  const onChange = (selected) => {
    const selectedLot = selected?.value;
    const existingLot = lotNumbersWithExpiration.find((l) => l.lotNumber === selectedLot);
    const lotExists = Boolean(existingLot);

    if (lotExists) {
      setDisabledExpirationDateFields((prev) => ({ ...prev, [id]: existingLot?.expirationDate }));
    }
    setValue(selected);
  };

  const onBlur = () => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: 'inventoryItem.lotNumber',
        value: value?.value,
      }),
    );
  };

  const selectOptions = useMemo(() => lotNumbersWithExpiration.map((item) => ({
    id: item.lotNumber,
    name: item.lotNumber,
    label: item.lotNumber,
    value: item.lotNumber,
  })), []);

  const isDisabled = useMemo(() =>
    !id.includes('newRow'), []);

  const placeholder = isDisabled
    && translate('react.cycleCount.emptyLotNumber.label', 'NO LOT')

  return (
    <TableCell
      className="rt-td rt-td-count-step pb-0"
      tooltipClassname="w-75"
      tooltipLabel={tooltipLabel}
      customTooltip
    >
      <SelectField
        value={value}
        options={selectOptions}
        disabled={isDisabled}
        onChange={onChange}
        onBlur={onBlur}
        placeholder={placeholder}
        className="m-1"
        hideErrorMessageWrapper
        creatable
      />
    </TableCell>
  );
};

export default LotNumberCell;
