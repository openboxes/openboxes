import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import SelectField from 'components/form-elements/v2/SelectField';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering a dropdown.
 */
const SelectCellComponent = ({
  options = [],
  value = null,
  onChange = undefined,
  label,
  defaultLabel,
  placeholder = '',
  disabled = false,
  selectFieldComponent: SelectFieldComponent = SelectField,
  labelKey = 'name',
  locationId = null,
  async = false,
  loadOptions = () => [],
  showValueTooltip = true,
  onBlur = undefined,
  clearable = true,
  errorMessage = null,
}) => {
  const translate = useTranslate();

  return (
    <TableCell
      className="rt-td"
      customTooltip
      tooltipLabel={errorMessage}
      tooltipClassname="w-100"
    >
      <div className="w-100" aria-label={translate(label, defaultLabel)}>
        <SelectFieldComponent
          options={options}
          value={value}
          onChange={onChange}
          labelKey={labelKey}
          placeholder={placeholder}
          disabled={disabled}
          errorMessage={errorMessage}
          hideErrorMessageWrapper
          locationId={locationId}
          async={async}
          loadOptions={loadOptions}
          showValueTooltip={showValueTooltip && !errorMessage}
          onBlur={onBlur}
          clearable={clearable}
        />
      </div>
    </TableCell>
  );
};

const SelectCell = React.memo(SelectCellComponent);

SelectCell.displayName = 'SelectCell';

SelectCellComponent.propTypes = {
  options: PropTypes.arrayOf(PropTypes.shape({})),
  value: PropTypes.shape({}),
  onChange: PropTypes.func,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  placeholder: PropTypes.string,
  disabled: PropTypes.bool,
  selectFieldComponent: PropTypes.elementType,
  labelKey: PropTypes.string,
  locationId: PropTypes.string,
  async: PropTypes.bool,
  loadOptions: PropTypes.func,
  showValueTooltip: PropTypes.bool,
  onBlur: PropTypes.func,
  clearable: PropTypes.bool,
  errorMessage: PropTypes.string,
};

export default SelectCell;
