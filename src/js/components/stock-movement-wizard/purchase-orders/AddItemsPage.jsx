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
import update from 'immutability-helper';

import 'react-confirm-alert/src/react-confirm-alert.css';

import TextField from '../../form-elements/TextField';
import SelectField from '../../form-elements/SelectField';
import ArrayField from '../../form-elements/ArrayField';
import ButtonField from '../../form-elements/ButtonField';
import LabelField from '../../form-elements/LabelField';
import DateField from '../../form-elements/DateField';
import { renderFormField } from '../../../utils/form-utils';
import { showSpinner, hideSpinner, fetchUsers } from '../../../actions';
import apiClient from '../../../utils/apiClient';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';
import { debounceProductsFetch } from '../../../utils/option-utils';

const DELETE_BUTTON_FIELD = {
  type: ButtonField,
  label: 'react.default.button.delete.label',
  defaultMessage: 'Delete',
  flexWidth: '1',
  fieldKey: '',
  buttonLabel: 'react.default.button.delete.label',
  buttonDefaultMessage: 'Delete',
  getDynamicAttr: ({
    fieldValue, removeRow, updateTotalCount,
  }) => ({
    onClick: fieldValue && fieldValue.id ?
      () => null :
      () => { updateTotalCount(-1); removeRow(); },
    disabled: fieldValue && fieldValue.id,
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const VENDOR_FIELDS = {
  lineItems: {
    type: ArrayField,
    arrowsNavigation: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    fields: {
      orderNumber: {
        type: LabelField,
        label: 'react.stockMovement.orderNumber.label',
        defaultMessage: 'Order number',
        flexWidth: '1',
      },
      palletName: {
        type: TextField,
        label: 'react.stockMovement.packLevel1.label',
        defaultMessage: 'Pack level 1',
        flexWidth: '1',
        getDynamicAttr: ({
          rowIndex, rowCount, values, updateRow,
        }) => ({
          autoFocus: rowIndex === rowCount - 1,
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      boxName: {
        type: TextField,
        label: 'react.stockMovement.packLevel2.label',
        defaultMessage: 'Pack level 2',
        flexWidth: '1',
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      product: {
        type: SelectField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        flexWidth: '4',
        required: true,
        attributes: {
          className: 'text-left',
          async: true,
          openOnClick: false,
          autoload: false,
          filterOptions: options => options,
          cache: false,
          options: [],
          disabled: true,
          showValueTooltip: true,
          optionRenderer: option => <strong style={{ color: option.color ? option.color : 'black' }}>{option.label}</strong>,
        },
        getDynamicAttr: ({ debouncedProductsFetch }) => ({
          loadOptions: debouncedProductsFetch,
        }),
      },
      lotNumber: {
        type: TextField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      expirationDate: {
        type: DateField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1.5',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
          autoComplete: 'off',
          placeholderText: 'MM/DD/YYYY',
        },
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      quantityRequested: {
        type: TextField,
        label: 'react.stockMovement.quantity.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
        required: true,
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      recipient: {
        type: SelectField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1.5',
        getDynamicAttr: ({
          recipients, rowIndex, values, updateRow,
        }) => ({
          options: recipients,
          onBlur: () => updateRow(values, rowIndex),
        }),
        attributes: {
          labelKey: 'name',
          openOnClick: false,
        },
      },
      split: {
        type: ButtonField,
        label: 'react.stockMovement.splitLine.label',
        defaultMessage: 'Split',
        flexWidth: '1',
        fieldKey: '',
        buttonLabel: 'react.stockMovement.splitLine.label',
        buttonDefaultMessage: 'Split line',
        getDynamicAttr: ({
          fieldValue, addRow, rowIndex, updateTotalCount,
        }) => ({
          onClick: () => {
            updateTotalCount(1);
            addRow({
              product: {
                ...fieldValue.product,
                label: `${fieldValue.product.productCode} ${fieldValue.product.name}`,
              },
              recipient: fieldValue.recipient,
              sortOrder: fieldValue.sortOrder + 1,
              orderItemId: fieldValue.orderItemId,
              referenceId: fieldValue.id,
              orderNumber: fieldValue.orderNumber,
            }, rowIndex);
          },
        }),
        attributes: {
          className: 'btn btn-outline-success',
        },
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

/* eslint class-methods-use-this: ["error",{ "exceptMethods": ["getLineItemsToBeSaved"] }] */
/**
 * The second step of stock movement where user can add items to stock list.
 * This component supports three different cases: with or without stocklist
 * when movement is from a depot and when movement is from a vendor.
 */
class AddItemsPage extends Component {
  static updateSortOrder(lineItems) {
    return _.map(lineItems, (item, rowIndex) => ({
      ...item,
      sortOrder: (item.sortOrder - (item.sortOrder % 100)) + rowIndex + 1,
    }));
  }

  constructor(props) {
    super(props);
    this.state = {
      values: { ...this.props.initialValues, lineItems: [] },
      totalCount: 0,
    };

    this.props.showSpinner();
    this.confirmSave = this.confirmSave.bind(this);
    this.confirmTransition = this.confirmTransition.bind(this);
    this.validate = this.validate.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateTotalCount = this.updateTotalCount.bind(this);
    this.updateRow = this.updateRow.bind(this);

    this.debouncedProductsFetch = debounceProductsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
      this.props.initialValues.origin.id,
    );
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
  }

  /**
   * Returns an array of new stock movement's items and items to be
   * updated (comparing to previous state of line items).
   * @param {object} lineItems
   * @public
   */
  getLineItemsToBeSaved(lineItems) {
    const items = AddItemsPage.updateSortOrder(lineItems);
    const lineItemsToBeAdded = _.filter(items, item => !item.id);
    const lineItemsToBeUpdated = _.filter(items, item => item.id);

    return [].concat(
      _.map(lineItemsToBeAdded, item => ({
        'product.id': item.product.id,
        quantityRequested: item.quantityRequested,
        palletName: item.palletName,
        boxName: item.boxName,
        lotNumber: item.lotNumber,
        expirationDate: item.expirationDate,
        'recipient.id': _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '',
        sortOrder: item.sortOrder,
        orderItemId: item.orderItemId,
      })),
      _.map(lineItemsToBeUpdated, item => ({
        id: item.id,
        'product.id': item.product.id,
        quantityRequested: item.quantityRequested,
        palletName: item.palletName,
        boxName: item.boxName,
        lotNumber: item.lotNumber,
        expirationDate: item.expirationDate,
        'recipient.id': _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '',
        sortOrder: item.sortOrder,
        orderItemId: item.orderItemId,
      })),
    );
  }

  setLineItems(response) {
    const { data } = response.data;
    let lineItemsData;

    if (this.state.values.lineItems.length === 0 && !data.length) {
      lineItemsData = new Array(1).fill({ sortOrder: 100 });
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
          referenceId: val.id,
        }),
      );
    }

    _.sort(lineItemsData, ['sortOrder']);

    this.setState({
      currentLineItems: this.props.isPaginated ?
        _.uniqBy(_.concat(this.state.currentLineItems, data), 'id') : data,
      values: {
        ...this.state.values,
        lineItems: this.props.isPaginated ?
          _.uniqBy(_.concat(this.state.values.lineItems, lineItemsData), 'id') : lineItemsData,
      },
      totalCount: lineItemsData.length > this.state.totalCount ?
        lineItemsData.length : this.state.totalCount,
    }, () => this.props.hideSpinner());
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
      if (!_.isNil(item.product) && (!item.quantityRequested || item.quantityRequested < 0)) {
        errors.lineItems[key] = { quantityRequested: 'react.stockMovement.error.enterQuantity.label' };
      }
      if (!_.isEmpty(item.boxName) && _.isEmpty(item.palletName)) {
        errors.lineItems[key] = { boxName: 'react.stockMovement.error.boxWithoutPallet.label' };
      }
      const dateRequested = moment(item.expirationDate, 'MM/DD/YYYY');
      if (date.diff(dateRequested) > 0) {
        errors.lineItems[key] = { expirationDate: 'react.stockMovement.error.invalidDate.label' };
      }
      const splitItems = _.filter(values.lineItems, lineItem =>
        lineItem.referenceId === item.referenceId);
      if (!item.id) {
        const originalItem = _.find(splitItems, original => original.id);
        const requestedQuantity = _.reduce(
          splitItems, (sum, val) =>
            (sum + (val.quantityRequested ? _.toInteger(val.quantityRequested) : 0)),
          0,
        );
        if (requestedQuantity !== originalItem.quantityRequired) {
          _.forEach(values.lineItems, (lineItem, lineItemKey) => {
            _.forEach(splitItems, (splitItem) => {
              if (lineItem === splitItem) {
                errors.lineItems[lineItemKey] = { quantityRequested: 'react.stockMovement.error.changedSplitQuantity.label' };
              }
            });
          });
        }
      } else if (splitItems.length === 1 &&
        item.quantityRequired !== _.toInteger(item.quantityRequested)) {
        errors.lineItems[key] = { quantityRequested: 'react.stockMovement.error.changedQuantity.label' };
      }
    });
    return errors;
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
  fetchAllData(forceFetch) {
    if (!this.props.recipientsFetched || forceFetch) {
      this.props.fetchUsers();
    }

    this.fetchAddItemsPageData();
    // TODO: Fix pagination support
    // if (!this.props.isPaginated) {
    //   this.fetchLineItems();
    // }
  }

  /**
   * Fetches 2nd step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=2`;

    return apiClient.get(url)
      .then((response) => {
        this.setLineItems(response);
      })
      .catch(err => err);
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
        const { statusCode, lineItems } = resp.data.data;
        const { totalCount } = resp.data;

        this.setState({
          values: {
            ...this.state.values,
            hasManageInventory,
            statusCode,
            // TODO: Fix pagination support
            lineItems: _.map(
              _.sortBy(lineItems, ['sortOrder']),
              val => ({
                ...val,
                disabled: true,
                product: {
                  ...val.product,
                  label: `${val.productCode} ${val.product.name}`,
                },
                referenceId: val.id,
              }),
            ),
          },
          totalCount: totalCount === 0 ? 1 : totalCount,
        }, () => this.props.hideSpinner());
      });
  }

  isRowLoaded({ index }) {
    return !!this.state.values.lineItems[index];
  }

  loadMoreRows({ startIndex, stopIndex }) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${stopIndex - startIndex > 0 ? stopIndex - startIndex : 1}&stepNumber=2`;
    apiClient.get(url)
    // TODO: Fix pagination support
      .then(() => {
        // this.setLineItems(response);
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
        .then(() => this.fetchAddItemsPageData());
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
          window.location = `/openboxes/stockMovement/show/${formValues.stockMovementId}`;
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
            onClick: () => { window.location = `/openboxes/stockMovement/show/${formValues.stockMovementId}`; },
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
          onClick: () => this.fetchAllData(true),
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
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

    if (this.state.values.statusCode === 'CREATED') {
      return apiClient.post(url, payload);
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
    this.props.showSpinner();
    if (!invalid) {
      this.saveRequisitionItemsInCurrentStep(values.lineItems)
        .then(() => {
          window.location = `/openboxes/order/redirectFromStockMovement/${values.stockMovementId}`;
        }).catch(() => this.props.hideSpinner());
    } else {
      this.props.hideSpinner();
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmPreviousPage.label', 'Validation error'),
        message: this.props.translate('react.stockMovement.confirmPreviousPage.message.label', 'Cannot save due to validation error on page'),
        buttons: [
          {
            label: this.props.translate('react.stockMovement.confirmPreviousPage.correctError.label', 'Correct error'),
          },
          {
            label: this.props.translate('react.stockMovement.confirmPreviousPage.continue.label', 'Continue (lose unsaved work)'),
            onClick: () => {
              window.location = `/openboxes/order/redirectFromStockMovement/${values.stockMovementId}`;
            },
          },
        ],
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
            <span>
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
            </span>
            <form onSubmit={handleSubmit}>
              {_.map(VENDOR_FIELDS, (fieldConfig, fieldName) =>
                renderFormField(fieldConfig, fieldName, {
                  stocklist: values.stocklist,
                  recipients: this.props.recipients,
                  debouncedProductsFetch: this.debouncedProductsFetch,
                  totalCount: this.state.totalCount,
                  loadMoreRows: this.loadMoreRows,
                  isRowLoaded: this.isRowLoaded,
                  updateTotalCount: this.updateTotalCount,
                  isPaginated: this.props.isPaginated,
                  isFromOrder: this.state.values.isFromOrder,
                  updateRow: this.updateRow,
                  values,
                }))}
              <div>
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
                      this.nextPage(values);
                    }
                  }}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                  disabled={!_.some(values.lineItems, item => !_.isEmpty(item))
                    || invalid}
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
  recipients: state.users.data,
  recipientsFetched: state.users.fetched,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  minimumExpirationDate: state.session.minimumExpirationDate,
  hasPackingSupport: state.session.currentLocation.hasPackingSupport,
  isPaginated: state.session.isPaginated,
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
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  nextPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function fetching users */
  fetchUsers: PropTypes.func.isRequired,
  /** Array of available recipients  */
  recipients: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  /** Indicator if recipients' data is fetched */
  recipientsFetched: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
};
