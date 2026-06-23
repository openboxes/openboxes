import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering the shipped quantity in the purchase order's unit of measure,
 * e.g. "54" with a muted "BTL/4" suffix.
 */
const ShippedInPoCell = React.memo(({
  packs, unitOfMeasure, label, defaultLabel,
}) => {
  const translate = useTranslate();

  const tooltipLabel = unitOfMeasure ? `${packs} ${unitOfMeasure}` : undefined;

  return (
    <TableCell className="rt-td" customTooltip tooltipLabel={tooltipLabel}>
      <div className="shipped-po" aria-label={translate(label, defaultLabel)}>
        {packs}
        {unitOfMeasure && <span className="shipped-po__uom">{unitOfMeasure}</span>}
      </div>
    </TableCell>
  );
});

ShippedInPoCell.displayName = 'ShippedInPoCell';

ShippedInPoCell.propTypes = {
  packs: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  unitOfMeasure: PropTypes.string,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
};

ShippedInPoCell.defaultProps = {
  packs: null,
  unitOfMeasure: undefined,
};

export default ShippedInPoCell;
