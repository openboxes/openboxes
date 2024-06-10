import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

import DataTable, { TableCell } from 'components/DataTable';
import Subsection from 'components/Layout/v2/Subsection';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import useTranslate from 'hooks/useTranslate';

const OutboundImportItems = ({ data }) => {
  const translate = useTranslate();

  const columns = useMemo(() => [
    {
      Header: translate('react.outboundImport.table.column.productCode.label', 'Code'),
      accessor: 'product.productCode',
    },
    {
      Header: translate('react.outboundImport.table.column.productName.label', 'Product'),
      accessor: 'product.name',
      Cell: row => (
        <TableCell
          {...row}
          link={INVENTORY_ITEM_URL.showStockCard(row.original.product?.id)}
        />
      ),
    },
    {
      Header: translate('react.outboundImport.table.column.lotNumber.label', 'Lot'),
      accessor: 'lotNumber',
    },
    {
      Header: translate('react.outboundImport.table.column.expirationDate.label', 'Expiry'),
      accessor: 'expirationDate',
    },
    {
      Header: translate('react.outboundImport.table.column.quantityPicked.label', 'Qty Picked'),
      accessor: 'quantityPicked',
    },
    {
      Header: translate('react.outboundImport.table.column.binLocation.label', 'Bin Location'),
      accessor: 'binLocation.name',
    },
    {
      Header: translate('react.outboundImport.table.column.recipient.label', 'Recipient'),
      accessor: 'recipient.name',
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
        data={data}
        loading={false}
      />
    </Subsection>
  );
};

export default OutboundImportItems;

OutboundImportItems.defaultProps = {
  data: [],
};

OutboundImportItems.propTypes = {
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
