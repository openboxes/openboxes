import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import Subsection from 'components/Layout/v2/Subsection';
import useTranslate from 'hooks/useTranslate';
import { formatProductDisplayName } from 'utils/form-values-utils';

const OutboundImportItems = ({ data, errors }) => {
  const translate = useTranslate();

  const isPalletColumnEmpty = useMemo(() => !data.some((it) => it.palletName), data);
  const isBoxColumnEmpty = useMemo(() => !data.some((it) => it.boxName), data);

  const {
    hasBinLocationSupport,
  } = useSelector((state) => ({
    hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  }));

  const columns = useMemo(() => [
    {
      Header: translate('react.outboundImport.table.column.productCode.label', 'Code'),
      accessor: 'product.productCode',
      width: 90,
      getProps: () => ({
        errorAccessor: 'product',
      }),
      Cell: (row) => <TableCell {...row} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.productName.label', 'Product'),
      accessor: 'product.name',
      minWidth: 150,
      getProps: () => ({
        errorAccessor: 'product',
      }),
      Cell: (row) => (
        <TableCell
          {...row}
          style={{ color: row.original?.product?.color }}
          tooltip={row.original?.product?.name}
          value={formatProductDisplayName(row.original?.product)}
          showError
        />
      ),
    },
    {
      Header: translate('react.outboundImport.table.column.lotNumber.label', 'Lot'),
      accessor: 'lotNumber',
      minWidth: 120,
      Cell: (row) => <TableCell {...row} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.expirationDate.label', 'Expiry'),
      accessor: 'expirationDate',
      width: 120,
      Cell: (row) => <DateCell {...row} />,
    },
    {
      Header: translate('react.outboundImport.table.column.quantityPicked.label', 'Qty Picked'),
      accessor: 'quantityPicked',
      Cell: (row) => <TableCell {...row} value={`${row.original.quantityPicked}`} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.binLocation.label', 'Bin Location'),
      accessor: 'binLocation',
      show: hasBinLocationSupport,
      Cell: (row) => <TableCell {...row} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.recipient.label', 'Recipient'),
      accessor: 'recipient',
      Cell: (row) => <TableCell {...row} showError />,
    },
    {
      Header: translate('react.outboundImport.table.column.palletName.label', 'Pack level 1'),
      accessor: 'palletName',
      Cell: (row) => <TableCell {...row} showError />,
      show: !isPalletColumnEmpty,
    },
    {
      Header: translate('react.outboundImport.table.column.boxName.label', 'Pack level 2'),
      accessor: 'boxName',
      Cell: (row) => <TableCell {...row} showError />,
      show: !isBoxColumnEmpty,
    },
  ], [translate, isPalletColumnEmpty, isBoxColumnEmpty, hasBinLocationSupport]);

  return (
    <Subsection
      title={{
        label: 'react.outboundImport.steps.items.label',
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
