import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import TextInput from 'components/form-elements/v2/TextInput';

const blurOnWheel = (e) => e.currentTarget.blur();

/**
 * Memoized cell rendering an editable quantity input.
 *
 * Controlled: the typed value is kept in local state so typing stays snappy, and the committed
 * integer value (or null when the field is cleared) is reported via `onCommit` on blur. The
 * caller decides where to store it; pass `value` to keep the input in sync with that store.
 */
const QuantityInputCell = React.memo(({
  value, onCommit, label, defaultLabel, disabled,
}) => {
  const [inputValue, setInputValue] = useState(value ?? '');

  // Re-sync when the stored value changes (e.g. after a save reconciles the row).
  useEffect(() => {
    setInputValue(value ?? '');
  }, [value]);

  // TextInput (type="number") hands us a number, or undefined when the field is empty.
  const onChange = (enteredValue) => {
    setInputValue(enteredValue ?? '');
  };

  // Commit on blur. Skip when nothing changed so the caller doesn't re-store / mark the row dirty.
  const onBlur = () => {
    const committed = inputValue === '' ? null : Math.trunc(inputValue);
    if (committed !== (value ?? null)) {
      onCommit(committed);
    }
  };

  return (
    <TableCell className="rt-td">
      <TextInput
        type="number"
        className="hide-arrows input-xs"
        value={inputValue}
        onChange={onChange}
        onBlur={onBlur}
        disabled={disabled}
        min="0"
        ariaLabel={{ id: label, defaultMessage: defaultLabel }}
        onWheel={blurOnWheel}
      />
    </TableCell>
  );
});

QuantityInputCell.displayName = 'QuantityInputCell';

QuantityInputCell.propTypes = {
  // Committed value to display (null/undefined when nothing has been entered yet).
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  // Called on blur with the committed integer value, or null when the field is cleared.
  onCommit: PropTypes.func.isRequired,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
};

QuantityInputCell.defaultProps = {
  value: null,
  disabled: false,
};

export default QuantityInputCell;
