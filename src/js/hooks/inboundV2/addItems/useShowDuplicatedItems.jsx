import React, { useEffect } from 'react';

import { createColumnHelper } from '@tanstack/react-table';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import showDuplicatedItemsColumn from 'consts/showDuplicatedItemsColumn';
import useTranslate from 'hooks/useTranslate';

const useConfirmDuplicatedItemsModal = () => {
  const translate = useTranslate();
  const columnHelper = createColumnHelper();

  useEffect(() => {
    document.body.style.overflowY = 'hidden';
    return () => {
      document.body.style.overflowY = 'auto';
    };
  }, []);

  const columns = [
    columnHelper.accessor(showDuplicatedItemsColumn.PRODUCT_PRODUCT_CODE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.stockMovement.code.label', 'Code')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          customTooltip
          tooltipLabel={getValue()}
          className="rt-td"
        >
          {getValue()}
        </TableCell>
      ),
      size: 50,
    }),
    columnHelper.accessor(showDuplicatedItemsColumn.PRODUCT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.stockMovement.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        const { name, id } = getValue();
        return (
          <TableCell
            customTooltip
            tooltipLabel={name}
            link={INVENTORY_ITEM_URL.showStockCard(id)}
            className="rt-td multiline-cell"
          >
            <div className="limit-lines-2">{name}</div>
          </TableCell>
        );
      },
      size: 150,
    }),
    columnHelper.accessor(showDuplicatedItemsColumn.QUANTITY_REQUESTED, {
      header: () => (
        <TableHeaderCell>
          {translate('react.stockMovement.quantity.label', 'Quantity')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          customTooltip
          tooltipLabel={getValue()}
          className="rt-td multiline-cell"
        >
          {getValue()}
        </TableCell>
      ),
      size: 50,
    }),
  ];

  return { columns };
};

export default useConfirmDuplicatedItemsModal;
