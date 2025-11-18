import React, { useEffect, useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import * as locales from 'date-fns/locale';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import stockMovementApi from 'api/services/StockMovementApi';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import inboundColumns from 'consts/inboundColumns';
import { OutboundWorkflowState } from 'consts/StockMovementState';
import { DateFormatDateFns } from 'consts/timeFormat';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';

const useInboundSendTable = () => {
  const [tableData, setTableData] = useState([]);
  const [loading, setLoading] = useState(true);
  const {
    currentLocale,
  } = useSelector((state) => ({
    currentLocale: getCurrentLocale(state),
  }));
  const spinner = useSpinner();
  const queryParams = useQueryParams();
  const translate = useTranslate();
  const columnHelper = createColumnHelper();

  const formatDate = (date) =>
    formatDateToString({
      date,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
      options: { locale: locales[currentLocale] },
    });

  const fetchStockMovementItems = async () => {
    try {
      spinner.show();
      setLoading(true);

      const response = await stockMovementApi.getStockMovementItems(queryParams.id,
        { stepNumber: OutboundWorkflowState.SEND_SHIPMENT });

      const { data } = response.data;
      setTableData(data);
    } finally {
      setLoading(false);
      spinner.hide();
    }
  };

  useEffect(() => {
    fetchStockMovementItems();
  }, []);

  // Determines whether the Pack Level 1 column should be shown
  const showPackLevel1Column = useMemo(
    () => tableData.some((row) => row[inboundColumns.PALLET_NAME]),
    [loading],
  );

  // Determines whether the Pack Level 2 column should be shown
  const showPackLevel2Column = useMemo(
    () => tableData.some((row) => row[inboundColumns.BOX_NAME]),
    [loading],
  );

  const columns = useMemo(() => [
    columnHelper.accessor(inboundColumns.PALLET_NAME, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.stockMovement.packLevel1.label', 'Pack Level 1')}
        >
          {translate('react.stockMovement.packLevel1.label', 'Pack Level 1')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td-send-step"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div className="text-truncate">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 100,
      meta: {
        hide: !showPackLevel1Column && !loading,
      },
    }),
    columnHelper.accessor(inboundColumns.BOX_NAME, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.stockMovement.packLevel2.label', 'Pack Level 2')}
        >
          {translate('react.stockMovement.packLevel2.label', 'Pack Level 2')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td-send-step"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div className="text-truncate">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 100,
      meta: {
        hide: !showPackLevel2Column && !loading,
      },
    }),
    columnHelper.accessor(inboundColumns.PRODUCT_CODE, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.stockMovement.code.label', 'Code')}
        >
          {translate('react.stockMovement.code.label', 'Code')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td-send-step"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div className="text-truncate">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 50,
    }),
    columnHelper.accessor(inboundColumns.PRODUCT_NAME, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.stockMovement.product.label', 'Product')}
        >
          {translate('react.stockMovement.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td-send-step"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div className="text-truncate">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 300,
    }),
    columnHelper.accessor(inboundColumns.INVENTORY_ITEM_LOT_NUMBER, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.stockMovement.lot.label', 'Lot')}
        >
          {translate('react.stockMovement.lot.label', 'Lot')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td-send-step"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div className="text-truncate">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 100,
    }),
    columnHelper.accessor(inboundColumns.INVENTORY_ITEM_EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.stockMovement.expiry.label', 'Expiry')}
        >
          {translate('react.stockMovement.expiry.label', 'Expiry')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td-send-step"
          customTooltip
          tooltipLabel={formatDate(getValue())}
        >
          {formatDate(getValue())}
        </TableCell>
      ),
      size: 100,
    }),
    columnHelper.accessor(inboundColumns.QUANTITY_PICKED, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.stockMovement.quantityPicked.label', 'Qty Picked')}
        >
          {translate('react.stockMovement.quantityPicked.label', 'Qty Picked')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td-send-step"
          customTooltip
          tooltipLabel={getValue()?.toString()}
        >
          <div className="text-truncate">
            {getValue()?.toString()}
          </div>
        </TableCell>
      ),
      size: 75,
    }),
    columnHelper.accessor(inboundColumns.RECIPIENT_NAME, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.stockMovement.recipient.label', 'Recipient')}
        >
          {translate('react.stockMovement.recipient.label', 'Recipient')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td-send-step"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div className="text-truncate">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 100,
    }),
  ], [currentLocale, loading]);

  return {
    columns,
    tableData,
    loading,
  };
};

export default useInboundSendTable;
