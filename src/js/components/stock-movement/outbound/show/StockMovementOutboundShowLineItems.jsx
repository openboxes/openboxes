import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import DateFormat from 'consts/dateFormat';
import StatusIndicator from 'utils/StatusIndicator';
import Translate from 'utils/Translate';

const StockMovementOutboundShowLineItems = ({ lineItems }) => {
  const columns = useMemo(() => [
    {
      Header: <Translate id="react.stockMovement.productCode.label" defaultMessage="Product Code" />,
      accessor: 'productCode',
      width: 120,
      Cell: (row) => <TableCell {...row} tooltip />,
    },
    {
      Header: <Translate id="react.stockMovement.product.label" defaultMessage="Product" />,
      accessor: 'product.name',
      minWidth: 250,
      Cell: (row) => <TableCell {...row} tooltip />,
    },
    {
      Header: <Translate id="react.stockMovement.lotNumber.label" defaultMessage="Lot Number" />,
      accessor: 'lotNumber',
      width: 150,
      Cell: (row) => <TableCell {...row} />,
    },
    {
      Header: <Translate id="react.stockMovement.expirationDate.label" defaultMessage="Expiry Date" />,
      accessor: 'expirationDate',
      width: 130,
      Cell: (row) => (
        <DateCell
          localizeDate
          formatLocalizedDate={DateFormat.DISPLAY}
          {...row}
        />
      ),
    },
    {
      Header: <Translate id="react.stockMovement.quantityRequired.label" defaultMessage="Qty Required" />,
      accessor: 'quantityRequired',
      width: 130,
      Cell: (row) => <TableCell {...row} />,
    },
    {
      Header: <Translate id="react.stockMovement.quantityPicked.label" defaultMessage="Qty Picked" />,
      accessor: 'quantityPicked',
      width: 120,
      Cell: (row) => <TableCell {...row} />,
    },
    {
      Header: <Translate id="react.stockMovement.column.status.label" defaultMessage="Status" />,
      accessor: 'statusCode',
      width: 130,
      Cell: (row) => (
        <TableCell {...row}>
          {row.value && (
            <StatusIndicator
              variant="default"
              status={row.value}
            />
          )}
        </TableCell>
      ),
    },
  ], []);

  return (
    <div className="stock-movement-show-line-items">
      <DataTable
        columns={columns}
        data={lineItems}
        defaultPageSize={10}
        showPagination={lineItems.length > 10}
        noDataText="No line items"
      />
    </div>
  );
};

StockMovementOutboundShowLineItems.propTypes = {
  lineItems: PropTypes.arrayOf(PropTypes.shape({
    productCode: PropTypes.string,
    product: PropTypes.shape({ name: PropTypes.string }),
    lotNumber: PropTypes.string,
    expirationDate: PropTypes.string,
    quantityRequired: PropTypes.number,
    quantityPicked: PropTypes.number,
    statusCode: PropTypes.string,
  })),
};

StockMovementOutboundShowLineItems.defaultProps = {
  lineItems: [],
};

export default StockMovementOutboundShowLineItems;
