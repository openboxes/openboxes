import React, { useRef } from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useIconsAfterLastLine from 'hooks/useIconsAfterLastLine';
import useTranslate from 'hooks/useTranslate';
import ProductHandlingIcons from 'utils/ProductHandlingIcons';

import 'utils/cells/ProductNameCell.scss';

/**
 * The product name cell, optionally with the product's handling icons displayed after the name.
 * Given maxLines, cutting the name down to them, so the name ends with ellipsis.
 */
const ProductNameCell = React.memo(({
  product, label, defaultLabel, className, showHandlingIcons, maxLines,
}) => {
  const translate = useTranslate();
  const textRef = useRef(null);
  const iconsRef = useRef(null);
  const handlingLabels = showHandlingIcons ? product?.handlingLabels : [];
  const hasHandlingIcons = Boolean(handlingLabels?.length);
  const isClamped = Boolean(maxLines);

  useIconsAfterLastLine({
    textRef,
    iconsRef,
    enabled: isClamped && hasHandlingIcons,
    content: product?.name,
    iconsCount: handlingLabels?.length,
  });

  return (
    <TableCell className="rt-td multiline-cell" customTooltip tooltipLabel={product?.name}>
      <div className="product-name">
        <div
          ref={textRef}
          className={`${isClamped ? `limit-lines-${maxLines}` : ''} ${className}`}
          aria-label={translate(label, defaultLabel)}
        >
          {product?.name}
          {!isClamped && <ProductHandlingIcons handlingLabels={handlingLabels} />}
        </div>
        {hasHandlingIcons && isClamped && (
          <span ref={iconsRef} className="product-name__icons--pinned">
            <ProductHandlingIcons handlingLabels={handlingLabels} />
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
    handlingLabels: PropTypes.arrayOf(PropTypes.shape({
      labelCode: PropTypes.string,
      labelText: PropTypes.string,
    })),
  }),
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
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
