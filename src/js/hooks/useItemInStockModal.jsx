import React, { useEffect, useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';
import { getCurrentLocationId } from 'selectors';

import productApi from 'api/services/ProductApi';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import outboundImportColumn from 'consts/outboundImportColumn';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';

const useItemInStockModal = ({ productId }) => {
  const [data, setData] = useState([]);

  const columnHelper = createColumnHelper();
  const translate = useTranslate();
  const spinner = useSpinner();

  const currentLocationId = useSelector(getCurrentLocationId);

  const fetchData = async () => {
    if (currentLocationId && productId) {
      try {
        const response = await productApi.availableItems({
          locationId: currentLocationId,
          productIds: productId,
        });
        setData(response?.data?.data);
      } finally {
        spinner.hide();
      }
    }
  };

  useEffect(() => {
    fetchData();
  }, [productId, currentLocationId]);

  const columns = useMemo(() => [
    columnHelper.accessor(outboundImportColumn.BIN_LOCATION, {
      header: () => (
        <TableHeaderCell>
          {translate('react.outboundImport.table.column.binLocation.label', 'Bin Location')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor(outboundImportColumn.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell>
          {translate('react.outboundImport.table.column.lotNumber.label', 'Lot')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor(outboundImportColumn.EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.outboundImport.table.column.expires.label', 'Expires')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor(outboundImportColumn.QUANTITY_ON_HAND, {
      header: () => (
        <TableHeaderCell>
          {translate('react.outboundImport.table.column.onHand.label', 'On Hand')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue() ?? '0'}
        </TableCell>
      ),
    }),
    columnHelper.accessor(outboundImportColumn.QUANTITY_AVAILABLE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.outboundImport.table.column.available.label', 'Available')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue() ?? '0'}
        </TableCell>
      ),
    }),
  ], [translate]);

  return { columns, data };
};

export default useItemInStockModal;
