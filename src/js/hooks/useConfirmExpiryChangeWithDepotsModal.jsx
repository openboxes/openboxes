import React from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import * as locales from 'date-fns/locale';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import depotAvailabilityColumn from 'consts/depotAvailabilityColumn';
import useTranslate from 'hooks/useTranslate';
import { formatDateToDatetimeString } from 'utils/dateUtils';

const useConfirmExpiryChangeWithDepotsModal = () => {
  const translate = useTranslate();
  const currentLocale = useSelector(getCurrentLocale);
  const columnHelper = createColumnHelper();

  const formatDate = (date) => formatDateToDatetimeString(date, locales[currentLocale])
    ?? translate('react.confirmExpirationDate.modal.noDate.label', 'no date');

  const columns = [
    columnHelper.accessor(depotAvailabilityColumn.DEPOT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.confirmExpirationDate.modal.depot.label', 'Depot')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell customTooltip tooltipLabel={getValue()} className="rt-td">
          {getValue()}
        </TableCell>
      ),
      size: 300,
    }),
    columnHelper.accessor(depotAvailabilityColumn.QUANTITY_ON_HAND, {
      header: () => (
        <TableHeaderCell>
          {translate('react.confirmExpirationDate.modal.qty.label', 'QTY')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => <TableCell className="rt-td">{getValue()}</TableCell>,
      size: 100,
    }),
  ];

  return { columns, formatDate };
};

export default useConfirmExpiryChangeWithDepotsModal;
