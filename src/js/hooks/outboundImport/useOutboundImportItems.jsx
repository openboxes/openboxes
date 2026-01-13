import React, { useMemo, useRef, useState } from 'react';

import { RiSearchLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getHasBinLocationSupport } from 'selectors';

import { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import useOutboundImportFiltering from 'hooks/outboundImport/useOutboundImportFiltering';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import { formatProductDisplayName } from 'utils/form-values-utils';

const useOutboundImportItems = ({ itemsInOrder }) => {
  const [isItemInStockModalOpen, setIsItemInStockModalOpen] = useState(false);
  const selectedItemInStock = useRef(null);

  const translate = useTranslate();
  const spinner = useSpinner();

  const isPalletColumnEmpty = useMemo(() =>
    !itemsInOrder?.some((it) => it.palletName), [itemsInOrder?.length]);
  const isBoxColumnEmpty = useMemo(() =>
    !itemsInOrder?.some((it) => it.boxName), [itemsInOrder?.length]);

  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);

  const {
    setIsFiltered,
    isFiltered,
    getFilteredTableData,
    getTablePageSize,
    toggleFiltering,
  } = useOutboundImportFiltering();

  const handleCloseItemInStockModal = () => {
    selectedItemInStock.current = null;
    setIsItemInStockModalOpen(false);
  };

  const handleOpenItemInStockModal = (product) => {
    spinner.show();
    selectedItemInStock.current = product;
    setIsItemInStockModalOpen(true);
  };

  const columns = useMemo(() => [
    {
      Header: translate('react.outboundImport.rowNumber.label', 'Row number'),
      accessor: 'fileRowNumber',
      width: 120,
    },
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
          tooltip
          tooltipLabel={row.original?.product?.name}
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
      Cell: (row) => <DateCell {...row} defaultValue="" />,
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
      Cell: (row) => <TableCell {...row} showError tooltip />,
    },
    {
      Header: translate('react.outboundImport.table.column.recipient.label', 'Recipient'),
      accessor: 'recipient',
      Cell: (row) => <TableCell {...row} showError tooltip />,
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
    {
      Cell: (row) => (
        <RiSearchLine
          className="text-primary cursor-pointer"
          onClick={() => handleOpenItemInStockModal(row.original?.product)}
          size={20}
        />
      ),
      width: 50,
    },
  ], [translate, isPalletColumnEmpty, isBoxColumnEmpty, hasBinLocationSupport]);

  return {
    columns,
    isFiltered,
    setIsFiltered,
    getFilteredTableData,
    getTablePageSize,
    toggleFiltering,
    handleCloseItemInStockModal,
    selectedItemInStock: selectedItemInStock.current,
    isItemInStockModalOpen,
  };
};

export default useOutboundImportItems;
