import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import TextInput from 'components/form-elements/v2/TextInput';

const RECEIVING_NOW_LABEL = {
  id: 'react.receiving.receivingNow.label',
  defaultMessage: 'Receiving Now',
};

const blurOnWheel = (e) => e.currentTarget.blur();

/**
 * Memoized cell rendering the editable "receiving now" quantity input.
 */
const QuantityInputCell = React.memo(({ defaultValue }) => (
  <TableCell className="rt-td">
    <TextInput
      type="number"
      className="hide-arrows input-xs"
      defaultValue={defaultValue}
      ariaLabel={RECEIVING_NOW_LABEL}
      onWheel={blurOnWheel}
    />
  </TableCell>
));

QuantityInputCell.displayName = 'QuantityInputCell';

QuantityInputCell.propTypes = {
  defaultValue: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
};

QuantityInputCell.defaultProps = {
  defaultValue: undefined,
};

export default QuantityInputCell;
