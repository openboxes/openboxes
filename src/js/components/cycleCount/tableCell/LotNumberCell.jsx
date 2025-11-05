import React, { useMemo, useState } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { makeGetCycleCountProduct, makeGetLotNumbersByProductId } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';

const LotNumberCell = ({
  id,
  cycleCountId,
  initialValue,
  setDisabledExpirationDateFields,
}) => {
  const dispatch = useDispatch();

  const getCycleCountProduct = useMemo(makeGetCycleCountProduct, []);

  const cycleCountProduct = useSelector(
    (state) => getCycleCountProduct(state, id),
  );

  const getLotNumbersByProductId = useMemo(() => makeGetLotNumbersByProductId(), []);

  const lotNumbersWithExpiration = useSelector(
    (state) => getLotNumbersByProductId(state, cycleCountProduct?.id),
  );

  const [value, setValue] = useState(initialValue ? {
    label: initialValue,
    value: initialValue,
    id: initialValue,
    name: initialValue,
  } : undefined);

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

  return (
    <TableCell className="rt-td rt-td-count-step pb-0">
      <SelectField
        value={value}
        options={selectOptions}
        disabled={isDisabled}
        onChange={onChange}
        onBlur={onBlur}
        creatable
      />
    </TableCell>
  );
};

export default LotNumberCell;
