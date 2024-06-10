import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import Subsection from 'components/Layout/v2/Subsection';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import useTranslate from 'hooks/useTranslate';

const OutboundImportItems = ({ data, errors }) => {
  const translate = useTranslate();

  const columns = useMemo(() => [
    {
      Header: translate('react.outboundImport.table.column.productCode.label', 'Code'),
      accessor: 'product.productCode',
      Cell: (row) => <TableCell {...row} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.productName.label', 'Product'),
      accessor: 'product.name',
      Cell: (row) => (
        <TableCell
          {...row}
          showError
          link={INVENTORY_ITEM_URL.showStockCard(row.original.product?.id)}
        />
      ),
    },
    {
      Header: translate('react.outboundImport.table.column.lotNumber.label', 'Lot'),
      accessor: 'lotNumber',
      Cell: (row) => <TableCell {...row} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.expirationDate.label', 'Expiry'),
      accessor: 'expirationDate',
      Cell: (row) => <DateCell {...row} />,
    },
    {
      Header: translate('react.outboundImport.table.column.quantityPicked.label', 'Qty Picked'),
      accessor: 'quantityPicked',
      Cell: (row) => <TableCell {...row} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.binLocation.label', 'Bin Location'),
      accessor: 'binLocation.name',
      Cell: (row) => <TableCell {...row} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.recipient.label', 'Recipient'),
      accessor: 'recipient.name',
      Cell: (row) => <TableCell {...row} showError />,
    },
  ], [translate]);

  return (
    <Subsection
      title={{
        label: 'react.outboundImport.form.sendingOptions.labeld',
        defaultMessage: 'Items',
      }}
      collapsable={false}
    >
      <DataTable
        style={{ maxHeight: '20rem' }}
        showPagination={false}
        pageSize={data.length}
        columns={columns}
        errors={errors}
        data={data}
        loading={false}
      />
    </Subsection>
  );
};

export default OutboundImportItems;

OutboundImportItems.defaultProps = {
  data: [],
  errors: {},
};

OutboundImportItems.propTypes = {
  errors: PropTypes.shape({}),
  data: PropTypes.arrayOf(PropTypes.shape({
    product: PropTypes.shape({
      id: PropTypes.string,
      productCode: PropTypes.string,
    }),
    lotNumber: PropTypes.string,
    quantityPicked: PropTypes.number,
    binLocation: PropTypes.shape({
      id: PropTypes.string,
      name: PropTypes.string,
      zone: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
      }),
    }),
    recipient: PropTypes.shape({
      id: PropTypes.string,
      name: PropTypes.string,
      firstName: PropTypes.string,
      lastName: PropTypes.string,
      username: PropTypes.string,
    }),
  })),
};
