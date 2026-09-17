import React, { useEffect, useRef, useState } from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import TextInput from 'components/form-elements/v2/TextInput';

const blurOnWheel = (e) => e.currentTarget.blur();

/**
 * Memoized cell rendering an editable quantity input for autosaved tables.
 *
 * Unlike the generic QuantityInputCell (which commits on blur), this cell reports every change
 * through `onCommit` as the user types - the committed integer value, or null when the field is
 * cleared. The autosave hook debounces the actual requests, so committing per keystroke only
 * marks the row dirty; nothing is sent until typing pauses or enough rows accumulate.
 */
const AutosaveQuantityInputCell = React.memo(({
  value, onCommit, label, defaultLabel, disabled, className,
}) => {
  const [inputValue, setInputValue] = useState(value ?? '');
  // Last value handed to onCommit. The `value` prop lags one render behind the store, so
  // deduplicating against it would let repeated commits of the same value through.
  const lastCommittedRef = useRef(value ?? null);

  // Re-sync when the stored value changes (e.g. after a save reconciles the row).
  useEffect(() => {
    setInputValue(value ?? '');
    lastCommittedRef.current = value ?? null;
  }, [value]);

  // TextInput (type="number") hands us a number, or undefined when the field is empty.
  const onChange = (enteredValue) => {
    setInputValue(enteredValue ?? '');
    const committed = enteredValue == null ? null : Math.trunc(enteredValue);
    if (committed !== lastCommittedRef.current) {
      lastCommittedRef.current = committed;
      onCommit(committed);
    }
  };

  return (
    <TableCell className="rt-td">
      <TextInput
        type="number"
        className={`hide-arrows input-xs ${className}`}
        value={inputValue}
        onChange={onChange}
        disabled={disabled}
        min="0"
        ariaLabel={{ id: label, defaultMessage: defaultLabel }}
        onWheel={blurOnWheel}
      />
    </TableCell>
  );
});

AutosaveQuantityInputCell.displayName = 'AutosaveQuantityInputCell';

AutosaveQuantityInputCell.propTypes = {
  // Committed value to display (null/undefined when nothing has been entered yet).
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  // Called on every change with the committed integer value, or null when the field is cleared.
  onCommit: PropTypes.func.isRequired,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  className: PropTypes.string,
};

AutosaveQuantityInputCell.defaultProps = {
  value: null,
  disabled: false,
  className: '',
};

export default AutosaveQuantityInputCell;
