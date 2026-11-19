import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering a value limited to a configurable number of lines
 * (two by default) with the full value displayed on a tooltip.
 */
const MultilineCellComponent = ({
  value = undefined, label, defaultLabel, maxLines = 2, className = '',
}) => {
  const translate = useTranslate();

  return (
    <TableCell className="rt-td multiline-cell" customTooltip tooltipLabel={value}>
      <div className={`limit-lines-${maxLines} ${className}`} aria-label={translate(label, defaultLabel)}>
        {value}
      </div>
    </TableCell>
  );
};

const MultilineCell = React.memo(MultilineCellComponent);

MultilineCell.displayName = 'MultilineCell';

MultilineCellComponent.propTypes = {
  value: PropTypes.string,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  maxLines: PropTypes.number,
  className: PropTypes.string,
};

export default MultilineCell;
