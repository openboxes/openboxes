import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering a dropdown.
 */
const SelectCell = React.memo(({
  options,
  value,
  onChange,
  label,
  defaultLabel,
  placeholder,
  disabled,
  productSelect,
  locationId,
  async,
  loadOptions,
  showValueTooltip,
  onBlur,
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
          placeholder={placeholder}
          disabled={disabled}
          hideErrorMessageWrapper
          productSelect={productSelect}
          locationId={locationId}
          async={async}
          loadOptions={loadOptions}
          showValueTooltip={showValueTooltip}
          onBlur={onBlur}
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
  productSelect: PropTypes.bool,
  locationId: PropTypes.string,
  async: PropTypes.bool,
  loadOptions: PropTypes.func,
  showValueTooltip: PropTypes.bool,
  onBlur: PropTypes.func,
};

SelectCell.defaultProps = {
  options: [],
  value: null,
  onChange: undefined,
  placeholder: '',
  disabled: false,
  productSelect: false,
  locationId: null,
  async: false,
  loadOptions: () => [],
  showValueTooltip: false,
  onBlur: undefined,
};

export default SelectCell;
