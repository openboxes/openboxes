import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';

/**
 * Memoized cell rendering a value limited to two lines with the full value displayed on a tooltip.
 */
const MultilineCell = React.memo(({ value }) => (
  <TableCell className="rt-td multiline-cell" customTooltip tooltipLabel={value}>
    <div className="limit-lines-2">
      {value}
    </div>
  </TableCell>
));

MultilineCell.displayName = 'MultilineCell';

MultilineCell.propTypes = {
  value: PropTypes.string,
};

MultilineCell.defaultProps = {
  value: undefined,
};

export default MultilineCell;
