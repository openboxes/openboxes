import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';

/**
 * Memoized cell rendering a single value with an optional tooltip and truncation.
 */
const ValueCell = React.memo(({
  value, ariaLabel, tooltipLabel, truncate,
}) => (
  <TableCell className="rt-td" customTooltip tooltipLabel={tooltipLabel}>
    <div className={truncate ? 'text-truncate' : undefined} aria-label={ariaLabel}>
      {value}
    </div>
  </TableCell>
));

ValueCell.displayName = 'ValueCell';

ValueCell.propTypes = {
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  ariaLabel: PropTypes.string.isRequired,
  tooltipLabel: PropTypes.string,
  truncate: PropTypes.bool,
};

ValueCell.defaultProps = {
  value: null,
  tooltipLabel: undefined,
  truncate: false,
};

export default ValueCell;
