import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { RiCopyrightLine } from 'react-icons/all';
import { Tooltip } from 'react-tippy';

import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import TextInput from 'components/form-elements/v2/TextInput';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import ContextMenu from 'utils/ContextMenu';
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
    getDynamicRowAttr: ({ rowValues }) => ({
      className: rowValues?.amount < 0 ? 'negative-row-value' : '',
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
            || invoiceItem?.inverse
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
            return <RiCopyrightLine size="23px" color="black" />;
          }

          return null;
        },
        label: '',
        defaultMessage: '',
        flexWidth: '0.25',
      },
      shipmentNumber: {
        type: LabelField,
        label: 'react.invoice.shipmentNumber.label',
        defaultMessage: 'Shipment Number',
        flexWidth: '1',
        getDynamicAttr: (params) => {
          const { invoiceItems, rowIndex } = params;
          const invoiceItem = invoiceItems[rowIndex];
          const shipmentId = invoiceItems
            && invoiceItems[rowIndex]
            && invoiceItems[rowIndex].shipmentId;
          return {
            url: shipmentId ? STOCK_MOVEMENT_URL.show(shipmentId) : '',
            formatValue: (value) => (invoiceItem?.inverse ? '' : value),
          };
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
        type: (params) => {
          const invoiceItem = params?.invoiceItems[params?.rowIndex];
          const errors = params.validate(invoiceItem);
          return (
            params.isEditable(invoiceItem?.id) && !invoiceItem?.orderAdjustment
              ? (
                <Tooltip
                  html={<div className="custom-tooltip">{errors}</div>}
                  theme="transparent"
                  disabled={!errors}
                >
                  <TextInput
                    type="number"
                    value={invoiceItem.quantity}
                    showErrorBorder={!!errors}
                    onChange={
                      params.updateInvoiceItemData(invoiceItem?.id, 'quantity')
                    }
                    {...params}
                  />
                </Tooltip>
              )
              : <LabelField {...params} />
          );
        },
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
        type: (params) => {
          const invoiceItem = params?.invoiceItems[params?.rowIndex];
          const errors = params.validate(invoiceItem);
          return (
            params.isEditable(invoiceItem?.id) && invoiceItem?.orderAdjustment
              ? (
                <Tooltip
                  html={<div className="custom-tooltip">{errors}</div>}
                  theme="transparent"
                  disabled={!errors}
                >
                  <TextInput
                    type="number"
                    value={invoiceItem.unitPrice}
                    showErrorBorder={!!errors}
                    onChange={
                      params.updateInvoiceItemData(invoiceItem?.id, 'unitPrice')
                    }
                    {...params}
                  />
                </Tooltip>
              )
              : <LabelField {...params} />
          );
        },
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
      actionDots: {
        type: (params) => {
          const invoiceItem = params?.invoiceItems?.[params.rowIndex];
          const canUseActionDots = params.isActionMenuVisible(
            params.invoiceStatus,
            invoiceItem?.inverse,
            params.isPrepaymentInvoice,
          );
          if (canUseActionDots) {
            return (
              <ContextMenu
                positions={['left']}
                actions={params.actions(invoiceItem)}
                id={params?.rowIndex}
              />
            );
          }

          return null;
        },
        flexWidth: '1',
      },
    },
  },
};

const InvoicePrepayedItemsTable = ({
  invoiceItems,
  updateInvoiceItemData,
  invoiceId,
  totalCount,
  loadMoreRows,
  isPrepaymentInvoice,
  invoicePrepaidItemsTableData,
  invoiceStatus,
}) => {
  const {
    actions,
    isRowLoaded,
    isEditable,
    editableRows,
    validate,
    isActionMenuVisible,
  } = invoicePrepaidItemsTableData;

  return (
    <div className="my-2 table-form prepayment-invoice-table">
      {_.map(INVOICE_ITEMS, (fieldConfig, fieldName) =>
        renderFormField(fieldConfig, fieldName, {
          invoiceId,
          invoiceItems,
          totalCount,
          loadMoreRows,
          isRowLoaded,
          isPrepaymentInvoice,
          editableRows,
          updateInvoiceItemData,
          validate,
          isEditable,
          actions,
          invoiceStatus,
          isActionMenuVisible,
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
  updateInvoiceItemData: PropTypes.func.isRequired,
  invoicePrepaidItemsTableData: PropTypes.shape({
    actions: PropTypes.arrayOf(
      PropTypes.shape({}),
    ).isRequired,
    isRowLoaded: PropTypes.func.isRequired,
    isEditable: PropTypes.func.isRequired,
    editableRows: PropTypes.arrayOf(
      PropTypes.shape({}),
    ).isRequired,
    validate: PropTypes.func.isRequired,
    isActionMenuVisible: PropTypes.func.isRequired,
  }).isRequired,
  invoiceStatus: PropTypes.string.isRequired,
};

export default InvoicePrepayedItemsTable;
