import React, { useMemo, useState } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { getBinLocations } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import useTranslate from 'hooks/useTranslate';
import { getBinLocationToDisplay, groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';

const BinLocationCell = ({
  initialValue,
  id,
  cycleCountId,
  showBinLocation,
}) => {
  if (!showBinLocation) {
    return null;
  }

  const translate = useTranslate();
  const binLocations = useSelector(getBinLocations);

  const dispatch = useDispatch();

  const [value, setValue] = useState(initialValue ? { name: initialValue } : undefined);

  const onChange = (selected) => {
    setValue(selected);
  };

  const onBlur = () => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: 'binLocation',
        value: { id: value?.id, name: value?.name },
      }),
    );
  };

  const selectOptions = useMemo(() =>
    groupBinLocationsByZone(binLocations, translate), []);

  const isDisabled = useMemo(() =>
    !id.includes('newRow'), []);

  const selectedValue = value
    ? { ...value, name: getBinLocationToDisplay(value) }
    : undefined;

  return (
    <TableCell className="rt-td rt-td-count-step pb-0">
      <SelectField
        value={selectedValue}
        labelKey="name"
        options={selectOptions}
        onChange={onChange}
        onBlur={onBlur}
        disabled={isDisabled}
      />
    </TableCell>
  );
};

export default BinLocationCell;
