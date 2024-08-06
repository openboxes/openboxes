import React, { useCallback, useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

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
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    getDynamicRowAttr: ({ rowValues }) => ({ className: rowValues && rowValues.totalAmount && rowValues.totalAmount < 0 ? 'negative-row-value' : '' }),
    fields: {
      prepaymentIcon: {
        type: (params) => {
          const { values } = params;
          const hasItems = values && values.invoiceItems;
          const isPrepLine = hasItems && (values.isPrepaymentInvoice
            || values.invoiceItems[params.rowIndex].isPrepaymentItem);
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
        getDynamicAttr: ({ values, rowIndex }) => {
          const orderId = values && values.invoiceItems
            && values.invoiceItems[rowIndex]
            && values.invoiceItems[rowIndex].orderId;
          return { url: orderId ? ORDER_URL.show(orderId) : '' };
        },
      },
      shipmentNumber: {
        type: LabelField,
        label: 'react.invoice.shipmentNumber.label',
        defaultMessage: 'Shipment Number',
        flexWidth: '1',
        getDynamicAttr: ({ values, rowIndex }) => {
          const shipmentId = values && values.invoiceItems
            && values.invoiceItems[rowIndex]
            && values.invoiceItems[rowIndex].shipmentId;
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
            const { values, rowIndex } = params;
            const rowValue = values?.invoiceItems?.[rowIndex];
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
            const { values } = params;
            const hasItems = values && values.invoiceItems;
            const isPrepLine = hasItems && values.invoiceItems[params.rowIndex].isPrepaymentItem;
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
      totalAmount: {
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

const InvoiceItemsTable = ({ values, loadMoreRows }) => {
  const [isFirstPageLoaded, setIsFirstPageLoaded] = useState(false);

  const isRowLoaded = useCallback(
    ({ index }) => !!values.invoiceItems[index],
    [values.invoiceItems],
  );

  const loadRows = (loadRowProps) => {
    if (!isFirstPageLoaded) {
      setIsFirstPageLoaded(true);
    }

    loadMoreRows(loadRowProps);
  };

  return (
    <div className="my-2 table-form">
      {_.map(INVOICE_ITEMS, (fieldConfig, fieldName) =>
        renderFormField(fieldConfig, fieldName, {
          values,
          totalCount: values.totalCount,
          loadMoreRows: loadRows,
          isRowLoaded,
          isFirstPageLoaded,
        }))}
    </div>
  );
};

InvoiceItemsTable.propTypes = {
  loadMoreRows: PropTypes.func.isRequired,
  values: PropTypes.shape({
    id: PropTypes.string.isRequired,
    invoiceItems: PropTypes.shape({}).isRequired,
    totalCount: PropTypes.number.isRequired,
  }).isRequired,
};

export default InvoiceItemsTable;
