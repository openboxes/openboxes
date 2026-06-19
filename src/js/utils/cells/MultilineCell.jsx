import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering a value limited to two lines with the full value displayed on a tooltip.
 */
const MultilineCell = React.memo(({ value, label, defaultLabel }) => {
  const translate = useTranslate();

  return (
    <TableCell className="rt-td multiline-cell" customTooltip tooltipLabel={value}>
      <div className="limit-lines-2" aria-label={translate(label, defaultLabel)}>
        {value}
      </div>
    </TableCell>
  );
});

MultilineCell.displayName = 'MultilineCell';

MultilineCell.propTypes = {
  value: PropTypes.string,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
};

MultilineCell.defaultProps = {
  value: undefined,
};

export default MultilineCell;
