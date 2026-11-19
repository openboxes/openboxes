import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering a single value with an optional tooltip and truncation.
 */
const ValueCellComponent = ({
  value = null, label, defaultLabel, tooltipLabel = undefined, truncate = false, className = '',
}) => {
  const translate = useTranslate();

  return (
    <TableCell className="rt-td" customTooltip tooltipLabel={tooltipLabel}>
      <div
        className={`${truncate ? 'text-truncate' : ''} ${className}`}
        aria-label={translate(label, defaultLabel)}
      >
        {value}
      </div>
    </TableCell>
  );
};

const ValueCell = React.memo(ValueCellComponent);

ValueCell.displayName = 'ValueCell';

ValueCellComponent.propTypes = {
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  tooltipLabel: PropTypes.string,
  truncate: PropTypes.bool,
  className: PropTypes.string,
};

export default ValueCell;
