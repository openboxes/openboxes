import React, { useEffect } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { locales } from 'moment';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import confirmExpirationDateColumn from 'consts/confirmExpirationDateColumn';
import { DateFormatDateFns } from 'consts/timeFormat';
import { formatDateToString } from 'utils/dateUtils';

import useTranslate from './useTranslate';

const useConfirmExpirationDateModal = () => {
  const translate = useTranslate();
  const columnHelper = createColumnHelper();
  const {
    currentLocale,
  } = useSelector((state) => ({
    currentLocale: getCurrentLocale(state),
  }));

  const formatDate = (date) =>
    formatDateToString({
      date,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
      options: { locale: locales[currentLocale] },
    });

  useEffect(() => {
    document.body.style.overflowY = 'hidden';
    return () => {
      document.body.style.overflowY = 'auto';
    };
  }, []);

  const columns = [
    columnHelper.accessor(confirmExpirationDateColumn.CODE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.confirmExpirationDate.modal.code.label', 'Code')}
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
      size: 40,
    }),
    columnHelper.accessor(confirmExpirationDateColumn.PRODUCT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.confirmExpirationDate.modal.productName.label', 'Product')}
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
      size: 170,
    }),
    columnHelper.accessor(confirmExpirationDateColumn.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell>
          {translate('react.confirmExpirationDate.modal.lot.label', 'Lot')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          customTooltip
          tooltipLabel={getValue()}
          className="rt-td multiline-cell"
        >
          <div className="limit-lines-2">{getValue()}</div>
        </TableCell>
      ),
      size: 100,
    }),
    columnHelper.accessor(confirmExpirationDateColumn.PREVIOUS_EXPIRY, {
      header: () => (
        <TableHeaderCell>
          {translate('react.confirmExpirationDate.modal.previousExpiry.label', 'Previous expiry')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          customTooltip
          tooltipLabel={formatDate(getValue())}
          className="rt-td"
        >
          {formatDate(getValue())}
        </TableCell>
      ),
      size: 70,
    }),
    columnHelper.accessor(confirmExpirationDateColumn.NEW_EXPIRY, {
      header: () => (
        <TableHeaderCell>
          {translate('react.confirmExpirationDate.modal.newExpiry.label', 'New expiry')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          customTooltip
          tooltipLabel={formatDate(getValue())}
          className="rt-td"
        >
          {formatDate(getValue())}
        </TableCell>
      ),
      size: 70,
    }),
  ];

  return { columns };
};

export default useConfirmExpirationDateModal;
