import React from 'react';

import PropTypes from 'prop-types';
import { FaArrowRight } from 'react-icons/fa';

import { TableCell } from 'components/DataTable';

/**
 * Green arrow and/or product code, shown only on the first split item row of a product.
 */
const SplitItemCellComponent = ({
  isFirstSplitItem = false, productCode = null, withArrow = false, className = '',
}) => (
  <TableCell
    className={`rt-td ${className}`}
    customTooltip
    tooltipLabel={isFirstSplitItem ? productCode : null}
  >
    {isFirstSplitItem && (
      <>
        {withArrow && (
          <FaArrowRight
            size={16}
            className={`receiving-table__split-item-arrow ${productCode ? 'mr-2' : ''}`}
            data-testid="split-item-arrow"
          />
        )}
        {productCode && <span>{productCode}</span>}
      </>
    )}
  </TableCell>
);

const SplitItemCell = React.memo(SplitItemCellComponent);

SplitItemCell.displayName = 'SplitItemCell';

SplitItemCellComponent.propTypes = {
  isFirstSplitItem: PropTypes.bool,
  productCode: PropTypes.string,
  withArrow: PropTypes.bool,
  className: PropTypes.string,
};

export default SplitItemCell;
