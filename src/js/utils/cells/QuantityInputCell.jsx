import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import TextInput from 'components/form-elements/v2/TextInput';

const blurOnWheel = (e) => e.currentTarget.blur();

/**
 * Memoized cell rendering an editable quantity input.
 */
const QuantityInputCell = React.memo(({ defaultValue, label, defaultLabel }) => (
  <TableCell className="rt-td">
    <TextInput
      type="number"
      className="hide-arrows input-xs"
      defaultValue={defaultValue}
      ariaLabel={{ id: label, defaultMessage: defaultLabel }}
      onWheel={blurOnWheel}
    />
  </TableCell>
));

QuantityInputCell.displayName = 'QuantityInputCell';

QuantityInputCell.propTypes = {
  defaultValue: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
};

QuantityInputCell.defaultProps = {
  defaultValue: undefined,
};

export default QuantityInputCell;
