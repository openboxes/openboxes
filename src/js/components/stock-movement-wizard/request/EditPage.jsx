import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import update from 'immutability-helper';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';
import { Tooltip } from 'react-tippy';

import { fetchReasonCodes, hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import LabelField from 'components/form-elements/LabelField';
import SelectField from 'components/form-elements/SelectField';
import TableRowWithSubfields from 'components/form-elements/TableRowWithSubfields';
import TextField from 'components/form-elements/TextField';
import DetailsModal from 'components/stock-movement-wizard/modals/DetailsModal';
import SubstitutionsModal from 'components/stock-movement-wizard/modals/SubstitutionsModal';
import apiClient from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import { formatProductDisplayName, showOutboundEditValidationErrors } from 'utils/form-values-utils';
import renderHandlingIcons from 'utils/product-handling-icons';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


const BTN_CLASS_MAPPER = {
  YES: 'btn btn-outline-success',
  NO: 'btn btn-outline-secondary',
  EARLIER: 'btn btn-outline-warning',
  HIDDEN: 'btn invisible',
};

const AD_HOCK_FIELDS = {
  editPageItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    rowComponent: TableRowWithSubfields,
    getDynamicRowAttr: ({ rowValues, showOnlyErroredItems, itemFilter }) => {
      let className = rowValues.statusCode === 'SUBSTITUTED' ? 'crossed-out ' : '';
      if (rowValues.quantityAvailable < rowValues.quantityRequested) {
        className += 'font-weight-bold';
      }
      const filterOutItems = itemFilter && !(
        rowValues.product.name.toLowerCase().includes(itemFilter.toLowerCase()) ||
        rowValues.productCode.toLowerCase().includes(itemFilter.toLowerCase())
      );
      const hideRow = (showOnlyErroredItems && !rowValues.hasError) || filterOutItems;
      return { className, hideRow };
    },
    subfieldKey: 'substitutionItems',
    headerGroupings: {
      requestInformation: {
        label: 'react.verifyRequest.requestInformation.label',
        defaultLabel: 'Request Information',
        flexWidth: 0.5 + 3 + 1 + 1 + 1, // = Sum of fields flexWidth
      },
      availability: {
        label: 'react.verifyRequest.availability.label',
        defaultLabel: 'Availability',
        flexWidth: 1 + 1 + 1 + 1, // = Sum of fields flexWidth
      },
      edit: {
        label: 'react.verifyRequest.edit.label',
        defaultLabel: 'Edit',
        flexWidth: 1 + 1 + 1 + 0.5, // = Sum of fields flexWidth
      },
    },
    fields: {
      productCode: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '0.5',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
        attributes: {
          showValueTooltip: true,
        },
      },
      product: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '3',
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
        attributes: {
          formatValue: value => (
            <Tooltip
              html={<div className="text-truncate">{value.name}</div>}
              theme="dark"
              disabled={!value.displayNames?.default}
              position="top-start"
            >
              <span className="d-flex align-items-center">
                <span className="text-truncate">
                  {value.displayNames?.default ?? value.name}
                </span>
                {renderHandlingIcons(value ? value.handlingIcons : null)}
              </span>
            </Tooltip>
          ),
        },
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      quantityOnHandRequesting: {
        type: LabelField,
        label: 'react.stockMovement.requesterQuantityOnHand.label',
        defaultMessage: 'Requester QOH',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityDemandRequesting: {
        type: LabelField,
        label: 'react.stockMovement.quantityDemand.label',
        defaultMessage: 'Demand',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityRequested: {
        type: (params) => {
          const { fieldName, values } = params;
          const fieldNameParts = _.split(fieldName, '.');
          if (fieldNameParts.length === 2) {
            const rowIdx = fieldNameParts[0];
            const rowValues = _.get(values, rowIdx);
            if (rowValues.comments) {
              return (
                <div className="d-flex align-items-center">
                  {/* flex: 1 to center qty label, marginLeft: 14px to mitigate icon font size */}
                  <div style={{ flex: 1, marginLeft: '14px' }}><LabelField {...params} /></div>
                  <Tooltip
                    html={rowValues.comments}
                    theme="transparent"
                    delay="150"
                    duration="250"
                    hideDelay="50"
                  >
                    <i className="fa fa-sticky-note pr-2" />
                  </Tooltip>
                </div>
              );
            }
          }
          return <LabelField {...params} />;
        },
        label: 'react.verifyRequest.quantityRequested.label',
        defaultMessage: 'Qty Requested',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityOnHand: {
        type: LabelField,
        label: 'react.stockMovement.quantityOnHand.label',
        defaultMessage: 'QOH',
        flexWidth: '1',
        fieldKey: '',
        headerClassName: 'left-border',
        attributes: {
          formatValue: value => (value.quantityOnHand ? (value.quantityOnHand.toLocaleString('en-US')) : value.quantityOnHand),
          numberField: true,
          className: 'left-border',
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.available.label',
        defaultMessage: 'Available',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue }) => {
          let className = '';
          if (fieldValue && (!fieldValue.quantityAvailable ||
              fieldValue.quantityAvailable < fieldValue.quantityRequested)) {
            className += 'text-danger';
          }
          return {
            className,
          };
        },
        attributes: {
          formatValue: value => (value.quantityAvailable ? (value.quantityAvailable.toLocaleString('en-US')) : value.quantityAvailable),
          numberField: true,
        },
      },
      quantityDemandFulfilling: {
        type: LabelField,
        label: 'react.stockMovement.demandPerMonth.label',
        defaultMessage: 'Demand per Month',
        flexWidth: '1',
        getDynamicAttr: () => ({
          formatValue: (value) => {
            if (value && value !== '0') {
              return value.toLocaleString('en-US');
            }

            return '0';
          },
          showValueTooltip: true,
        }),
        attributes: {
          numberField: true,
        },
      },
      detailsButton: {
        label: 'react.stockMovement.details.label',
        defaultMessage: 'Details',
        type: DetailsModal,
        flexWidth: '1',
        fieldKey: '',
        attributes: {
          title: 'react.stockMovement.pendingRequisitionDetails.label',
          defaultTitleMessage: 'Pending Requisition Details',
        },
        getDynamicAttr: ({ fieldValue, stockMovementId, values }) => ({
          productId: fieldValue && fieldValue.product && fieldValue.product.id,
          productCode: fieldValue && fieldValue.product && fieldValue.product.productCode,
          productName: fieldValue && fieldValue.product && fieldValue.product.name,
          displayName: fieldValue?.product?.displayNames?.default,
          originId: values && values.origin && values.origin.id,
          stockMovementId,
          quantityRequested: fieldValue && fieldValue.quantityRequested,
          quantityOnHand: fieldValue && fieldValue.quantityOnHand,
          quantityAvailable: fieldValue && fieldValue.quantityAvailable,
          btnOpenText: 'react.stockMovement.details.label',
          btnOpenDefaultText: 'Details',
          btnCancelText: 'Close',
          btnSaveStyle: { display: 'none' },
          btnContainerClassName: 'float-right',
          btnOpenAsIcon: true,
          btnOpenStyle: { border: 'none', cursor: 'pointer' },
          btnOpenIcon: 'fa-search',
        }),
      },
      substituteButton: {
        label: 'react.stockMovement.substitution.label',
        defaultMessage: 'Substitution',
        type: SubstitutionsModal,
        fieldKey: '',
        flexWidth: '1',
        headerClassName: 'left-border',
        attributes: {
          cellClassName: 'left-border',
          title: 'react.stockMovement.substitutes.label',
          defaultTitleMessage: 'Substitutes',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, stockMovementId, onResponse,
          reviseRequisitionItems, values, reasonCodes, showOnly,
        }) => ({
          onOpen: () => reviseRequisitionItems(values),
          productCode: fieldValue && fieldValue.productCode,
          btnOpenText: `react.stockMovement.${fieldValue && fieldValue.substitutionStatus}.label`,
          btnOpenDefaultText: `${fieldValue && fieldValue.substitutionStatus}`,
          btnOpenDisabled: (fieldValue && fieldValue.statusCode === 'SUBSTITUTED') || showOnly,
          btnOpenClassName: BTN_CLASS_MAPPER[(fieldValue && fieldValue.substitutionStatus) || 'HIDDEN'],
          rowIndex,
          lineItem: fieldValue,
          stockMovementId,
          onResponse,
          reasonCodes,
        }),
      },
      quantityRevised: {
        label: 'react.stockMovement.quantityRevised.label',
        defaultMessage: 'Qty revised',
        type: TextField,
        fieldKey: 'statusCode',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          fieldValue, subfield, showOnly, updateRow, values, rowIndex,
        }) => ({
          disabled: (fieldValue && fieldValue === 'SUBSTITUTED') || subfield || showOnly,
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      reasonCode: {
        type: SelectField,
        label: 'react.stockMovement.reasonCode.label',
        defaultMessage: 'Reason code',
        flexWidth: '1',
        fieldKey: 'quantityRevised',
        getDynamicAttr: ({
          fieldValue, subfield, reasonCodes, updateRow, values, rowIndex, showOnly,
        }) => {
          const isSubstituted = fieldValue && fieldValue.statusCode === 'SUBSTITUTED';
          return {
            disabled: fieldValue === null ||
              fieldValue === undefined ||
              subfield ||
              isSubstituted ||
              showOnly,
            options: reasonCodes,
            showValueTooltip: true,
            onBlur: () => updateRow(values, rowIndex),
          };
        },
      },
      revert: {
        type: ButtonField,
        label: 'react.default.button.undo.label',
        defaultMessage: 'Undo',
        flexWidth: '0.5',
        fieldKey: '',
        buttonLabel: 'react.default.button.undo.label',
        buttonDefaultMessage: 'Undo',
        getDynamicAttr: ({
          fieldValue, revertItem, values, showOnly,
        }) => ({
          onClick: fieldValue && fieldValue.requisitionItemId ?
            () => revertItem(values, fieldValue.requisitionItemId) : () => null,
          hidden: fieldValue && fieldValue.statusCode ? !_.includes(['CHANGED', 'CANCELED'], fieldValue.statusCode) : false,
          disabled: showOnly,
        }),
        attributes: {
          className: 'btn btn-outline-danger',
        },
      },
    },
  },
};

const STOCKLIST_FIELDS_PUSH_TYPE = {
  editPageItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    rowComponent: TableRowWithSubfields,
    getDynamicRowAttr: ({ rowValues, showOnlyErroredItems, itemFilter }) => {
      let className = rowValues.statusCode === 'SUBSTITUTED' ? 'crossed-out ' : '';
      if (rowValues.quantityAvailable < rowValues.quantityRequested) {
        className += 'font-weight-bold';
      }
      const filterOutItems = itemFilter && !(
        rowValues.product.name.toLowerCase().includes(itemFilter.toLowerCase()) ||
        rowValues.productCode.toLowerCase().includes(itemFilter.toLowerCase())
      );
      const hideRow = (showOnlyErroredItems && !rowValues.hasError) || filterOutItems;
      return { className, hideRow };
    },
    subfieldKey: 'substitutionItems',
    headerGroupings: {
      requestInformation: {
        label: 'react.verifyRequest.requestInformation.label',
        defaultLabel: 'Request Information',
        flexWidth: 0.5 + 3 + 1 + 1 + 1, // = Sum of fields flexWidth
      },
      availability: {
        label: 'react.verifyRequest.availability.label',
        defaultLabel: 'Availability',
        flexWidth: 1 + 1 + 1 + 1, // = Sum of fields flexWidth
      },
      edit: {
        label: 'react.verifyRequest.edit.label',
        defaultLabel: 'Edit',
        flexWidth: 1 + 1 + 1 + 0.5, // = Sum of fields flexWidth
      },
    },
    fields: {
      productCode: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '0.5',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
        attributes: {
          showValueTooltip: true,
        },
      },
      product: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '3',
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
        attributes: {
          formatValue: formatProductDisplayName,
        },
        getDynamicAttr: ({ subfield, fieldValue }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
          showValueTooltip: !!fieldValue?.displayNames?.default,
          tooltipValue: fieldValue?.name,
        }),
      },
      quantityOnStocklist: {
        type: LabelField,
        label: 'react.stockMovement.quantityOnStocklist.label',
        defaultMessage: 'Stocklist QTY',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityOnHandRequesting: {
        type: LabelField,
        label: 'react.stockMovement.requesterQuantityOnHand.label',
        defaultMessage: 'Requester QOH',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityRequested: {
        type: (params) => {
          const { fieldName, values } = params;
          const fieldNameParts = _.split(fieldName, '.');
          if (fieldNameParts.length === 2) {
            const rowIdx = fieldNameParts[0];
            const rowValues = _.get(values, rowIdx);
            if (rowValues.comments) {
              return (
                <div className="d-flex align-items-center">
                  {/* flex: 1 to center qty label, marginLeft: 14px to mitigate icon font size */}
                  <div style={{ flex: 1, marginLeft: '14px' }}><LabelField {...params} /></div>
                  <Tooltip
                    html={rowValues.comments}
                    theme="transparent"
                    delay="150"
                    duration="250"
                    hideDelay="50"
                  >
                    <i className="fa fa-sticky-note pr-2" />
                  </Tooltip>
                </div>
              );
            }
          }
          return <LabelField {...params} />;
        },
        label: 'react.verifyRequest.quantityRequested.label',
        defaultMessage: 'Qty Requested',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityOnHand: {
        type: LabelField,
        label: 'react.stockMovement.quantityOnHand.label',
        defaultMessage: 'QOH',
        flexWidth: '1',
        fieldKey: '',
        headerClassName: 'left-border',
        attributes: {
          formatValue: value => (value.quantityOnHand ? (value.quantityOnHand.toLocaleString('en-US')) : value.quantityOnHand),
          numberField: true,
          className: 'left-border',
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.available.label',
        defaultMessage: 'Available',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue }) => {
          let className = '';
          if (fieldValue && (!fieldValue.quantityAvailable ||
              fieldValue.quantityAvailable < fieldValue.quantityRequested)) {
            className += 'text-danger';
          }
          return {
            className,
          };
        },
        attributes: {
          formatValue: value => (value.quantityAvailable ? (value.quantityAvailable.toLocaleString('en-US')) : value.quantityAvailable),
          numberField: true,
        },
      },
      quantityDemandFulfilling: {
        type: LabelField,
        label: 'react.stockMovement.demandPerMonth.label',
        defaultMessage: 'Demand per Month',
        flexWidth: '1',
        getDynamicAttr: () => ({
          formatValue: (value) => {
            if (value && value !== '0') {
              return value.toLocaleString('en-US');
            }

            return '0';
          },
          showValueTooltip: true,
        }),
        attributes: {
          numberField: true,
        },
      },
      detailsButton: {
        label: 'react.stockMovement.details.label',
        defaultMessage: 'Details',
        type: DetailsModal,
        flexWidth: '1',
        fieldKey: '',
        attributes: {
          title: 'react.stockMovement.pendingRequisitionDetails.label',
          defaultTitleMessage: 'Pending Requisition Details',
        },
        getDynamicAttr: ({ fieldValue, stockMovementId, values }) => ({
          productId: fieldValue && fieldValue.product && fieldValue.product.id,
          productCode: fieldValue && fieldValue.product && fieldValue.product.productCode,
          productName: fieldValue && fieldValue.product && fieldValue.product.name,
          displayName: fieldValue?.product?.displayNames?.default,
          originId: values && values.origin && values.origin.id,
          stockMovementId,
          quantityRequested: fieldValue && fieldValue.quantityRequested,
          quantityOnHand: fieldValue && fieldValue.quantityOnHand,
          quantityAvailable: fieldValue && fieldValue.quantityAvailable,
          btnOpenText: 'react.stockMovement.details.label',
          btnOpenDefaultText: 'Details',
          btnCancelText: 'Close',
          btnSaveStyle: { display: 'none' },
          btnContainerClassName: 'float-right',
          btnOpenAsIcon: true,
          btnOpenStyle: { border: 'none', cursor: 'pointer' },
          btnOpenIcon: 'fa-search',
        }),
      },
      substituteButton: {
        label: 'react.stockMovement.substitution.label',
        defaultMessage: 'Substitution',
        type: SubstitutionsModal,
        fieldKey: '',
        flexWidth: '1',
        headerClassName: 'left-border',
        attributes: {
          cellClassName: 'left-border',
          title: 'react.stockMovement.substitutes.label',
          defaultTitleMessage: 'Substitutes',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, stockMovementId, onResponse,
          reviseRequisitionItems, values, reasonCodes, showOnly,
        }) => ({
          onOpen: () => reviseRequisitionItems(values),
          productCode: fieldValue && fieldValue.productCode,
          btnOpenText: `react.stockMovement.${fieldValue && fieldValue.substitutionStatus}.label`,
          btnOpenDefaultText: `${fieldValue && fieldValue.substitutionStatus}`,
          btnOpenDisabled: (fieldValue && fieldValue.statusCode === 'SUBSTITUTED') || showOnly,
          btnOpenClassName: BTN_CLASS_MAPPER[(fieldValue && fieldValue.substitutionStatus) || 'HIDDEN'],
          rowIndex,
          lineItem: fieldValue,
          stockMovementId,
          onResponse,
          reasonCodes,
        }),
      },
      quantityRevised: {
        label: 'react.stockMovement.quantityRevised.label',
        defaultMessage: 'Qty revised',
        type: TextField,
        fieldKey: 'statusCode',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          fieldValue, subfield, showOnly, updateRow, values, rowIndex,
        }) => ({
          disabled: (fieldValue && fieldValue === 'SUBSTITUTED') || subfield || showOnly,
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      reasonCode: {
        type: SelectField,
        label: 'react.stockMovement.reasonCode.label',
        defaultMessage: 'Reason code',
        flexWidth: '1',
        fieldKey: 'quantityRevised',
        getDynamicAttr: ({
          fieldValue, subfield, reasonCodes, updateRow, values, rowIndex, showOnly,
        }) => {
          const isSubstituted = fieldValue && fieldValue.statusCode === 'SUBSTITUTED';
          return {
            disabled: fieldValue === null ||
              fieldValue === undefined ||
              subfield ||
              isSubstituted ||
              showOnly,
            options: reasonCodes,
            showValueTooltip: true,
            onBlur: () => updateRow(values, rowIndex),
          };
        },
      },
      revert: {
        type: ButtonField,
        label: 'react.default.button.undo.label',
        defaultMessage: 'Undo',
        flexWidth: '0.5',
        fieldKey: '',
        buttonLabel: 'react.default.button.undo.label',
        buttonDefaultMessage: 'Undo',
        getDynamicAttr: ({
          fieldValue, revertItem, values, showOnly,
        }) => ({
          onClick: fieldValue && fieldValue.requisitionItemId ?
            () => revertItem(values, fieldValue.requisitionItemId) : () => null,
          hidden: fieldValue && fieldValue.statusCode ? !_.includes(['CHANGED', 'CANCELED'], fieldValue.statusCode) : false,
          disabled: showOnly,
        }),
        attributes: {
          className: 'btn btn-outline-danger',
        },
      },
    },
  },
};

const STOCKLIST_FIELDS_PULL_TYPE = {
  editPageItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    rowComponent: TableRowWithSubfields,
    getDynamicRowAttr: ({ rowValues, showOnlyErroredItems, itemFilter }) => {
      let className = rowValues.statusCode === 'SUBSTITUTED' ? 'crossed-out ' : '';
      if (rowValues.quantityAvailable < rowValues.quantityRequested) {
        className += 'font-weight-bold';
      }
      const filterOutItems = itemFilter && !(
        rowValues.product.name.toLowerCase().includes(itemFilter.toLowerCase()) ||
        rowValues.productCode.toLowerCase().includes(itemFilter.toLowerCase())
      );
      const hideRow = (showOnlyErroredItems && !rowValues.hasError) || filterOutItems;
      return { className, hideRow };
    },
    subfieldKey: 'substitutionItems',
    headerGroupings: {
      requestInformation: {
        label: 'react.verifyRequest.requestInformation.label',
        defaultLabel: 'Request Information',
        flexWidth: 0.5 + 3.5 + 1 + 1 + 1, // = Sum of fields flexWidth
      },
      availability: {
        label: 'react.verifyRequest.availability.label',
        defaultLabel: 'Availability',
        flexWidth: 1 + 1 + 1 + 1, // = Sum of fields flexWidth
      },
      edit: {
        label: 'react.verifyRequest.edit.label',
        defaultLabel: 'Edit',
        flexWidth: 1 + 1 + 1.5 + 0.5, // = Sum of fields flexWidth
      },
    },
    fields: {
      productCode: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '0.5',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
        attributes: {
          showValueTooltip: true,
        },
      },
      product: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '3.5',
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
        attributes: {
          formatValue: formatProductDisplayName,
        },
        getDynamicAttr: ({ subfield, fieldValue }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
          showValueTooltip: !!fieldValue?.displayNames?.default,
          tooltipValue: fieldValue?.name,
        }),
      },
      demandPerReplenishmentPeriod: {
        type: LabelField,
        label: 'react.stockMovement.demandPerReplenishmentPeriod.label',
        defaultMessage: 'Demand per Replenishment Period',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityOnHandRequesting: {
        type: LabelField,
        label: 'react.stockMovement.requesterQuantityOnHand.label',
        defaultMessage: 'Requester QOH',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityRequested: {
        type: (params) => {
          const { fieldName, values } = params;
          const fieldNameParts = _.split(fieldName, '.');
          if (fieldNameParts.length === 2) {
            const rowIdx = fieldNameParts[0];
            const rowValues = _.get(values, rowIdx);
            if (rowValues.comments) {
              return (
                <div className="d-flex align-items-center">
                  {/* flex: 1 to center qty label, marginLeft: 14px to mitigate icon font size */}
                  <div style={{ flex: 1, marginLeft: '14px' }}><LabelField {...params} /></div>
                  <Tooltip
                    html={rowValues.comments}
                    theme="transparent"
                    delay="150"
                    duration="250"
                    hideDelay="50"
                  >
                    <i className="fa fa-sticky-note pr-2" />
                  </Tooltip>
                </div>
              );
            }
          }
          return <LabelField {...params} />;
        },
        label: 'react.verifyRequest.quantityRequested.label',
        defaultMessage: 'Qty Requested',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
          numberField: true,
        },
      },
      quantityOnHand: {
        type: LabelField,
        label: 'react.stockMovement.quantityOnHand.label',
        defaultMessage: 'QOH',
        flexWidth: '1',
        fieldKey: '',
        headerClassName: 'left-border',
        attributes: {
          formatValue: value => (value.quantityOnHand ? (value.quantityOnHand.toLocaleString('en-US')) : value.quantityOnHand),
          numberField: true,
          className: 'left-border',
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.available.label',
        defaultMessage: 'Available',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue }) => {
          let className = '';
          if (fieldValue && (!fieldValue.quantityAvailable ||
              fieldValue.quantityAvailable < fieldValue.quantityRequested)) {
            className += 'text-danger';
          }
          return {
            className,
          };
        },
        attributes: {
          formatValue: value => (value.quantityAvailable ? (value.quantityAvailable.toLocaleString('en-US')) : value.quantityAvailable),
          numberField: true,
        },
      },
      quantityDemandFulfilling: {
        type: LabelField,
        label: 'react.stockMovement.demandPerMonth.labe',
        defaultMessage: 'Demand per Month',
        flexWidth: '1',
        getDynamicAttr: () => ({
          formatValue: (value) => {
            if (value && value !== '0') {
              return value.toLocaleString('en-US');
            }

            return '0';
          },
          showValueTooltip: true,
        }),
        attributes: {
          numberField: true,
        },
      },
      detailsButton: {
        label: 'react.stockMovement.details.label',
        defaultMessage: 'Details',
        type: DetailsModal,
        fieldKey: '',
        flexWidth: '1',
        attributes: {
          title: 'react.stockMovement.pendingRequisitionDetails.label',
          defaultTitleMessage: 'Pending Requisition Details',
        },
        getDynamicAttr: ({ fieldValue, stockMovementId, values }) => ({
          productId: fieldValue && fieldValue.product && fieldValue.product.id,
          productCode: fieldValue && fieldValue.product && fieldValue.product.productCode,
          productName: fieldValue && fieldValue.product && fieldValue.product.name,
          displayName: fieldValue?.product?.displayNames?.default,
          originId: values && values.origin && values.origin.id,
          stockMovementId,
          quantityRequested: fieldValue && fieldValue.quantityRequested,
          quantityOnHand: fieldValue && fieldValue.quantityOnHand,
          quantityAvailable: fieldValue && fieldValue.quantityAvailable,
          btnOpenText: 'react.stockMovement.details.label',
          btnOpenDefaultText: 'Details',
          btnCancelText: 'Close',
          btnSaveStyle: { display: 'none' },
          btnContainerClassName: 'float-right',
          btnOpenAsIcon: true,
          btnOpenStyle: { border: 'none', cursor: 'pointer' },
          btnOpenIcon: 'fa-search',
        }),
      },
      substituteButton: {
        label: 'react.stockMovement.substitution.label',
        defaultMessage: 'Substitution',
        type: SubstitutionsModal,
        fieldKey: '',
        flexWidth: '1',
        headerClassName: 'left-border',
        attributes: {
          cellClassName: 'left-border',
          title: 'react.stockMovement.substitutes.label',
          defaultTitleMessage: 'Substitutes',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, stockMovementId, onResponse,
          reviseRequisitionItems, values, reasonCodes, showOnly,
        }) => ({
          onOpen: () => reviseRequisitionItems(values),
          productCode: fieldValue && fieldValue.productCode,
          btnOpenText: `react.stockMovement.${fieldValue && fieldValue.substitutionStatus}.label`,
          btnOpenDefaultText: `${fieldValue && fieldValue.substitutionStatus}`,
          btnOpenDisabled: (fieldValue && fieldValue.statusCode === 'SUBSTITUTED') || showOnly,
          btnOpenClassName: BTN_CLASS_MAPPER[(fieldValue && fieldValue.substitutionStatus) || 'HIDDEN'],
          rowIndex,
          lineItem: fieldValue,
          stockMovementId,
          onResponse,
          reasonCodes,
        }),
      },
      quantityRevised: {
        label: 'react.stockMovement.quantityRevised.label',
        defaultMessage: 'Qty revised',
        type: TextField,
        fieldKey: 'statusCode',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          fieldValue, subfield, showOnly, updateRow, values, rowIndex,
        }) => ({
          disabled: (fieldValue && fieldValue === 'SUBSTITUTED') || subfield || showOnly,
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      reasonCode: {
        type: SelectField,
        label: 'react.stockMovement.reasonCode.label',
        defaultMessage: 'Reason code',
        flexWidth: '1.5',
        fieldKey: 'quantityRevised',
        getDynamicAttr: ({
          fieldValue, subfield, reasonCodes, updateRow, values, rowIndex, showOnly,
        }) => {
          const isSubstituted = fieldValue && fieldValue.statusCode === 'SUBSTITUTED';
          return {
            disabled: fieldValue === null ||
              fieldValue === undefined ||
              subfield ||
              isSubstituted ||
              showOnly,
            options: reasonCodes,
            showValueTooltip: true,
            onBlur: () => updateRow(values, rowIndex),
          };
        },
      },
      revert: {
        type: ButtonField,
        label: 'react.default.button.undo.label',
        defaultMessage: 'Undo',
        flexWidth: '0.5',
        fieldKey: '',
        buttonLabel: 'react.default.button.undo.label',
        buttonDefaultMessage: 'Undo',
        getDynamicAttr: ({
          fieldValue, revertItem, values, showOnly,
        }) => ({
          onClick: fieldValue && fieldValue.requisitionItemId ?
            () => revertItem(values, fieldValue.requisitionItemId) : () => null,
          hidden: fieldValue && fieldValue.statusCode ? !_.includes(['CHANGED', 'CANCELED'], fieldValue.statusCode) : false,
          disabled: showOnly,
        }),
        attributes: {
          className: 'btn btn-outline-danger',
        },
      },
    },
  },
};

const REPLENISHMENT_TYPE_PULL = 'PULL';

function validateForSave(values) {
  const errors = {};
  errors.editPageItems = [];

  _.forEach(values.editPageItems, (item, key) => {
    if (!_.isEmpty(item.quantityRevised) && _.isEmpty(item.reasonCode)) {
      errors.editPageItems[key] = { reasonCode: 'react.stockMovement.errors.reasonCodeRequired.label' };
    } else if (_.isNil(item.quantityRevised) && !_.isEmpty(item.reasonCode) && item.statusCode !== 'SUBSTITUTED') {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.revisedQuantityRequired.label' };
    }
    if (parseInt(item.quantityRevised, 10) === item.quantityRequested) {
      errors.editPageItems[key] = {
        quantityRevised: 'react.stockMovement.errors.sameRevisedQty.label',
      };
    }
    if (!_.isEmpty(item.quantityRevised) && item.quantityAvailable >= 0 &&
      (item.quantityRevised > item.quantityAvailable)) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.higherQty.label' };
    }
    if (!_.isEmpty(item.quantityRevised) && (item.quantityRevised < 0)) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.negativeQty.label' };
    }
  });
  return errors;
}

/**
 * The third step of stock movement(for stock requests) where user can see the
 * stock available and adjust quantities or make substitutions based on that information.
 * or validate the current status of a request
 */
class EditItemsPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      statusCode: '',
      revisedItems: [],
      values: { ...this.props.initialValues, editPageItems: [] },
      hasItemsLoaded: false,
      totalCount: 0,
      isFirstPageLoaded: false,
      showOnlyErroredItems: false,
      itemFilter: '',
    };

    this.revertItem = this.revertItem.bind(this);
    this.fetchEditPageItems = this.fetchEditPageItems.bind(this);
    this.reviseRequisitionItems = this.reviseRequisitionItems.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateRow = this.updateRow.bind(this);
    this.markErroredLines = this.markErroredLines.bind(this);
    this.validate = this.validate.bind(this);
    this.props.showSpinner();
  }

  componentDidMount() {
    if (this.props.stockMovementTranslationsFetched) {
      this.dataFetched = true;

      this.fetchAllData(false);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchAllData(false);
    }

    // If we change the language, refetch the reason codes
    if (nextProps.currentLocale !== this.props.currentLocale) {
      this.props.fetchReasonCodes();
    }
  }

  setEditPageItems(response, startIndex) {
    this.props.showSpinner();
    const { data } = response.data;
    const editPageItems = _.map(
      data,
      val => ({
        ...val,
        disabled: true,
        quantityOnHand: val.quantityOnHand > 0 ? val.quantityOnHand : 0,
        quantityAvailable:
            val.quantityAvailable > 0 ? val.quantityAvailable : 0,
        product: {
          ...val.product,
          label: `${val.productCode} ${val.productName}`,
        },
        // eslint-disable-next-line max-len
        reasonCode: _.find(this.props.reasonCodes, ({ value }) => _.includes(val.reasonCode, value)),
        substitutionItems: _.map(val.substitutionItems, sub => ({
          ...sub,
          requisitionItemId: val.requisitionItemId,
          product: {
            ...sub.product,
            label: `${sub.productCode} ${sub.productName}`,
          },
          // eslint-disable-next-line max-len
          reasonCode: _.find(this.props.reasonCodes, ({ value }) => _.includes(val.reasonCode, value)),
        })),
      }),
    );

    this.setState({
      revisedItems: _.filter(editPageItems, item => item.statusCode === 'CHANGED'),
      values: {
        ...this.state.values,
        editPageItems: _.uniqBy(_.concat(this.state.values.editPageItems, editPageItems), 'requisitionItemId'),
      },
      hasItemsLoaded: this.state.hasItemsLoaded
      || this.state.totalCount === _.uniqBy(_.concat(this.state.values.editPageItems, editPageItems), 'requisitionItemId').length,
    }, () => {
      // eslint-disable-next-line max-len
      if (!_.isNull(startIndex) && this.state.values.editPageItems.length !== this.state.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      }
      this.props.hideSpinner();
    });
  }

  getFields() {
    if (_.get(this.state.values.stocklist, 'id')) {
      if (_.get(this.state.values.replenishmentType, 'name') === REPLENISHMENT_TYPE_PULL) {
        return STOCKLIST_FIELDS_PULL_TYPE;
      }
      return STOCKLIST_FIELDS_PUSH_TYPE;
    }

    return AD_HOCK_FIELDS;
  }

  validate(values) {
    const errors = validateForSave(values);

    _.forEach(values.editPageItems, (item, key) => {
      if (_.isNil(item.quantityRevised) && (item.quantityRequested > item.quantityAvailable) && (item.statusCode !== 'SUBSTITUTED')) {
        errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.lowerQty.label' };
      }
    });

    this.markErroredLines(values, errors);

    return errors;
  }

  markErroredLines(values, errors) {
    let updatedValues = values;

    _.forEach(this.state.values.editPageItems, (item, itemIdx) => {
      updatedValues = update(updatedValues, {
        editPageItems: {
          [itemIdx]: {
            hasError: {
              $set: !!_.find(errors.editPageItems, (error, errorIdx) => itemIdx === errorIdx),
            },
          },
        },
      });
    });

    this.setState({
      values: updatedValues,
      showOnlyErroredItems: !errors.editPageItems.length ? false : this.state.showOnlyErroredItems,
    });
  }

  dataFetched = false;

  /**
   * Fetches all required data.
   * @param {boolean} forceFetch
   * @public
   */
  fetchAllData(forceFetch) {
    this.props.showSpinner();
    // TODO: When having full React, fetch only if not fetched yet or language changed
    this.props.fetchReasonCodes();

    this.fetchEditPageData().then((resp) => {
      const { statusCode } = resp.data.data;
      const { totalCount } = resp.data;

      this.setState({
        statusCode,
        totalCount,
      }, () => {
        if (!this.props.isPaginated || forceFetch) {
          this.fetchItems();
        }
      });
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  fetchItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=3`;
    apiClient.get(url)
      .then((response) => {
        this.setEditPageItems(response, null);
        this.setState({
          hasItemsLoaded: true,
        });
      });
  }

  fetchEditPageItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=3`;
    apiClient.get(url)
      .then((response) => {
        const { data } = response.data;
        this.setState({
          hasItemsLoaded: true,
          values: {
            ...this.state.values,
            editPageItems: _.map(data, item => ({
              ...item,
              quantityOnHand: item.quantityOnHand || 0,
              // eslint-disable-next-line max-len
              reasonCode: _.find(this.props.reasonCodes, ({ value }) => _.includes(item.reasonCode, value)),
              substitutionItems: _.map(item.substitutionItems, sub => ({
                ...sub,
                requisitionItemId: item.requisitionItemId,
                // eslint-disable-next-line max-len
                reasonCode: _.find(this.props.reasonCodes, ({ value }) => _.includes(item.reasonCode, value)),
              })),
            })),
          },
        }, () => {
          this.fetchAllData(false);
          this.props.hideSpinner();
        });
      }).catch(() => {
        this.props.hideSpinner();
      });
  }

  loadMoreRows({ startIndex }) {
    if (this.state.totalCount) {
      this.setState({
        isFirstPageLoaded: true,
      });
      const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${this.props.pageSize}&stepNumber=3`;
      apiClient.get(url)
        .then((response) => {
          this.setEditPageItems(response, startIndex);
        });
    }
  }

  isRowLoaded({ index }) {
    return !!this.state.values.editPageItems[index];
  }

  /**
   * Sends data of revised items with post method.
   * @param {object} values
   * @public
   */
  reviseRequisitionItems(values) {
    const itemsToRevise = _.filter(
      values.editPageItems,
      (item) => {
        if (item.quantityRevised && item.reasonCode) {
          const oldRevision = _.find(
            this.state.revisedItems,
            revision => revision.requisitionItemId === item.requisitionItemId,
          );
          return _.isEmpty(oldRevision) ? true :
            ((_.toInteger(oldRevision.quantityRevised) !== _.toInteger(item.quantityRevised)) ||
              (oldRevision.reasonCode !== item.reasonCode));
        }
        return false;
      },
    );

    let updatedValues = values;

    _.forEach(itemsToRevise, (item) => {
      const editPageItemIndex = _.findIndex(this.state.values.editPageItems, editPageItem =>
        item.requisitionItemId === editPageItem.requisitionItemId);

      updatedValues = update(updatedValues, {
        editPageItems: {
          [editPageItemIndex]: {
            statusCode: {
              $set: 'CHANGED',
            },
          },
        },
      });
    });

    this.setState({
      values: updatedValues,
    });

    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/reviseItems`;
    const payload = {
      lineItems: _.map(itemsToRevise, item => ({
        id: item.requisitionItemId,
        quantityRevised: item.quantityRevised,
        reasonCode: item.reasonCode.value,
      })),
    };

    if (payload.lineItems.length) {
      return apiClient.post(url, payload);
    }

    return Promise.resolve();
  }

  updateRow(values, index) {
    const item = values.editPageItems[index];
    let val = values;
    val = update(values, {
      editPageItems: { [index]: { $set: item } },
    });
    this.setState({
      values: val,
    });
  }

  /**
   * Saves list of requisition items in current step (without step change).
   * @param {object} formValues
   * @public
   */
  save(formValues) {
    this.props.showSpinner();

    const errors = validateForSave(formValues).editPageItems;

    if (errors.length) {
      showOutboundEditValidationErrors({ translate: this.props.translate, errors });

      this.props.hideSpinner();
      return null;
    }

    return this.reviseRequisitionItems(formValues)
      .then((resp) => {
        // If reponse 200, then save revise items taken from the payload
        const payload = JSON.parse(resp.config.data);
        if (payload.lineItems && payload.lineItems.length) {
          const savedItemIds = payload.lineItems.map(item => item.id);
          // Map to have the required field
          // (requisitionItemId, quantityRevised and reasonCode as obj)
          const savedItems = payload.lineItems.map(item => ({
            ...item,
            requisitionItemId: item.id,
            reasonCode: _.find(
              this.props.reasonCodes,
              ({ value }) => _.includes(item.reasonCode, value),
            ),
          }));
          // Get old revise items, that were not changed in this request
          const oldItems = _.filter(
            this.state.values.editPageItems,
            item => savedItemIds.indexOf(item.requisitionItemId) < 0,
          );
          this.setState({
            revisedItems: [...oldItems, ...savedItems],
          });
        }

        this.props.hideSpinner();
        Alert.success(this.props.translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'), { timeout: 3000 });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Refetch the data, all not saved changes will be lost.
   * @public
   */
  refresh() {
    confirmAlert({
      title: this.props.translate('react.stockMovement.message.confirmRefresh.label', 'Confirm refresh'),
      message: this.props.translate(
        'react.stockMovement.confirmRefresh.message',
        'Are you sure you want to refresh? Your progress since last save will be lost.',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => {
            this.setState({
              revisedItems: [],
              values: { ...this.props.initialValues, editPageItems: [] },
              hasItemsLoaded: false,
              totalCount: 0,
              isFirstPageLoaded: false,
            });
            this.fetchAllData(true);
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Transition to next stock movement status (PICKING)
   * after sending createPicklist: 'true' to backend autopick functionality is invoked.
   * @public
   */
  transitionToNextStep() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = {
      status: 'PICKING',
      createPicklist: this.state.statusCode === 'REQUESTED' ? 'true' : 'false',
    };

    return apiClient.post(url, payload);
  }

  /**
   * Fetches 3rd step data from current stock movement.
   * @public
   */
  fetchEditPageData() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    this.props.showSpinner();
    this.reviseRequisitionItems(formValues)
      .then(() => {
        this.transitionToNextStep()
          .then(() => this.props.nextPage(formValues))
          .catch(() => this.props.hideSpinner());
      }).catch(() => this.props.hideSpinner());
  }

  /**
   * Saves changes made when item reverted.
   * @param {object} editPageItem
   * @public
   */
  updateEditPageItem(values, editPageItem) {
    const editPageItemIndex = _.findIndex(this.state.values.editPageItems, item =>
      item.requisitionItemId === editPageItem.requisitionItemId);
    const revisedItemIndex = _.findIndex(this.state.revisedItems, item =>
      item.requisitionItemId === editPageItem.requisitionItemId);

    this.setState({
      values: {
        ...values,
        editPageItems: update(values.editPageItems, {
          [editPageItemIndex]: {
            $set: {
              ...values.editPageItems[editPageItemIndex],
              ...editPageItem,
              quantityOnHand: editPageItem.quantityOnHand || 0,
              quantityAvailable: editPageItem.quantityAvailable || 0,
              substitutionItems: _.map(editPageItem.substitutionItems, sub => ({
                ...sub,
                requisitionItemId: editPageItem.requisitionItemId,
              })),
            },
          },
        }),
      },
      revisedItems: update(this.state.revisedItems, revisedItemIndex > -1 ? {
        $splice: [[revisedItemIndex, 1]],
      } : {}),
    });
  }

  /**
   * Saves changes made by user in this step and redirects to the shipment view page
   * @param {object} formValues
   * @public
   */
  saveAndExit(formValues) {
    const errors = validateForSave(formValues).editPageItems;

    if (errors.length) {
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmExit.label', 'Confirm save'),
        message: this.props.translate(
          'react.stockMovement.confirmExit.message',
          'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => { window.location = `/openboxes/stockMovement/show/${formValues.stockMovementId}`; },
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
          },
        ],
      });
      this.props.hideSpinner();
    } else {
      this.reviseRequisitionItems(formValues)
        .then(() => {
          window.location = `/openboxes/stockMovement/show/${formValues.stockMovementId}`;
        });
    }
  }

  /**
   * Reverts to previous state of requisition item (reverts substitutions and quantity revisions)
   * @param {string} itemId
   * @public
   */
  revertItem(values, itemId) {
    this.props.showSpinner();
    const revertItemsUrl = `/openboxes/api/stockMovementItems/${itemId}/revertItem`;
    const itemsUrl = `/openboxes/api/stockMovementItems/${itemId}?stepNumber=3`;

    return apiClient.post(revertItemsUrl)
      .then(() => apiClient.get(itemsUrl)
        .then((response) => {
          const editPageItem = response.data.data;
          this.updateEditPageItem(values, editPageItem);
          this.props.hideSpinner();
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.stockMovement.error.revertRequisitionItem.label', 'Could not revert requisition items')));
        }))
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.stockMovement.error.revertRequisitionItem.label', 'Could not revert requisition items')));
      });
  }

  render() {
    const { showOnlyErroredItems, itemFilter } = this.state;
    const { showOnly } = this.props;
    const erroredItemsCount = this.state.values && this.state.values.editPageItems.length > 0 ? _.filter(this.state.values.editPageItems, item => item.hasError).length : '0';
    return (
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            { !showOnly ?
              <span className="buttons-container">
                <div className="d-flex mr-auto justify-content-center align-items-center">
                  <input
                    value={itemFilter}
                    onChange={event => this.setState({ itemFilter: event.target.value })}
                    className="float-left btn btn-outline-secondary btn-xs filter-input mr-1 mb-1"
                    placeholder={this.props.translate('react.stockMovement.searchPlaceholder.label', 'Search...')}
                  />
                  {itemFilter &&
                    <i
                      role="button"
                      className="fa fa-times-circle"
                      style={{ color: 'grey', cursor: 'pointer' }}
                      onClick={() => this.setState({ itemFilter: '' })}
                      onKeyPress={() => this.setState({ itemFilter: '' })}
                      tabIndex={0}
                    />
                  }
                </div>
                <button
                  type="button"
                  onClick={() => this.setState({ showOnlyErroredItems: !showOnlyErroredItems })}
                  className={`float-right mb-1 btn btn-outline-secondary align-self-end ml-3 btn-xs ${showOnlyErroredItems ? 'active' : ''}`}
                >
                  <span>{erroredItemsCount} <Translate id="react.stockMovement.erroredItemsCount.label" defaultMessage="item(s) require your attention" /></span>
                </button>
                <button
                  type="button"
                  onClick={() => this.refresh()}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end ml-3 btn-xs"
                >
                  <span><i className="fa fa-refresh pr-2" /><Translate
                    id="react.default.button.refresh.label"
                    defaultMessage="Reload"
                  />
                  </span>
                </button>
                <button
                  type="button"
                  onClick={() => this.save(values)}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end ml-3 btn-xs"
                >
                  <span><i className="fa fa-save pr-2" /><Translate
                    id="react.default.button.saveProgress.label"
                    defaultMessage="Save Progress"
                  />
                  </span>
                </button>
                <button
                  type="button"
                  onClick={() => this.saveAndExit(values)}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                >
                  <span><i className="fa fa-sign-out pr-2" /><Translate
                    id="react.default.button.saveAndExit.label"
                    defaultMessage="Save and exit"
                  />
                  </span>
                </button>
              </span>
                :
              <button
                type="button"
                onClick={() => {
                  window.location = !this.props.supportedActivities.includes('MANAGE_INVENTORY') && this.props.supportedActivities.includes('SUBMIT_REQUEST')
                    ? '/openboxes/dashboard'
                    : '/openboxes/stockMovement/list?direction=INBOUND';
                }}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
              >
                <span><i className="fa fa-sign-out pr-2" /> <Translate id="react.default.button.exit.label" defaultMessage="Exit" /> </span>
              </button> }
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(this.getFields(), (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    stockMovementId: values.stockMovementId,
                    hasStockList: !!_.get(values.stocklist, 'id'),
                    translate: this.props.translate,
                    reasonCodes: this.props.reasonCodes,
                    onResponse: this.fetchEditPageItems,
                    revertItem: this.revertItem,
                    reviseRequisitionItems: this.reviseRequisitionItems,
                    totalCount: this.state.totalCount,
                    loadMoreRows: this.loadMoreRows,
                    isRowLoaded: this.isRowLoaded,
                    isPaginated: this.props.isPaginated,
                    updateRow: this.updateRow,
                    isFirstPageLoaded: this.state.isFirstPageLoaded,
                    values,
                    showOnly,
                    showOnlyErroredItems,
                    itemFilter,
                }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="submit"
                  disabled={!this.state.hasItemsLoaded || showOnly || invalid}
                  onClick={() => {
                    if (!invalid) {
                      this.nextPage(values);
                    }
                  }}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                >
                  <Translate id="react.stockMovement.button.generatePicklist.label" defaultMessage="Generate Picklist" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  reasonCodes: state.reasonCodes.data,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  isPaginated: state.session.isPaginated,
  pageSize: state.session.pageSize,
  supportedActivities: state.session.supportedActivities,
  currentLocale: state.session.activeLanguage,
});

export default connect(mapStateToProps, {
  fetchReasonCodes, showSpinner, hideSpinner,
})(EditItemsPage);

EditItemsPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  nextPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function fetching reason codes */
  fetchReasonCodes: PropTypes.func.isRequired,
  /** Array of available reason codes */
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  /** Return true if show only */
  showOnly: PropTypes.bool.isRequired,
  pageSize: PropTypes.number.isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  currentLocale: PropTypes.string.isRequired,
};
