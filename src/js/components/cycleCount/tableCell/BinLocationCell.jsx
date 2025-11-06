import React, { useMemo } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { getBinLocations, makeGetCycleCountItem } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import useTranslate from 'hooks/useTranslate';
import { getBinLocationToDisplay, groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';

const BinLocationCell = ({
  id,
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

  const dispatch = useDispatch();

  const onChange = (selected) => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: 'binLocation',
        value: { id: selected?.id, name: selected?.name },
      }),
    );
  };

  const selectOptions = useMemo(() =>
    groupBinLocationsByZone(binLocations, translate), []);

  const isDisabled = useMemo(() =>
    !id?.includes('newRow'), []);

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
        labelKey="name"
        options={selectOptions}
        onChange={onChange}
        disabled={isDisabled}
        className="m-1"
        hideErrorMessageWrapper
      />
    </TableCell>
  );
};

export default BinLocationCell;
