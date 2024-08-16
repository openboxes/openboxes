import React, { useCallback } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { RiCloseCircleLine } from 'react-icons/all';
import { Tooltip } from 'react-tippy';

import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import { ORDER_URL, STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import InvoiceItemType from 'consts/invoiceItemType';
import { renderFormField } from 'utils/form-utils';
import { getInvoiceDescription } from 'utils/form-values-utils';
import accountingFormat from 'utils/number-utils';

const getRowColouring = (canceled, type) => {
  const isInverseItem = type === InvoiceItemType.INVERSE;
  if (canceled) {
    return 'disabled-row';
  }
  return isInverseItem ? 'negative-row-value' : null;
};

const INVOICE_ITEMS = {
  invoiceItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    getDynamicRowAttr: ({ rowValues }) => ({
      className: getRowColouring(rowValues?.isCanceled, rowValues?.type),
    }),
    fields: {
      rowIcon: {
        type: (params) => {
          const {
            invoiceItems,
            rowIndex,
            isPrepaymentInvoice,
          } = params;
          const hasItems = !!invoiceItems;
          const invoiceItem = invoiceItems[rowIndex];
          const isPrepLine = hasItems && (isPrepaymentInvoice
            || invoiceItem?.isPrepaymentItem
          );

          if (isPrepLine) {
            return (
              <div className="d-flex align-items-center justify-content-center">
                <Tooltip
                  html="Prepayment"
                  theme="transparent"
                  delay="150"
                  duration="250"
                  hideDelay="50"
                >
                  {/* &#x24C5; = hexadecimal circled letter P */}
                  <b>&#x24C5;</b>
                </Tooltip>
              </div>
            );
          }

          if (invoiceItem?.isCanceled) {
            return <RiCloseCircleLine size="23px" color="black" />;
          }

          return null;
        },
        label: '',
        defaultMessage: '',
        flexWidth: '0.25',
      },
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
        getDynamicAttr: (params) => ({
          formatValue: () => {
            const { invoiceItems } = params;
            const hasItems = invoiceItems;
            const isPrepLine = hasItems && invoiceItems[params.rowIndex]?.isPrepaymentItem;
            if (isPrepLine) {
              return params.fieldValue * (-1);
            }
            return params.fieldValue;
          },
        }),
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

const InvoicePrepayedItemsTable = ({
  invoiceItems, invoiceId, totalCount, loadMoreRows, isPrepaymentInvoice,
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
          isPrepaymentInvoice,
        }))}
    </div>
  );
};

InvoicePrepayedItemsTable.propTypes = {
  invoiceId: PropTypes.string.isRequired,
  isPrepaymentInvoice: PropTypes.bool.isRequired,
  invoiceItems: PropTypes.shape({}).isRequired,
  totalCount: PropTypes.number.isRequired,
  loadMoreRows: PropTypes.func.isRequired,
};

export default InvoicePrepayedItemsTable;
