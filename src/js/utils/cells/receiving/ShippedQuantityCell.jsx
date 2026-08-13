import React from 'react';

import PropTypes from 'prop-types';
import { RiInformationLine } from 'react-icons/ri';

import { TableCell } from 'components/DataTable';
import useFormatNumber from 'hooks/useFormatNumber';
import useTranslate from 'hooks/useTranslate';
import getShippedQuantityInPoUom from 'utils/receiving/getShippedQuantityInPoUom';

/**
 * On a purchase order line the shipped quantity carries an info icon and a tooltip with its
 * equivalent in the order's unit of measure, for example "75 PK/100".
 * Otherwise, it is just the number.
 * The displayed value is always in single units (each), never in packs.
 */
const ShippedQuantityCell = React.memo(({
  item, isShipmentFromPurchaseOrder, label, defaultLabel,
}) => {
  const translate = useTranslate();
  const formatNumber = useFormatNumber();

  const value = formatNumber(item?.quantityShipped);
  // Only a purchase order has a unit of measure to convert the quantity into.
  const quantityInPoUomLabel = isShipmentFromPurchaseOrder
    ? getShippedQuantityInPoUom({ item, formatNumber })
    : null;

  return (
    <TableCell className="rt-td" customTooltip tooltipLabel={quantityInPoUomLabel || value}>
      <div
        className="receiving-table__quantity d-flex align-items-center"
        aria-label={translate(label, defaultLabel)}
      >
        {value}
        {quantityInPoUomLabel && (
          <RiInformationLine className="receiving-table__shipped-info ml-1" size={20} />
        )}
      </div>
    </TableCell>
  );
});

ShippedQuantityCell.displayName = 'ShippedQuantityCell';

ShippedQuantityCell.propTypes = {
  item: PropTypes.shape({
    quantityShipped: PropTypes.number,
    packSize: PropTypes.number,
    unitOfMeasure: PropTypes.string,
  }),
  isShipmentFromPurchaseOrder: PropTypes.bool,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
};

ShippedQuantityCell.defaultProps = {
  item: undefined,
  isShipmentFromPurchaseOrder: false,
};

export default ShippedQuantityCell;
