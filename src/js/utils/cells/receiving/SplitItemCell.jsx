import React from 'react';

import PropTypes from 'prop-types';
import { FaArrowRight } from 'react-icons/fa';

import { TableCell } from 'components/DataTable';

/**
 * Green arrow and/or product code, shown only on the first split item row of a product.
 */
const SplitItemCell = React.memo(({
  isFirstSplitItem, productCode, withArrow, className,
}) => (
  <TableCell
    className={`rt-td ${className}`}
    customTooltip
    tooltipLabel={isFirstSplitItem ? productCode : null}
  >
    {isFirstSplitItem && (
      <>
        {withArrow && (
          <FaArrowRight size={16} className="receiving-table__split-item-arrow" />
        )}
        {productCode && <span>{productCode}</span>}
      </>
    )}
  </TableCell>
));

SplitItemCell.displayName = 'SplitItemCell';

SplitItemCell.propTypes = {
  isFirstSplitItem: PropTypes.bool,
  productCode: PropTypes.string,
  withArrow: PropTypes.bool,
  className: PropTypes.string,
};

SplitItemCell.defaultProps = {
  isFirstSplitItem: false,
  productCode: null,
  withArrow: false,
  className: '',
};

export default SplitItemCell;
