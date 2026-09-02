import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering a dropdown.
 */
const SelectCell = React.memo(({
  options, value, onChange, label, defaultLabel, placeholder, disabled,
}) => {
  const translate = useTranslate();

  return (
    <TableCell className="rt-td">
      <div className="w-100" aria-label={translate(label, defaultLabel)}>
        <SelectField
          options={options}
          value={value}
          onChange={onChange}
          labelKey="name"
          valueKey="id"
          placeholder={placeholder}
          disabled={disabled}
          hideErrorMessageWrapper
        />
      </div>
    </TableCell>
  );
});

SelectCell.displayName = 'SelectCell';

SelectCell.propTypes = {
  options: PropTypes.arrayOf(PropTypes.shape({})),
  value: PropTypes.shape({}),
  onChange: PropTypes.func,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  placeholder: PropTypes.string,
  disabled: PropTypes.bool,
};

SelectCell.defaultProps = {
  options: [],
  value: null,
  onChange: undefined,
  placeholder: '',
  disabled: false,
};

export default SelectCell;
