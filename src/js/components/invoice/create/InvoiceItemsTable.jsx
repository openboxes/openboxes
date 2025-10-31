import React, { useCallback } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import { ORDER_URL, STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import { renderFormField } from 'utils/form-utils';
import { getInvoiceDescription } from 'utils/form-values-utils';
import accountingFormat from 'utils/number-utils';

const INVOICE_ITEMS = {
  invoiceItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    getDynamicRowAttr: ({ rowValues }) => ({ className: rowValues && rowValues.totalAmount && rowValues.totalAmount < 0 ? 'negative-row-value' : '' }),
    fields: {
      orderNumber: {
        type: LabelField,
        label: 'react.invoice.orderNumber.label',
        defaultMessage: 'PO Number',
        flexWidth: '1',
        getDynamicAttr: (params) => {
          const { invoiceItems, rowIndex } = params;
          const orderId = invoiceItems
            && invoiceItems[rowIndex]
            && invoiceItems[rowIndex].orderId;
          return { url: orderId ? ORDER_URL.show(orderId) : '' };
        },
      },
      shipmentNumber: {
        type: LabelField,
        label: 'react.invoice.shipmentNumber.label',
        defaultMessage: 'Shipment Number',
        flexWidth: '1',
        getDynamicAttr: (params) => {
          const { invoiceItems, rowIndex } = params;
          const shipmentId = invoiceItems
            && invoiceItems[rowIndex]
            && invoiceItems[rowIndex].shipmentId;
          return { url: shipmentId ? STOCK_MOVEMENT_URL.show(shipmentId) : '' };
        },
      },
      budgetCode: {
        type: LabelField,
        label: 'react.invoice.budgetCode.label',
        defaultMessage: 'Budget Code',
        flexWidth: '1',
      },
      glCode: {
        type: LabelField,
        label: 'react.invoice.glCode.label',
        defaultMessage: 'GL Code',
        flexWidth: '1',
      },
      productCode: {
        type: LabelField,
        label: 'react.invoice.itemNo.label',
        defaultMessage: 'Item No',
        flexWidth: '1',
      },
      description: {
        type: LabelField,
        label: 'react.invoice.description.label',
        defaultMessage: 'Description',
        flexWidth: '5',
        attributes: {
          className: 'text-left',
        },
        getDynamicAttr: (params) => ({
          formatValue: () => {
            const { invoiceItems, rowIndex } = params;
            const rowValue = invoiceItems?.[rowIndex];
            // If it's not an adjustment, but product, and it has a synonym, display it
            // with a tooltip with the original name of the product
            return getInvoiceDescription(rowValue);
          },
        }),
      },
      quantity: {
        type: LabelField,
        label: 'react.invoice.qty.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
      },
      uom: {
        type: LabelField,
        label: 'react.invoice.uom.label',
        defaultMessage: 'UOM',
        flexWidth: '1',
      },
      unitPrice: {
        type: LabelField,
        label: 'react.invoice.unitPrice.label',
        defaultMessage: 'Unit Price',
        flexWidth: '1',
        attributes: {
          formatValue: (value) => (value ? accountingFormat(value) : value),
        },
      },
      amount: {
        type: LabelField,
        label: 'react.invoice.totalPrice.label',
        defaultMessage: 'Total Price',
        flexWidth: '1',
        attributes: {
          formatValue: (value) => (value ? accountingFormat(value) : value),
        },
      },
    },
  },
};

const InvoiceItemsTable = ({
  invoiceItems, invoiceId, totalCount, loadMoreRows,
}) => {
  const isRowLoaded = useCallback(
    ({ index }) => !!invoiceItems[index],
    [invoiceItems],
  );

  return (
    <div className="my-2 table-form">
      {_.map(INVOICE_ITEMS, (fieldConfig, fieldName) =>
        renderFormField(fieldConfig, fieldName, {
          invoiceId,
          invoiceItems,
          totalCount,
          loadMoreRows,
          isRowLoaded,
        }))}
    </div>
  );
};

InvoiceItemsTable.propTypes = {
  invoiceId: PropTypes.string.isRequired,
  invoiceItems: PropTypes.shape({}).isRequired,
  totalCount: PropTypes.number.isRequired,
  loadMoreRows: PropTypes.func.isRequired,
};

export default InvoiceItemsTable;
