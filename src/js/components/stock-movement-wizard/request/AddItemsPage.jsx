import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import update from 'immutability-helper';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import Alert from 'react-s-alert';

import { fetchUsers, hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import LabelField from 'components/form-elements/LabelField';
import ProductSelectField from 'components/form-elements/ProductSelectField';
import TextField from 'components/form-elements/TextField';
import apiClient from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import isRequestFromWard from 'utils/supportedActivitiesUtils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

function addButton({
  // eslint-disable-next-line react/prop-types
  addRow, getSortOrder, newItemAdded, updateTotalCount,
}) {
  return (
    <button
      type="button"
      className="btn btn-outline-success btn-xs"
      onClick={() => {
        updateTotalCount(1);
        addRow({ sortOrder: getSortOrder() });
        newItemAdded();
      }}
    ><span><i className="fa fa-plus pr-2" /><Translate id="react.default.button.addLine.label" defaultMessage="Add line" /></span>
    </button>
  );
}

// Used for util function to calculate quantityRequested for requests from wards
// where quantityRequested is calculated by subtracting one of below from QOH
const RequestFromWardTypes = {
  MANUAL: {
    calculateQtyRequestedFrom: 'monthlyDemand',
  },
  STOCKLIST_PUSH_TYPE: {
    calculateQtyRequestedFrom: 'quantityAllowed',
  },
  STOCKLIST_PULL_TYPE: {
    calculateQtyRequestedFrom: 'demandPerReplenishmentPeriod',
  },
};

const FIELDS = {
  product: {
    type: ProductSelectField,
    label: 'react.stockMovement.requestedProduct.label',
    defaultMessage: 'Requested product',
    headerAlign: 'left',
    flexWidth: '9',
    attributes: {
      showSelectedOptionColor: true,
    },
  },
  quantityOnHand: {
    type: LabelField,
    label: 'react.stockMovement.quantityOnHand.label',
    defaultMessage: 'QoH',
    flexWidth: '1.7',
    attributes: {
      type: 'number',
    },
  },
  quantityOnHandAtRequestSite: {
    type: TextField,
    flexWidth: '1.7',
    label: 'react.stockMovement.quantityOnHandAtRequestSite.label',
    defaultMessage: 'QOH at Request Site',
    attributes: {
      type: 'number',
    },
  },
  quantityAvailable: {
    type: LabelField,
    label: 'react.stockMovement.available.label',
    defaultMessage: 'Available',
    flexWidth: '1.7',
    attributes: {
      type: 'number',
    },
  },
  quantityAllowed: {
    type: LabelField,
    label: 'react.stockMovement.maxQuantity.label',
    defaultMessage: 'Max Qty',
    flexWidth: '1.7',
    attributes: {
      type: 'number',
    },
  },
  monthlyDemand: {
    type: LabelField,
    label: 'react.stockMovement.demandPerMonth.label',
    defaultMessage: 'Demand per Month',
    flexWidth: '1.7',
    attributes: {
      type: 'number',
    },
  },
  quantityRequested: {
    type: TextField,
    label: 'react.stockMovement.neededQuantity.label',
    defaultMessage: 'Needed Qty',
    flexWidth: '1.7',
    attributes: {
      type: 'number',
    },
  },
  comments: {
    type: TextField,
    label: 'react.stockMovement.comments.label',
    defaultMessage: 'Comments',
    flexWidth: '1.7',
    getDynamicAttr: ({
      addRow, rowCount, rowIndex, getSortOrder,
      updateTotalCount, updateRow, values,
    }) => ({
      onTabPress: rowCount === rowIndex + 1 ? () => {
        updateTotalCount(1);
        addRow({ sortOrder: getSortOrder() });
      } : null,
      arrowRight: rowCount === rowIndex + 1 ? () => {
        updateTotalCount(1);
        addRow({ sortOrder: getSortOrder() });
      } : null,
      arrowDown: rowCount === rowIndex + 1 ? () => () => {
        updateTotalCount(1);
        addRow({ sortOrder: getSortOrder() });
      } : null,
      onBlur: () => updateRow(values, rowIndex),
    }),
  },
  demandPerReplenishmentPeriod: {
    type: LabelField,
    label: 'react.stockMovement.demandPerReplenishmentPeriod.label',
    defaultMessage: 'Demand per Replenishment Period',
    flexWidth: '1.7',
    attributes: {
      type: 'number',
    },
  },
  demandPerRequestPeriod: {
    type: LabelField,
    label: 'react.stockMovement.demandPerRequestPeriod.label',
    defaultMessage: 'Demand per Request Period',
    flexWidth: '1.7',
    attributes: {
      type: 'number',
    },
  },
};

const DELETE_BUTTON_FIELD = {
  type: ButtonField,
  label: 'react.default.button.delete.label',
  defaultMessage: 'Delete',
  flexWidth: '1',
  fieldKey: '',
  buttonLabel: 'react.default.button.delete.label',
  buttonDefaultMessage: 'Delete',
  getDynamicAttr: ({
    fieldValue, removeItem, removeRow, updateTotalCount,
  }) => ({
    onClick: fieldValue && fieldValue.id ? () => {
      removeItem(fieldValue.id).then(() => {
        updateTotalCount(-1);
        removeRow();
      });
    } : () => { updateTotalCount(-1); removeRow(); },
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const LINE_ITEMS_ATTR = {
  type: ArrayField,
  arrowsNavigation: true,
  virtualized: true,
  totalCount: ({ totalCount }) => totalCount,
  isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
  loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
  isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
  addButton,
};


const NO_STOCKLIST_FIELDS = {
  lineItems: {
    ...LINE_ITEMS_ATTR,
    fields: {
      product: {
        ...FIELDS.product,
        fieldKey: '',
        flexWidth: '9.5',
        getDynamicAttr: ({
          rowIndex, rowCount, updateProductData, values, originId, focusField,
        }) => ({
          onChange: value => updateProductData(value, values, rowIndex),
          autoFocus: rowIndex === rowCount - 1,
          locationId: originId,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityRequested');
            }
          },
        }),
      },
      quantityOnHand: FIELDS.quantityOnHand,
      quantityAvailable: FIELDS.quantityAvailable,
      monthlyDemand: FIELDS.monthlyDemand,
      quantityRequested: {
        ...FIELDS.quantityRequested,
        flexWidth: '2.5',
        fieldKey: '',
        getDynamicAttr: ({
          updateRow, values, rowIndex,
        }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      comments: FIELDS.comments,
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const STOCKLIST_FIELDS_PUSH_TYPE = {
  lineItems: {
    ...LINE_ITEMS_ATTR,
    fields: {
      product: {
        ...FIELDS.product,
        fieldKey: 'disabled',
        getDynamicAttr: ({
          fieldValue, rowIndex, rowCount, newItem, originId, focusField,
        }) => ({
          locationId: originId,
          disabled: !!fieldValue,
          autoFocus: newItem && rowIndex === rowCount - 1,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityRequested');
            }
          },
        }),
      },
      quantityAllowed: FIELDS.quantityAllowed,
      quantityOnHand: FIELDS.quantityOnHand,
      quantityAvailable: FIELDS.quantityAvailable,
      quantityRequested: {
        ...FIELDS.quantityRequested,
        getDynamicAttr: ({
          rowIndex, values, updateRow,
        }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      comments: FIELDS.comments,
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const STOCKLIST_FIELDS_PULL_TYPE = {
  lineItems: {
    ...LINE_ITEMS_ATTR,
    fields: {
      product: {
        ...FIELDS.product,
        fieldKey: 'disabled',
        getDynamicAttr: ({
          fieldValue, rowIndex, rowCount, newItem, originId, focusField,
        }) => ({
          locationId: originId,
          disabled: !!fieldValue,
          autoFocus: newItem && rowIndex === rowCount - 1,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityRequested');
            }
          },
        }),
      },
      demandPerReplenishmentPeriod: FIELDS.demandPerReplenishmentPeriod,
      quantityOnHand: FIELDS.quantityOnHand,
      quantityAvailable: FIELDS.quantityAvailable,
      quantityRequested: FIELDS.quantityRequested,
      comments: FIELDS.comments,
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const REQUEST_FROM_WARD_STOCKLIST_FIELDS_PUSH_TYPE = {
  lineItems: {
    ...LINE_ITEMS_ATTR,
    fields: {
      product: {
        ...FIELDS.product,
        fieldKey: 'disabled',
        getDynamicAttr: ({
          fieldValue, rowIndex, rowCount, newItem, originId, focusField,
        }) => ({
          locationId: originId,
          disabled: !!fieldValue,
          autoFocus: newItem && rowIndex === rowCount - 1,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityOnHand');
            }
          },
        }),
      },
      quantityAllowed: {
        ...FIELDS.quantityAllowed,
        headerTooltip: 'react.stockMovement.tooltip.maxQuantity.label',
        headerAlign: 'right',
        attributes: {
          ...FIELDS.quantityAllowed.attributes,
          cellClassName: 'text-right',
        },
      },
      quantityOnHand: {
        ...FIELDS.quantityOnHand,
        type: TextField,
        headerTooltip: 'react.stockMovement.tooltip.quantityOnHand.label',
        headerAlign: 'right',
        attributes: {
          ...FIELDS.quantityOnHand.attributes,
          cellClassName: 'text-right',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, values, updateRow, calculateQtyRequested,
        }) => ({
          onBlur: () => {
            const valuesWithUpdatedQtyRequested =
              calculateQtyRequested(
                values,
                rowIndex,
                fieldValue,
                RequestFromWardTypes.STOCKLIST_PUSH_TYPE,
              );
            updateRow(valuesWithUpdatedQtyRequested, rowIndex);
          },
        }),
      },
      quantityRequested: {
        ...FIELDS.quantityRequested,
        headerAlign: 'right',
        required: true,
        headerTooltip: 'react.stockMovement.tooltip.quantityRequested.label',
        attributes: {
          ...FIELDS.quantityRequested.attributes,
          cellClassName: 'text-right',
        },
        getDynamicAttr: ({
          rowIndex, values, updateRow,
        }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      comments: {
        ...FIELDS.comments,
        headerTooltip: 'react.stockMovement.tooltip.comments.label',
        headerAlign: 'left',
        attributes: {
          ...FIELDS.comments.attributes,
          cellClassName: 'text-left',
        },
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const REQUEST_FROM_WARD_STOCKLIST_FIELDS_PULL_TYPE = {
  lineItems: {
    ...LINE_ITEMS_ATTR,
    fields: {
      product: {
        ...FIELDS.product,
        fieldKey: 'disabled',
        flexWidth: '2',
        getDynamicAttr: ({
          fieldValue, rowIndex, rowCount, newItem, originId, focusField,
        }) => ({
          locationId: originId,
          disabled: !!fieldValue,
          autoFocus: newItem && rowIndex === rowCount - 1,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityOnHand');
            }
          },
        }),
      },
      demandPerReplenishmentPeriod: {
        type: LabelField,
        label: 'react.stockMovement.demandPerRequestPeriod.label',
        defaultMessage: 'Demand per Request Period',
        flexWidth: '1',
        headerAlign: 'right',
        headerTooltip: 'react.stockMovement.demandPerRequestPeriod.headerTooltip.label',
        headerDefaultTooltip: 'The average of your previous requests for this product.',
        attributes: {
          type: 'number',
          className: 'text-right',
        },
      },
      quantityOnHand: {
        ...FIELDS.quantityOnHandAtRequestSite,
        label: 'react.stockMovement.quantityOnHand.label',
        defaultMessage: 'QOH',
        flexWidth: '0.6',
        headerAlign: 'right',
        headerTooltip: 'react.stockMovement.quantityOnHand.headerTooltip.label',
        headerDefaultTooltip: 'Enter your current quantity on hand for this product',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, values, updateRow, calculateQtyRequested,
        }) => ({
          onBlur: () => {
            const valuesWithUpdatedQtyRequested =
              calculateQtyRequested(
                values,
                rowIndex,
                fieldValue,
                RequestFromWardTypes.STOCKLIST_PULL_TYPE,
              );
            updateRow(valuesWithUpdatedQtyRequested, rowIndex);
          },
        }),
      },
      quantityRequested: {
        type: TextField,
        label: 'react.stockMovement.neededQuantity.label',
        defaultMessage: 'Needed Qty',
        flexWidth: '0.6',
        required: true,
        headerAlign: 'right',
        headerTooltip: 'react.stockMovement.quantityRequested.headerTooltip.label',
        headerDefaultTooltip: 'Your demand for the request period minus your QOH. Edit as needed.',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          rowIndex, values, updateRow,
        }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      comments: {
        type: TextField,
        label: 'react.stockMovement.comments.label',
        defaultMessage: 'Comments',
        flexWidth: '1.6',
        headerAlign: 'left',
        headerTooltip: 'react.stockMovement.comments.headerTooltip.label',
        headerDefaultTooltip: 'Leave a comment for the person who will review this request.',
        getDynamicAttr: ({
          addRow, rowCount, rowIndex, getSortOrder,
          updateTotalCount, updateRow, values,
        }) => ({
          onTabPress: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder() });
          } : null,
          arrowRight: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder() });
          } : null,
          arrowDown: rowCount === rowIndex + 1 ? () => () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder() });
          } : null,
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const REQUEST_FROM_WARD_FIELDS = {
  lineItems: {
    ...LINE_ITEMS_ATTR,
    fields: {
      product: {
        ...FIELDS.product,
        fieldKey: 'disabled',
        flexWidth: '2.4',
        getDynamicAttr: ({
          rowIndex, rowCount, updateProductData, values, newItem, originId, focusField,
        }) => ({
          locationId: originId,
          onChange: value => updateProductData(value, values, rowIndex),
          autoFocus: newItem && rowIndex === rowCount - 1,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityOnHand');
            }
          },
        }),
      },
      monthlyDemand: {
        type: LabelField,
        label: 'react.stockMovement.demandPerMonth.label',
        defaultMessage: 'Demand per Month',
        headerAlign: 'right',
        flexWidth: '0.8',
        headerTooltip: 'react.stockMovement.demandPerRequestPeriod.headerTooltip.label',
        headerDefaultTooltip: 'The average of your previous requests for this product.',
        attributes: {
          type: 'number',
          className: 'text-right',
        },
      },
      quantityOnHand: {
        type: TextField,
        label: 'react.stockMovement.quantityOnHand.label',
        defaultMessage: 'QOH',
        flexWidth: '0.6',
        headerAlign: 'right',
        headerTooltip: 'react.stockMovement.quantityOnHand.headerTooltip.label',
        headerDefaultTooltip: 'Enter your current quantity on hand for this product',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, values, updateRow, calculateQtyRequested,
        }) => ({
          onBlur: () => {
            const valuesWithUpdatedQtyRequested =
              calculateQtyRequested(values, rowIndex, fieldValue, RequestFromWardTypes.MANUAL);
            updateRow(valuesWithUpdatedQtyRequested, rowIndex);
          },
        }),
      },
      quantityRequested: {
        type: TextField,
        label: 'react.stockMovement.neededQuantity.label',
        defaultMessage: 'Needed Qty',
        flexWidth: '0.6',
        required: true,
        headerAlign: 'right',
        headerTooltip: 'react.stockMovement.quantityRequested.headerTooltip.label',
        headerDefaultTooltip: 'Your demand for the request period minus your QOH. Edit as needed.',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          rowIndex, values, updateRow,
        }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      comments: {
        type: TextField,
        label: 'react.stockMovement.comments.label',
        defaultMessage: 'Comments',
        flexWidth: '2.4',
        headerAlign: 'left',
        headerTooltip: 'react.stockMovement.comments.headerTooltip.label',
        headerDefaultTooltip: 'Leave a comment for the person who will review this request.',
        getDynamicAttr: ({
          addRow, rowCount, rowIndex, getSortOrder,
          updateTotalCount, updateRow, values,
        }) => ({
          onTabPress: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder() });
          } : null,
          arrowRight: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder() });
          } : null,
          arrowDown: rowCount === rowIndex + 1 ? () => () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder() });
          } : null,
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const REPLENISHMENT_TYPE_PULL = 'PULL';

function calculateQuantityRequested(values, rowIndex, fieldValue, requestType) {
  const valuesWithUpdatedQtyRequested = values;
  const lineItem = valuesWithUpdatedQtyRequested.lineItems[rowIndex];
  // Options: quantityAllowed, demandPerReplenishmentPeriod, monthlyDemand
  // depending on request from ward type: stocklist push, stocklist pull, manual respectively
  const baseValue = lineItem[requestType.calculateQtyRequestedFrom];
  if (baseValue && fieldValue) {
    valuesWithUpdatedQtyRequested.lineItems[rowIndex].quantityRequested =
      baseValue - fieldValue >= 0 ?
        baseValue - fieldValue : 0;
  }
  return valuesWithUpdatedQtyRequested;
}


/**
 * The second step of stock movement where user can add items to stock list.
 * This component supports three different cases: with or without stocklist
 * when movement is from a depot and when movement is from a vendor.
 */
class AddItemsPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentLineItems: [],
      sortOrder: 0,
      values: { ...this.props.initialValues, lineItems: [] },
      newItem: false,
      totalCount: 0,
      isFirstPageLoaded: false,
      isRequestFromWard: false,
    };

    this.props.showSpinner();
    this.removeItem = this.removeItem.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
    this.getSortOrder = this.getSortOrder.bind(this);
    this.confirmSave = this.confirmSave.bind(this);
    this.confirmTransition = this.confirmTransition.bind(this);
    this.newItemAdded = this.newItemAdded.bind(this);
    this.validate = this.validate.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateTotalCount = this.updateTotalCount.bind(this);
    this.updateRow = this.updateRow.bind(this);
    this.updateProductData = this.updateProductData.bind(this);
    this.submitRequest = this.submitRequest.bind(this);
    this.calculateQuantityRequested =
      calculateQuantityRequested.bind(this);
    this.cancelRequest = this.cancelRequest.bind(this);
  }

  componentDidMount() {
    if (this.props.stockMovementTranslationsFetched) {
      this.dataFetched = true;

      this.fetchAllData();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchAllData();
    }
  }

  /**
   * Returns proper fields depending on origin type or if stock list is chosen.
   * @public
   */
  getFields() {
    if (this.state.isRequestFromWard) {
      if (_.get(this.state.values.stocklist, 'id')) {
        if (_.get(this.state.values.replenishmentType, 'name') === REPLENISHMENT_TYPE_PULL) {
          return REQUEST_FROM_WARD_STOCKLIST_FIELDS_PULL_TYPE;
        }
        return REQUEST_FROM_WARD_STOCKLIST_FIELDS_PUSH_TYPE;
      }
      return REQUEST_FROM_WARD_FIELDS;
    }

    if (_.get(this.state.values.stocklist, 'id')) {
      if (_.get(this.state.values.replenishmentType, 'name') === REPLENISHMENT_TYPE_PULL) {
        return STOCKLIST_FIELDS_PULL_TYPE;
      }
      return STOCKLIST_FIELDS_PUSH_TYPE;
    }

    return NO_STOCKLIST_FIELDS;
  }

  /**
   * Returns an array of new stock movement's items and items to be
   * updated (comparing to previous state of line items).
   * @param {object} lineItems
   * @public
   */
  getLineItemsToBeSaved(lineItems) {
    // First find items that are new and should be added (don't have status code)
    const lineItemsToBeAdded = _.filter(lineItems, item =>
      !item.statusCode && item.quantityRequested && item.quantityRequested !== '0' && item.product);
    // Then get a list of items that already exist in this request (have status code)
    const lineItemsWithStatus = _.filter(lineItems, item => item.statusCode);
    const lineItemsToBeUpdated = [];
    // For each already existing items - find the ones that have changed
    _.forEach(lineItemsWithStatus, (item) => {
      const oldItem = _.find(this.state.currentLineItems, old => old.id === item.id);
      const oldQty = parseInt(oldItem.quantityRequested, 10);
      const newQty = parseInt(item.quantityRequested, 10);
      // Intersection of keys common to both objects (excluding product key)
      const keyIntersection = _.remove(
        _.intersection(
          _.keys(oldItem),
          _.keys(item),
        ),
        key => key !== 'product',
      );

      if (
        (this.state.values.origin.type === 'SUPPLIER' || !this.state.values.hasManageInventory) &&
        (
          !_.isEqual(_.pick(item, keyIntersection), _.pick(oldItem, keyIntersection)) ||
          (item.product.id !== oldItem.product.id)
        )
      ) {
        lineItemsToBeUpdated.push(item);
      } else if (newQty !== oldQty || !item.quantityRequested ||
        (oldItem.comments !== item.comments && !_.isNil(item.comments))) {
        lineItemsToBeUpdated.push(item);
      }
    });

    // Combine items to be added and items to be updated into one list to be saved
    return [].concat(
      _.map(lineItemsToBeAdded, item => ({
        'product.id': item.product.id,
        quantityRequested: item.quantityRequested,
        sortOrder: item.sortOrder,
        comments: !_.isNil(item.comments) ? item.comments : '',
      })),
      _.map(lineItemsToBeUpdated, item => ({
        id: item.id,
        'product.id': item.product.id,
        quantityRequested: item.quantityRequested,
        sortOrder: item.sortOrder,
        comments: !_.isNil(item.comments) ? item.comments : '',
      })),
    );
  }

  getSortOrder() {
    this.setState({
      sortOrder: this.state.sortOrder + 100,
    });

    return this.state.sortOrder;
  }

  setLineItems(response, startIndex) {
    const { data } = response.data;
    let lineItemsData;

    const isPullType = _.get(this.state.values.replenishmentType, 'name') === REPLENISHMENT_TYPE_PULL;

    if (this.state.values.lineItems.length === 0 && !data.length) {
      lineItemsData = new Array(1).fill({ sortOrder: 100 });
    } else if (this.state.isRequestFromWard && _.get(this.state.values.stocklist, 'id')) {
      lineItemsData = _.map(
        data,
        (val) => {
          const {
            quantityRequested,
            demandPerReplenishmentPeriod,
            quantityOnHand,
            quantityAllowed,
          } = val;

          let qtyRequested = 0;
          if (quantityRequested) qtyRequested = quantityRequested;
          else if (isPullType) qtyRequested = demandPerReplenishmentPeriod - quantityOnHand;
          else qtyRequested = quantityAllowed;

          return {
            ...val,
            quantityOnHand: '',
            disabled: true,
            quantityRequested: qtyRequested >= 0 ? qtyRequested : 0,
          };
        },
      );
    } else if (isPullType) {
      lineItemsData = _.map(
        data,
        (val) => {
          const { quantityRequested, demandPerReplenishmentPeriod, quantityAvailable } = val;
          const qtyRequested =
              quantityRequested || demandPerReplenishmentPeriod - quantityAvailable;
          return {
            ...val,
            disabled: true,
            quantityRequested: qtyRequested >= 0 ? qtyRequested : 0,
            quantityOnHand: this.state.isRequestFromWard ? '' : val.quantityOnHand,
          };
        },
      );
    } else {
      lineItemsData = _.map(
        data,
        val => ({
          ...val,
          disabled: true,
          quantityOnHand: this.state.isRequestFromWard ? '' : val.quantityOnHand,
        }),
      );
    }

    const sortOrder = _.toInteger(_.last(lineItemsData).sortOrder) + 100;
    this.setState({
      currentLineItems: this.props.isPaginated ?
        _.uniqBy(_.concat(this.state.currentLineItems, data), 'id') : data,
      values: {
        ...this.state.values,
        lineItems: this.props.isPaginated ?
          _.uniqBy(_.concat(this.state.values.lineItems, lineItemsData), 'id') : lineItemsData,
      },
      sortOrder,
    }, () => {
      if (!_.isNull(startIndex) && this.state.values.lineItems.length !== this.state.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      }
      this.props.hideSpinner();
    });
  }

  updateTotalCount(value) {
    this.setState({
      totalCount: this.state.totalCount + value === 0 ? 1 : this.state.totalCount + value,
    });
  }

  updateRow(values, index) {
    const item = values.lineItems[index];
    this.setState({
      values: update(values, {
        lineItems: { [index]: { $set: item } },
      }),
    });
  }

  dataFetched = false;


  validate(values) {
    const errors = {};
    errors.lineItems = [];
    const date = moment(this.props.minimumExpirationDate, 'MM/DD/YYYY');

    _.forEach(values.lineItems, (item, key) => {
      const rowErrors = {};
      if (!_.isNil(item.product)) {
        if ((_.isNil(item.quantityRequested) || item.quantityRequested < 0)) {
          rowErrors.quantityRequested = 'react.stockMovement.error.enterQuantity.label';
        }
      }
      if (!_.isEmpty(item.boxName) && _.isEmpty(item.palletName)) {
        rowErrors.boxName = 'react.stockMovement.error.boxWithoutPallet.label';
      }
      const dateRequested = moment(item.expirationDate, 'MM/DD/YYYY');
      if (date.diff(dateRequested) > 0) {
        rowErrors.expirationDate = 'react.stockMovement.error.invalidDate.label';
      }

      if (this.state.isRequestFromWard) {
        if ((_.isNil(item.quantityRequested) || item.quantityRequested < 0)) {
          rowErrors.quantityRequested = 'react.stockMovement.error.enterQuantity.label';
        }
      }
      if (!_.isEmpty(rowErrors)) {
        errors.lineItems[key] = rowErrors;
      }
    });
    return errors;
  }

  newItemAdded() {
    this.setState({
      newItem: true,
    });
  }

  /**
   * Exports current state of stock movement's to csv file.
   * @param {object} formValues
   * @public
   */
  exportTemplate(formValues) {
    const lineItems = _.filter(formValues.lineItems, item => !_.isEmpty(item));

    this.saveItemsAndExportTemplate(formValues, lineItems);
  }

  /**
   * Exports current state of stock movement's to csv file.
   * @param {object} formValues
   * @param {object} lineItems
   * @public
   */
  saveItemsAndExportTemplate(formValues, lineItems) {
    const { movementNumber, stockMovementId } = formValues;
    const url = `/openboxes/stockMovement/exportCsv/${stockMovementId}`;
    this.props.showSpinner();
    return this.saveRequisitionItemsInCurrentStep(lineItems)
      .then(() => {
        apiClient.get(url, { responseType: 'blob' })
          .then((response) => {
            fileDownload(response.data, `ItemList${movementNumber ? `-${movementNumber}` : ''}.csv`, 'text/csv');
            this.props.hideSpinner();
          });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Imports chosen file to backend and then fetches line items.
   * @param {object} event
   * @public
   */
  importTemplate(event) {
    this.props.showSpinner();
    const formData = new FormData();
    const file = event.target.files[0];
    const { stockMovementId } = this.state.values;

    formData.append('importFile', file.slice(0, file.size, 'text/csv'));
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };

    const url = `/openboxes/stockMovement/importCsv/${stockMovementId}`;

    return apiClient.post(url, formData, config)
      .then(() => {
        this.setState({
          values: {
            ...this.state.values,
            lineItems: [],
          },
        });
        this.fetchLineItems();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  /**
   * Shows save confirmation dialog.
   * @param {function} onConfirm
   * @public
   */
  confirmSave(onConfirm) {
    confirmAlert({
      title: this.props.translate('react.stockMovement.message.confirmSave.label', 'Confirm save'),
      message: this.state.isRequestFromWard ?
        this.props.translate(
          'react.stockMovement.QOHWillNotBeSaved.message',
          'This save action won’t save the quantity on hand you have entered. You will have to reenter these when you came back to this request later. Also if there are any empty or zero quantity lines, those lines will be deleted. Are you sure you want to proceed?',
        )
        :
        this.props.translate(
          'react.stockMovement.confirmSave.message',
          'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
        ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Shows transition confirmation dialog if there are items with the same code.
   * @param {function} onConfirm
   * @param {object} items
   * @public
   */
  confirmTransition(onConfirm, items) {
    confirmAlert({
      title: this.props.translate('react.stockMovement.confirmTransition.label', 'You have entered the same code twice. Do you want to continue?'),
      message: _.map(items, item => (
        <p key={item.sortOrder}>
          {`${item.product.productCode} ${item.product.displayNames?.default || item.product.name} ${item.quantityRequested}`}
        </p>)),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  confirmSubmit(onConfirm) {
    confirmAlert({
      title: this.props.translate('react.stockMovement.message.confirmSubmit.label', 'Confirm submit'),
      message: this.props.translate(
        'react.stockMovement.confirmSubmit.message',
        'Please confirm you are ready to submit your request. Once submitted, you cannot edit the request.',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.goBack.label', 'Go back'),
        },
        {
          label: this.props.translate('react.default.submit.label', 'Submit'),
          onClick: onConfirm,
        },
      ],
    });
  }

  /**
   * Fetches all required data.
   * @param {boolean} forceFetch
   * @public
   */
  fetchAllData() {
    this.fetchAddItemsPageData();
    if (!this.props.isPaginated) {
      this.fetchLineItems();
    } else if (this.state.isFirstPageLoaded) {
      // Workaround for refetching items from scratch
      // when the first page was already loaded and table is paginated
      this.loadMoreRows({ startIndex: 0 });
    }
  }

  /**
   * Fetches 2nd step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=2`;

    return apiClient.get(url)
      .then((response) => {
        this.setState({
          totalCount: response.data.data.length,
        }, () => this.setLineItems(response, null));
      })
      .catch(err => err);
  }

  isRowLoaded({ index }) {
    return !!this.state.values.lineItems[index];
  }

  /**
   * Fetches stock movement's line items and sets them in redux form and in
   * state as current line items.
   * @public
   */
  fetchAddItemsPageData() {
    this.props.showSpinner();
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
    apiClient.get(url)
      .then((resp) => {
        const { data: { hasManageInventory, statusCode }, totalCount } = resp.data;

        this.setState({
          values: {
            ...this.state.values,
            hasManageInventory,
            statusCode,
          },
          totalCount: totalCount === 0 ? 1 : totalCount,
          isRequestFromWard: isRequestFromWard(
            this.props.currentLocationId,
            this.state.values.destination.id,
            this.props.supportedActivities,
          ),
        });
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  loadMoreRows({ startIndex }) {
    this.setState({
      isFirstPageLoaded: true,
    });
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${this.props.pageSize}&stepNumber=2`;
    apiClient.get(url)
      .then((response) => {
        this.setLineItems(response, startIndex);
      });
  }

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    const lineItems = _.filter(formValues.lineItems, val => !_.isEmpty(val) && val.product);

    if (_.some(lineItems, item => !item.quantityRequested || item.quantityRequested === '0')) {
      this.confirmSave(() =>
        this.checkDuplicatesSaveAndTransitionToNextStep(formValues, lineItems));
    } else {
      this.checkDuplicatesSaveAndTransitionToNextStep(formValues, lineItems);
    }
  }

  checkDuplicatesSaveAndTransitionToNextStep(formValues, lineItems) {
    const itemsMap = {};
    _.forEach(lineItems, (item) => {
      if (itemsMap[item.product.productCode]) {
        itemsMap[item.product.productCode].push(item);
      } else {
        itemsMap[item.product.productCode] = [item];
      }
    });
    const itemsWithSameCode = _.filter(itemsMap, item => item.length > 1);

    if (_.some(itemsMap, item => item.length > 1) && !(this.state.values.origin.type === 'SUPPLIER' || !this.state.values.hasManageInventory)) {
      this.confirmTransition(
        () => this.saveAndTransitionToNextStep(formValues, lineItems),
        _.reduce(itemsWithSameCode, (a, b) => a.concat(b), []),
      );
    } else {
      this.saveAndTransitionToNextStep(formValues, lineItems);
    }
  }

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @param {object} lineItems
   * @public
   */
  saveAndTransitionToNextStep(formValues, lineItems) {
    this.props.showSpinner();

    if (formValues.origin.type === 'SUPPLIER' || !formValues.hasManageInventory) {
      this.saveRequisitionItems(lineItems)
        .then((resp) => {
          let values = formValues;
          if (resp) {
            values = { ...formValues, lineItems: resp.data.data.lineItems };
          }
          this.transitionToNextStep('CHECKING')
            .then(() => {
              this.props.nextPage(values);
            })
            .catch(() => this.props.hideSpinner());
        })
        .catch(() => this.props.hideSpinner());
    } else {
      this.saveRequisitionItems(lineItems)
        .then((resp) => {
          let values = formValues;
          if (resp) {
            values = { ...formValues, lineItems: resp.data.data.lineItems };
          }
          this.transitionToNextStep('VERIFYING')
            .then(() => {
              this.props.nextPage(values);
            })
            .catch(() => this.props.hideSpinner());
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  /**
   * Saves list of stock movement items with post method.
   * @param {object} lineItems
   * @public
   */
  saveRequisitionItems(lineItems) {
    const itemsToSave = this.getLineItemsToBeSaved(lineItems);
    const updateItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/updateItems`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };

    if (payload.lineItems.length) {
      return apiClient.post(updateItemsUrl, payload)
        .then(() => this.fetchAddItemsPageData())
        .catch(() => Promise.reject(new Error('react.stockMovement.error.saveRequisitionItems.label')));
    }

    return Promise.resolve();
  }

  submitRequest(lineItems) {
    this.confirmSubmit(() => {
      const nonEmptyLineItems = _.filter(lineItems, val => !_.isEmpty(val) && val.product);
      this.saveRequisitionItems(nonEmptyLineItems)
        .then(() => this.transitionToNextStep('REQUESTED'));
    });
  }

  /**
   * Saves list of requisition items in current step (without step change). Used to export template.
   * @param {object} itemCandidatesToSave
   * @public
   */
  saveRequisitionItemsInCurrentStep(itemCandidatesToSave) {
    const itemsToSave = this.getLineItemsToBeSaved(itemCandidatesToSave);
    const updateItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/updateItems`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };
    if (payload.lineItems.length) {
      return apiClient.post(updateItemsUrl, payload)
        .then(() => this.setState({
          currentLineItems: [],
          values: { ...this.state.values, lineItems: [] },
          sortOrder: 0,
          newItem: false,
          totalCount: 0,
        }, () => this.fetchAllData()))
        .catch(() => Promise.reject(new Error(this.props.translate('react.stockMovement.error.saveRequisitionItems.label', 'Could not save requisition items'))));
    }
    return Promise.resolve();
  }

  /**
   * Saves list of requisition items in current step (without step change).
   * @param {object} formValues
   * @public
   */
  save(formValues) {
    const lineItems = _.filter(formValues.lineItems, item => !_.isEmpty(item));
    const zeroedLines = _.some(lineItems, item => !item.quantityRequested || item.quantityRequested === '0');
    if (zeroedLines || this.state.isRequestFromWard) {
      this.confirmSave(() => this.saveItems(lineItems));
    } else {
      this.saveItems(lineItems);
    }
  }

  cancelRequest() {
    confirmAlert({
      title: this.props.translate(
        'react.stockMovement.request.confirmCancellation.label',
        'Confirm request cancellation',
      ),
      message: this.props.translate(
        'react.stockMovement.request.confirmCancellation.message.label',
        'Are you sure you want to delete current request ?',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => {
            this.props.showSpinner();
            apiClient.delete(`/openboxes/api/stockMovements/${this.state.values.stockMovementId}`)
              .then((response) => {
                if (response.status === 204) {
                  this.props.hideSpinner();
                  Alert.success(this.props.translate(
                    'react.stockMovement.request.successfullyDeleted.label',
                    'Request was successfully deleted',
                  ), { timeout: 3000 });
                  if (this.state.isRequestFromWard) {
                    this.props.history.push('/openboxes/');
                  } else {
                    window.location = '/openboxes/stockMovement/list?direction=INBOUND';
                  }
                }
              })
              .catch(() => this.props.hideSpinner());
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Saves changes made by user in this step and redirects to the shipment view page
   * @param {object} formValues
   * @public
   */
  // eslint-disable-next-line consistent-return
  saveAndExit(formValues) {
    const saveAndRedirect = (lineItems) => {
      this.props.showSpinner();
      return this.saveRequisitionItemsInCurrentStep(lineItems)
        .then(() => {
          let redirectTo = '/openboxes/stockMovement/list?direction=INBOUND';
          if (!this.props.supportedActivities.includes('MANAGE_INVENTORY') && this.props.supportedActivities.includes('SUBMIT_REQUEST')) {
            redirectTo = '/openboxes/dashboard';
          }
          window.location = redirectTo;
        })
        .catch(() => {
          this.props.hideSpinner();
        });
    };
    const errors = this.validate(formValues).lineItems;
    if (!errors.length) {
      const lineItems = _.filter(formValues.lineItems, item => !_.isEmpty(item));
      const zeroedLines = _.some(lineItems, item => !item.quantityRequested || item.quantityRequested === '0');
      if (zeroedLines || this.state.isRequestFromWard) {
        this.confirmSave(() => {
          saveAndRedirect(lineItems);
        });
      } else {
        saveAndRedirect(lineItems);
      }
    } else {
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmExit.label', 'Confirm save'),
        message: this.props.translate(
          'react.stockMovement.confirmExit.message',
          'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => {
              let redirectTo = '/openboxes/stockMovement/list?direction=INBOUND';
              if (!this.props.supportedActivities.includes('MANAGE_INVENTORY') && this.props.supportedActivities.includes('SUBMIT_REQUEST')) {
                redirectTo = '/openboxes/dashboard';
              }
              window.location = redirectTo;
            },
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
          },
        ],
      });
    }
  }

  /**
   * Saves list of requisition items in current step (without step change).
   * @param {object} lineItems
   * @public
   */
  saveItems(lineItems) {
    this.props.showSpinner();
    return this.saveRequisitionItemsInCurrentStep(lineItems)
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'), { timeout: 3000 });
      })
      .catch(() => {
        this.props.hideSpinner();
      });
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
          onClick: () => this.fetchAllData(),
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Removes chosen item from requisition's items list.
   * @param {string} itemId
   * @public
   */
  removeItem(itemId) {
    const removeItemsUrl = `/openboxes/api/stockMovementItems/${itemId}/removeItem`;

    return apiClient.delete(removeItemsUrl)
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('react.stockMovement.error.deleteRequisitionItem.label'));
      });
  }

  /**
   * Removes all items from requisition's items list.
   * @public
   */
  removeAll() {
    const removeItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/removeAllItems`;

    return apiClient.delete(removeItemsUrl)
      .then(() => {
        this.setState({
          totalCount: 1,
          currentLineItems: [],
          values: {
            ...this.state.values,
            lineItems: new Array(1).fill({ sortOrder: 100 }),
          },
        });
      })
      .catch(() => {
        this.fetchLineItems();
        return Promise.reject(new Error('react.stockMovement.error.deleteRequisitionItem.label'));
      });
  }

  /**
   * Transition to next stock movement status:
   * - 'CHECKING' if origin type is supplier.
   * - 'VERIFYING' if origin type is other than supplier.
   * @param {string} status
   * @public
   */
  transitionToNextStep(status) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status };
    const { movementNumber } = this.state.values;

    if (this.state.values.statusCode === 'CREATED') {
      return apiClient.post(url, payload)
        .then(() => {
          const translatedSubmitMessage = this.props.translate(
            'react.stockMovement.request.submitMessage.label',
            'Thank you for submitting your request. You can check the status of your request using stock movement number',
          );
          let redirectToURL = '';
          if (!this.props.supportedActivities.includes('MANAGE_INVENTORY') && this.props.supportedActivities.includes('SUBMIT_REQUEST')) {
            redirectToURL = '/openboxes/';
          } else {
            redirectToURL = '/openboxes/stockMovement/list?direction=INBOUND';
          }
          Alert.success(`${translatedSubmitMessage} ${movementNumber}`);
          this.props.history.push(redirectToURL);
        });
    }
    return Promise.resolve();
  }

  /**
   * Saves changes made by user in this step and go back to previous page
   * @param {object} values
   * @param {boolean} invalid
   * @public
   */
  // eslint-disable-next-line consistent-return
  previousPage(values, invalid) {
    const saveAndRedirect = (lineItems) => {
      this.props.showSpinner();
      return this.saveRequisitionItemsInCurrentStep(lineItems)
        .then(() => this.props.previousPage(values))
        .catch(() => {
          this.props.hideSpinner();
        });
    };
    if (!invalid) {
      const lineItems = _.filter(values.lineItems, item => !_.isEmpty(item));
      const zeroedLines = _.some(lineItems, item => !item.quantityRequested || item.quantityRequested === '0');
      if (zeroedLines || this.state.isRequestFromWard) {
        this.confirmSave(() => {
          saveAndRedirect(lineItems);
        });
      } else {
        saveAndRedirect(lineItems);
      }
    } else {
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmPreviousPage.label', 'Validation error'),
        message: this.props.translate('react.stockMovement.confirmPreviousPage.message.label', 'Cannot save due to validation error on page'),
        buttons: [
          {
            label: this.props.translate('react.stockMovement.confirmPreviousPage.correctError.label', 'Correct error'),
          },
          {
            label: this.props.translate('react.stockMovement.confirmPreviousPage.continue.label', 'Continue (lose unsaved work)'),
            onClick: () => this.props.previousPage(values),
          },
        ],
      });
    }
  }

  updateProductData(product, values, index) {
    if (product) {
      if (this.state.isRequestFromWard) {
        const url = `/openboxes/api/products/${product.id}/productDemand?originId=${this.state.values.origin.id}&destinationId=${this.state.values.destination.id}`;

        apiClient.get(url)
          .then((response) => {
            const monthlyDemand = parseFloat(response.data.monthlyDemand);
            const quantityRequested = monthlyDemand - (response.data.quantityOnHand || 0);
            this.setState({
              values: update(values, {
                lineItems: {
                  [index]: {
                    product: { $set: product },
                    quantityOnHand: { $set: '' },
                    monthlyDemand: { $set: monthlyDemand },
                    quantityRequested: { $set: quantityRequested >= 0 ? quantityRequested : 0 },
                  },
                },
              }),
            });
          })
          .catch(this.props.hideSpinner());
      } else {
        const url = `/openboxes/api/products/${product.id}/productAvailabilityAndDemand?locationId=${this.state.values.destination.id}`;

        apiClient.get(url)
          .then((response) => {
            const { monthlyDemand, quantityAvailable, quantityOnHand } = response.data;
            const quantityRequested = monthlyDemand - quantityAvailable > 0 ?
              monthlyDemand - quantityAvailable : 0;
            this.setState({
              values: update(values, {
                lineItems: {
                  [index]: {
                    product: { $set: product },
                    quantityOnHand: { $set: quantityOnHand },
                    quantityAvailable: { $set: quantityAvailable },
                    monthlyDemand: { $set: monthlyDemand },
                    quantityRequested: { $set: quantityRequested },
                  },
                },
              }),
            });
          })
          .catch(this.props.hideSpinner());
      }
    } else {
      this.setState({
        values: update(values, {
          lineItems: {
            [index]: {
              product: { $set: null },
              quantityOnHand: { $set: '' },
              quantityAvailable: { $set: '' },
              monthlyDemand: { $set: '' },
              quantityRequested: { $set: '' },
            },
          },
        }),
      });
    }
  }

  render() {
    return (
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            <span className="buttons-container">
              <label
                htmlFor="csvInput"
                className={`float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs ${this.state.isRequestFromWard ? 'disabled' : ''}`}
                title={this.state.isRequestFromWard ? 'Temporarily disabled' : ''}
              >
                <span><i className="fa fa-download pr-2" /><Translate id="react.default.button.importTemplate.label" defaultMessage="Import template" /></span>
                <input
                  id="csvInput"
                  type="file"
                  style={{ display: 'none' }}
                  onChange={this.importTemplate}
                  disabled={this.state.isRequestFromWard}
                  onClick={(event) => {
                    // eslint-disable-next-line no-param-reassign
                    event.target.value = null;
                  }}
                  accept=".csv"
                />
              </label>
              <button
                type="button"
                onClick={() => this.exportTemplate(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span>
                  <i className="fa fa-upload pr-2" />
                  <Translate id="react.default.button.exportTemplate.label" defaultMessage="Export template" />
                </span>
              </button>
              <button
                type="button"
                onClick={() => this.refresh()}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span>
                  <i className="fa fa-refresh pr-2" />
                  <Translate id="react.default.button.refresh.label" defaultMessage="Reload" />
                </span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.save(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span>
                  <i className="fa fa-save pr-2" />
                  <Translate id="react.default.button.save.label" defaultMessage="Save" />
                </span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.saveAndExit(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span>
                  <i className="fa fa-sign-out pr-2" />
                  <Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" />
                </span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.removeAll()}
                className="float-right mb-1 btn btn-outline-danger align-self-end ml-1 btn-xs"
              >
                <span>
                  <i className="fa fa-remove pr-2" />
                  <Translate id="react.default.button.deleteAll.label" defaultMessage="Delete all" />
                </span>
              </button>
              <button
                type="button"
                className="float-right mb-1 btn btn-outline-danger align-self-end ml-1 btn-xs"
                onClick={() => this.cancelRequest()}
              >
                <span>
                  <i className="fa fa-remove pr-2" />
                  <Translate id="react.stockMovement.request.cancel.label" defaultMessage="Cancel Request" />
                </span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(this.getFields(), (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    stocklist: values.stocklist,
                    removeItem: this.removeItem,
                    originId: this.props.initialValues.origin.id,
                    getSortOrder: this.getSortOrder,
                    newItemAdded: this.newItemAdded,
                    newItem: this.state.newItem,
                    totalCount: this.state.totalCount,
                    loadMoreRows: this.loadMoreRows,
                    isRowLoaded: this.isRowLoaded,
                    updateTotalCount: this.updateTotalCount,
                    isPaginated: this.props.isPaginated,
                    isFromOrder: this.state.values.isFromOrder,
                    updateRow: this.updateRow,
                    values,
                    isFirstPageLoaded: this.state.isFirstPageLoaded,
                    updateProductData: this.updateProductData,
                    calculateQtyRequested: this.calculateQuantityRequested,
                  }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="submit"
                  disabled={invalid}
                  onClick={() => this.previousPage(values, invalid)}
                  className="btn btn-outline-primary btn-form btn-xs"
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  onClick={() => this.submitRequest(values.lineItems)}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                  disabled={
                    invalid || !_.some(values.lineItems, item =>
                        item.product && _.parseInt(item.quantityRequested))
                  }
                ><Translate id="react.default.button.submitRequest.label" defaultMessage="Submit request" />
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
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  minimumExpirationDate: state.session.minimumExpirationDate,
  isPaginated: state.session.isPaginated,
  pageSize: state.session.pageSize,
  currentLocationId: state.session.currentLocation.id,
  supportedActivities: state.session.supportedActivities,
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
  fetchUsers,
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(AddItemsPage));

AddItemsPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({
    origin: PropTypes.shape({
      id: PropTypes.string,
    }),
    hasManageInventory: PropTypes.bool,
  }).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  nextPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  pageSize: PropTypes.number.isRequired,
  currentLocationId: PropTypes.string.isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};
