import React, { useEffect, useMemo, useState } from 'react';

import PropTypes from 'prop-types';
import { useDispatch, useSelector } from 'react-redux';
import { getFormatLocalizedDate, makeGetCycleCountItem } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import DatePicker from 'components/form-elements/v2/DateField';
import { NEW_ROW } from 'consts/cycleCount';
import { DateFormat } from 'consts/timeFormat';

const ExpirationDateCell = ({
  id,
  cycleCountId,
  disabledExpirationDateFields,
  isStepEditable,
}) => {
  const getCycleCountItem = useMemo(() => makeGetCycleCountItem(), []);
  const item = useSelector(
    (state) => getCycleCountItem(state, cycleCountId, id),
  );
  const { inventoryItem: { expirationDate: initialValue } } = item;

  const [value, setValue] = useState(initialValue);

  useEffect(() => {
    setValue(initialValue);
  }, [initialValue]);

  const dispatch = useDispatch();

  if (!isStepEditable) {
    const formatLocalizedDate = useSelector(getFormatLocalizedDate);

    return (
      <TableCell
        className="static-cell-count-step d-flex align-items-center"
      >
        {formatLocalizedDate(value, DateFormat.DD_MMM_YYYY)}
      </TableCell>
    );
  }

  const isDisabled = disabledExpirationDateFields?.[id]
    || !id?.includes(NEW_ROW);

  const onChange = (date) => {
    setValue(date);
  };

  const onBlur = () => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: 'inventoryItem.expirationDate',
        value,
      }),
    );
  };

  return (
    <TableCell
      className="rt-td rt-td-count-step"
    >
      <DatePicker
        value={value || disabledExpirationDateFields?.[id]}
        customDateFormat={DateFormat.DD_MMM_YYYY}
        onChange={onChange}
        onBlur={onBlur}
        disabled={isDisabled}
        className="m-1 w-75"
        hideErrorMessageWrapper
      />
    </TableCell>
  );
};

export default ExpirationDateCell;

ExpirationDateCell.propTypes = {
  id: PropTypes.string.isRequired,
  cycleCountId: PropTypes.string.isRequired,
  disabledExpirationDateFields: PropTypes.objectOf(PropTypes.string).isRequired,
  isStepEditable: PropTypes.bool.isRequired,
};
