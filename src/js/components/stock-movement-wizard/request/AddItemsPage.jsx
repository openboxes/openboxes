import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import Alert from 'react-s-alert';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import moment from 'moment';
import fileDownload from 'js-file-download';
import update from 'immutability-helper';

import 'react-confirm-alert/src/react-confirm-alert.css';

import TextField from '../../form-elements/TextField';
import SelectField from '../../form-elements/SelectField';
import ArrayField from '../../form-elements/ArrayField';
import ButtonField from '../../form-elements/ButtonField';
import LabelField from '../../form-elements/LabelField';
import { renderFormField } from '../../../utils/form-utils';
import { showSpinner, hideSpinner, fetchUsers } from '../../../actions';
import apiClient from '../../../utils/apiClient';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';
import { debounceProductsFetch } from '../../../utils/option-utils';
import renderHandlingIcons from '../../../utils/product-handling-icons';

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
      removeItem(fieldValue.id).then(() => removeRow());
      updateTotalCount(-1);
    } : () => { updateTotalCount(-1); removeRow(); },
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const NO_STOCKLIST_FIELDS = {
  lineItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    // eslint-disable-next-line react/prop-types
    addButton: ({
      // eslint-disable-next-line react/prop-types
      addRow, getSortOrder, newItemAdded, updateTotalCount,
    }) => (
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
    ),
    fields: {
      product: {
        fieldKey: '',
        type: SelectField,
        label: 'react.stockMovement.requestedProduct.label',
        defaultMessage: 'Requested product',
        headerAlign: 'left',
        flexWidth: '9.5',
        attributes: {
          async: true,
          openOnClick: false,
          autoload: false,
          filterOptions: options => options,
          cache: false,
          options: [],
          showValueTooltip: true,
          className: 'text-left',
          optionRenderer: option => (
            <strong style={{ color: option.color ? option.color : 'black' }} className="d-flex align-items-center">
              {option.label}
              &nbsp;
              {renderHandlingIcons(option.value ? option.value.handlingIcons : [])}
            </strong>
          ),
          valueRenderer: option => (
            <span className="d-flex align-items-center">
              <span className="text-truncate">
                {option.label}
              </span>
              &nbsp;
              {renderHandlingIcons(option ? option.handlingIcons : [])}
            </span>
          ),
        },
        getDynamicAttr: ({
          debouncedProductsFetch, rowIndex, rowCount, updateProductData, values,
        }) => ({
          onChange: value => updateProductData(value, values, rowIndex),
          loadOptions: debouncedProductsFetch,
          autoFocus: rowIndex === rowCount - 1,
        }),
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
      monthlyDemand: {
        type: LabelField,
        label: 'react.stockMovement.demandPerMonth',
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
        flexWidth: '2.5',
        attributes: {
          type: 'number',
        },
        fieldKey: '',
        getDynamicAttr: ({
          updateRow, values, rowIndex,
        }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
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
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const STOCKLIST_FIELDS_PUSH_TYPE = {
  lineItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    addButton: ({
      // eslint-disable-next-line react/prop-types
      addRow, getSortOrder, newItemAdded, updateTotalCount,
    }) => (
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
    ),
    fields: {
      product: {
        fieldKey: 'disabled',
        type: SelectField,
        label: 'react.stockMovement.requestedProduct.label',
        defaultMessage: 'Requested product',
        headerAlign: 'left',
        flexWidth: '9',
        attributes: {
          async: true,
          openOnClick: false,
          autoload: false,
          filterOptions: options => options,
          cache: false,
          options: [],
          showValueTooltip: true,
          className: 'text-left',
          optionRenderer: option => (
            <strong style={{ color: option.color ? option.color : 'black' }} className="d-flex align-items-center">
              {option.label}
              &nbsp;
              {renderHandlingIcons(option.value ? option.value.handlingIcons : [])}
            </strong>
          ),
          valueRenderer: option => (
            <span className="d-flex align-items-center">
              <span className="text-truncate">
                {option.label}
              </span>
              &nbsp;
              {renderHandlingIcons(option ? option.handlingIcons : [])}
            </span>
          ),
        },
        getDynamicAttr: ({
          fieldValue, debouncedProductsFetch, rowIndex, rowCount, newItem,
        }) => ({
          disabled: !!fieldValue,
          loadOptions: debouncedProductsFetch,
          autoFocus: newItem && rowIndex === rowCount - 1,
        }),
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
      quantityOnHand: {
        type: LabelField,
        label: 'react.stockMovement.quantityOnHand.label',
        defaultMessage: 'QoH',
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
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const STOCKLIST_FIELDS_PULL_TYPE = {
  lineItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    addButton: ({
      // eslint-disable-next-line react/prop-types
      addRow, getSortOrder, newItemAdded, updateTotalCount,
    }) => (
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
    ),
    fields: {
      product: {
        fieldKey: 'disabled',
        type: SelectField,
        label: 'react.stockMovement.requestedProduct.label',
        defaultMessage: 'Requested product',
        headerAlign: 'left',
        flexWidth: '9',
        attributes: {
          async: true,
          openOnClick: false,
          autoload: false,
          filterOptions: options => options,
          cache: false,
          options: [],
          showValueTooltip: true,
          className: 'text-left',
          optionRenderer: option => (
            <strong style={{ color: option.color ? option.color : 'black' }} className="d-flex align-items-center">
              {option.label}
              &nbsp;
              {renderHandlingIcons(option.value ? option.value.handlingIcons : [])}
            </strong>
          ),
          valueRenderer: option => (
            <span className="d-flex align-items-center">
              <span className="text-truncate">
                {option.label}
              </span>
                &nbsp;
              {renderHandlingIcons(option ? option.handlingIcons : [])}
            </span>
          ),
        },
        getDynamicAttr: ({
          fieldValue, debouncedProductsFetch, rowIndex, rowCount, newItem,
        }) => ({
          disabled: !!fieldValue,
          loadOptions: debouncedProductsFetch,
          autoFocus: newItem && rowIndex === rowCount - 1,
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
      quantityOnHand: {
        type: LabelField,
        label: 'react.stockMovement.quantityOnHand.label',
        defaultMessage: 'QoH',
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
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const REPLENISHMENT_TYPE_PULL = 'PULL';

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

    this.debouncedProductsFetch = debounceProductsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
      this.props.initialValues.origin.id,
    );
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
    const lineItemsToBeAdded = _.filter(lineItems, item =>
      !item.statusCode && item.quantityRequested && item.quantityRequested !== '0' && item.product);
    const lineItemsWithStatus = _.filter(lineItems, item => item.statusCode);
    const lineItemsToBeUpdated = [];
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

    if (this.state.values.lineItems.length === 0 && !data.length) {
      lineItemsData = new Array(1).fill({ sortOrder: 100 });
    } else if (_.get(this.state.values.replenishmentType, 'name') === REPLENISHMENT_TYPE_PULL) {
      lineItemsData = _.map(
        data,
        (val) => {
          const { quantityRequested, demandPerReplenishmentPeriod, quantityOnHand } = val;
          const qtyRequested = quantityRequested || demandPerReplenishmentPeriod - quantityOnHand;
          return {
            ...val,
            disabled: true,
            quantityRequested: qtyRequested >= 0 ? qtyRequested : 0,
            product: {
              ...val.product,
              label: `${val.productCode} ${val.product.name}`,
            },
          };
        },
      );
    } else {
      lineItemsData = _.map(
        data,
        val => ({
          ...val,
          disabled: true,
          product: {
            ...val.product,
            label: `${val.productCode} ${val.product.name}`,
          },
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
      if (!_.isNil(item.product) && (_.isNil(item.quantityRequested)
        || item.quantityRequested < 0)) {
        errors.lineItems[key] = { quantityRequested: 'react.stockMovement.error.enterQuantity.label' };
      }
      if (!_.isEmpty(item.boxName) && _.isEmpty(item.palletName)) {
        errors.lineItems[key] = { boxName: 'react.stockMovement.error.boxWithoutPallet.label' };
      }
      const dateRequested = moment(item.expirationDate, 'MM/DD/YYYY');
      if (date.diff(dateRequested) > 0) {
        errors.lineItems[key] = { expirationDate: 'react.stockMovement.error.invalidDate.label' };
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
    this.props.showSpinner();

    const { movementNumber, stockMovementId } = formValues;
    const url = `/openboxes/stockMovement/exportCsv/${stockMovementId}`;
    this.saveRequisitionItemsInCurrentStep(lineItems)
      .then(() => {
        apiClient.get(url, { responseType: 'blob' })
          .then((response) => {
            fileDownload(response.data, `ItemList${movementNumber ? `-${movementNumber}` : ''}.csv`, 'text/csv');
            this.props.hideSpinner();
          })
          .catch(() => this.props.hideSpinner());
      });
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
      message: this.props.translate(
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
      message: _.map(items, item =>
        <p key={item.sortOrder}>{item.product.label} {item.quantityRequested}</p>),
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
        const { hasManageInventory } = resp.data.data;
        const { statusCode } = resp.data.data;
        const { totalCount } = resp.data;

        this.setState({
          values: {
            ...this.state.values,
            hasManageInventory,
            statusCode,
          },
          totalCount: totalCount === 0 ? 1 : totalCount,
        }, () => this.props.hideSpinner());
      });
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
        .catch(() => Promise.reject(new Error('react.stockMovement.error.saveRequisitionItems.label')));
    }

    return Promise.resolve();
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
        .then((resp) => {
          const { lineItems } = resp.data.data;
          const lineItemsBackendData = _.map(
            lineItems,
            val => ({
              ...val,
              product: {
                ...val.product,
                label: `${val.productCode} ${val.product.name}`,
              },
              quantityOnHand: _.find(
                this.state.values.lineItems,
                lineItem => lineItem.sortOrder === val.sortOrder,
              ).quantityOnHand,
              monthlyDemand: _.find(
                this.state.values.lineItems,
                lineItem => lineItem.sortOrder === val.sortOrder,
              ).monthlyDemand,
              demandPerReplenishmentPeriod: _.find(
                this.state.values.lineItems,
                lineItem => lineItem.sortOrder === val.sortOrder,
              ).demandPerReplenishmentPeriod,
            }),
          );

          this.setState({
            values:
              { ...this.state.values, lineItems: lineItemsBackendData },
            totalCount: lineItems.length,
            currentLineItems: lineItemsBackendData,
          });
        })
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

    if (_.some(lineItems, item => !item.quantityRequested || item.quantityRequested === '0')) {
      this.confirmSave(() => this.saveItems(lineItems));
    } else {
      this.saveItems(lineItems);
    }
  }

  /**
   * Saves changes made by user in this step and redirects to the shipment view page
   * @param {object} formValues
   * @public
   */
  saveAndExit(formValues) {
    const errors = this.validate(formValues).lineItems;
    if (!errors.length) {
      this.saveRequisitionItemsInCurrentStep(formValues.lineItems)
        .then(() => {
          window.location = '/openboxes/stockMovement/list?direction=INBOUND';
        });
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
            onClick: () => { window.location = '/openboxes/stockMovement/list?direction=INBOUND'; },
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

    this.saveRequisitionItemsInCurrentStep(lineItems)
      .then(() => {
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
          window.location = `/openboxes/stockMovement/list?direction=INBOUND&movementNumber=${movementNumber}&submitted=true`;
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
  previousPage(values, invalid) {
    if (!invalid) {
      this.saveRequisitionItemsInCurrentStep(values.lineItems)
        .then(() => this.props.previousPage(values));
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
      const url = `/openboxes/api/products/${product.id}/productAvailabilityAndDemand?locationId=${this.state.values.destination.id}`;

      apiClient.get(url)
        .then((response) => {
          const monthlyDemand = parseFloat(response.data.monthlyDemand.replace(',', ''));
          const quantityRequested = monthlyDemand - response.data.quantityOnHand > 0 ?
            monthlyDemand - response.data.quantityOnHand : 0;
          this.setState({
            values: update(values, {
              lineItems: {
                [index]: {
                  product: { $set: product },
                  quantityOnHand: { $set: response.data.quantityOnHand },
                  monthlyDemand: { $set: monthlyDemand },
                  quantityRequested: { $set: quantityRequested },
                },
              },
            }),
          });
        })
        .catch(this.props.hideSpinner());
    } else {
      this.setState({
        values: update(values, {
          lineItems: {
            [index]: {
              product: { $set: null },
              quantityOnHand: { $set: '' },
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
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-download pr-2" /><Translate id="react.default.button.importTemplate.label" defaultMessage="Import template" /></span>
                <input
                  id="csvInput"
                  type="file"
                  style={{ display: 'none' }}
                  onChange={this.importTemplate}
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
                <span><i className="fa fa-upload pr-2" /><Translate id="react.default.button.exportTemplate.label" defaultMessage="Export template" /></span>
              </button>
              <button
                type="button"
                onClick={() => this.refresh()}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-refresh pr-2" /><Translate id="react.default.button.refresh.label" defaultMessage="Reload" /></span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.save(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-save pr-2" /><Translate id="react.default.button.save.label" defaultMessage="Save" /></span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.saveAndExit(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.removeAll()}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs"
              >
                <span><i className="fa fa-remove pr-2" /><Translate id="react.default.button.deleteAll.label" defaultMessage="Delete all" /></span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(this.getFields(), (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    stocklist: values.stocklist,
                    removeItem: this.removeItem,
                    debouncedProductsFetch: this.debouncedProductsFetch,
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
                  onClick={() => {
                    if (!invalid) {
                      this.confirmSubmit(() => this.saveRequisitionItems(_.filter(values.lineItems, val => !_.isEmpty(val) && val.product)).then(() => this.transitionToNextStep('REQUESTED')));
                    }
                  }}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                  disabled={invalid}
                ><Translate id="react.default.button.next.label" defaultMessage="Next" />
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
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  minimumExpirationDate: state.session.minimumExpirationDate,
  isPaginated: state.session.isPaginated,
  pageSize: state.session.pageSize,
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchUsers,
})(AddItemsPage));

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
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  pageSize: PropTypes.number.isRequired,
};
