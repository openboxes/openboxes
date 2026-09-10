import React, { useRef } from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useIconsAfterLastLine from 'hooks/receiving/v2/useIconsAfterLastLine';
import useTranslate from 'hooks/useTranslate';
import renderHandlingIcons from 'utils/product-handling-icons';

/**
 * The product name cell, optionally with the product's handling icons displayed after the name.
 * Given maxLines, cutting the name down to them, so the name ends with ellipsis.
 */
const ProductNameCell = React.memo(({
  product, className, showHandlingIcons, maxLines,
}) => {
  const translate = useTranslate();
  const textRef = useRef(null);
  const iconsRef = useRef(null);
  const handlingIcons = showHandlingIcons && renderHandlingIcons(product?.handlingIcons);
  const isClamped = Boolean(maxLines);

  useIconsAfterLastLine({
    textRef,
    iconsRef,
    enabled: isClamped && Boolean(handlingIcons),
    content: product?.name,
    iconsCount: product?.handlingIcons?.length,
  });

  return (
    <TableCell className="rt-td multiline-cell" customTooltip tooltipLabel={product?.name}>
      <div className="receiving-product-name">
        <div
          ref={textRef}
          className={`${isClamped ? `limit-lines-${maxLines}` : ''} ${className}`}
          aria-label={translate('react.receiving.product.label', 'Product')}
        >
          {product?.name}
          {handlingIcons && !isClamped && (
            <span className="receiving-product-name__icons">
              {handlingIcons}
            </span>
          )}
        </div>
        {handlingIcons && isClamped && (
          <span ref={iconsRef} className="receiving-product-name__icons--pinned">
            {handlingIcons}
          </span>
        )}
      </div>
    </TableCell>
  );
});

ProductNameCell.displayName = 'ProductNameCell';

ProductNameCell.propTypes = {
  product: PropTypes.shape({
    name: PropTypes.string,
    handlingIcons: PropTypes.arrayOf(PropTypes.shape({
      icon: PropTypes.string,
      color: PropTypes.string,
    })),
  }),
  className: PropTypes.string,
  showHandlingIcons: PropTypes.bool,
  /** Number of lines the name is cut down to. Without it the name is displayed in full. */
  maxLines: PropTypes.number,
};

ProductNameCell.defaultProps = {
  product: null,
  className: '',
  showHandlingIcons: false,
  maxLines: undefined,
};

export default ProductNameCell;
