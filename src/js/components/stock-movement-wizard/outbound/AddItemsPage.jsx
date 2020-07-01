import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import fileDownload from 'js-file-download';
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
    fieldValue, removeItem, removeRow, showOnly, updateTotalCount,
  }) => ({
    onClick: fieldValue && fieldValue.id ? () => {
      removeItem(fieldValue.id).then(() => removeRow());
      updateTotalCount(-1);
    } : () => { updateTotalCount(-1); removeRow(); },
    disabled: (fieldValue && fieldValue.statusCode === 'SUBSTITUTED') || showOnly,
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
    addButton: ({
      // eslint-disable-next-line react/prop-types
      addRow, getSortOrder, showOnly, updateTotalCount,
    }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        disabled={showOnly}
        onClick={() => {
          updateTotalCount(1);
          addRow({ sortOrder: getSortOrder() });
        }
        }
      ><Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
      </button>
    ),
    fields: {
      product: {
        fieldKey: 'disabled',
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
          optionRenderer: option => <strong style={{ color: option.color ? option.color : 'black' }}>{option.label}</strong>,
        },
        getDynamicAttr: ({
          fieldValue, debouncedProductsFetch, rowIndex, rowCount,
        }) => ({
          disabled: !!fieldValue,
          loadOptions: debouncedProductsFetch,
          autoFocus: rowIndex === rowCount - 1,
        }),
      },
      quantityRequested: {
        type: TextField,
        label: 'react.stockMovement.quantity.label',
        defaultMessage: 'Quantity',
        flexWidth: '2.5',
        attributes: {
          type: 'number',
        },
        fieldKey: '',
        getDynamicAttr: ({
          fieldValue, updateRow, values, rowIndex,
        }) => ({
          disabled: (fieldValue && fieldValue.statusCode === 'SUBSTITUTED') || _.isNil(fieldValue && fieldValue.product),
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      recipient: {
        type: SelectField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '2.5',
        fieldKey: '',
        getDynamicAttr: ({
          fieldValue, recipients, addRow, rowCount, rowIndex, getSortOrder,
          updateTotalCount, updateRow, values,
        }) => ({
          options: recipients,
          disabled: (fieldValue && fieldValue.statusCode === 'SUBSTITUTED') || _.isNil(fieldValue && fieldValue.product),
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
        attributes: {
          labelKey: 'name',
          openOnClick: false,
        },
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const STOCKLIST_FIELDS = {
  lineItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
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
      ><Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
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
          optionRenderer: option => <strong style={{ color: option.color ? option.color : 'black' }}>{option.label}</strong>,
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
      quantityRequested: {
        type: TextField,
        label: 'react.stockMovement.neededQuantity.label',
        defaultMessage: 'Needed Qty',
        flexWidth: '1.7',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          addRow, rowCount, rowIndex, getSortOrder, updateTotalCount, updateRow, values,
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
   * Returns proper fields depending on origin type or if stock list is chosen.
   * @public
   */
  getFields() {
    if (_.get(this.state.values.stocklist, 'id')) {
      return STOCKLIST_FIELDS;
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
      const oldRecipient = oldItem.recipient && _.isObject(oldItem.recipient) ?
        oldItem.recipient.id : oldItem.recipient;
      const newRecipient = item.recipient && _.isObject(item.recipient) ?
        item.recipient.id : item.recipient;

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
      } else if (newQty !== oldQty || newRecipient !== oldRecipient) {
        lineItemsToBeUpdated.push(item);
      }
    });

    return [].concat(
      _.map(lineItemsToBeAdded, item => ({
        'product.id': item.product.id,
        quantityRequested: item.quantityRequested,
        'recipient.id': _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '',
        sortOrder: item.sortOrder,
      })),
      _.map(lineItemsToBeUpdated, item => ({
        id: item.id,
        'product.id': item.product.id,
        quantityRequested: item.quantityRequested,
        'recipient.id': _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '',
        sortOrder: item.sortOrder,
      })),
    );
  }

  getSortOrder() {
    this.setState({
      sortOrder: this.state.sortOrder + 100,
    });

    return this.state.sortOrder;
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
      totalCount: lineItemsData.length > this.state.totalCount ?
        lineItemsData.length : this.state.totalCount,
    }, () => this.props.hideSpinner());
  }

  updateTotalCount(value) {
    this.setState({
      totalCount: this.state.totalCount + value === 0 ? 1 : this.state.totalCount + value,
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
    });
    return errors;
  }

  updateRow(values, index) {
    const item = values.lineItems[index];
    this.setState({
      values: update(values, {
        lineItems: { [index]: { $set: item } },
      }),
    });
  }

  newItemAdded() {
    this.setState({
      newItem: true,
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
  fetchAllData(forceFetch) {
    if (!this.props.recipientsFetched || forceFetch) {
      this.props.fetchUsers();
    }

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

  loadMoreRows({ startIndex, stopIndex }) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${stopIndex - startIndex > 0 ? stopIndex - startIndex : 1}&stepNumber=2`;
    apiClient.get(url)
      .then((response) => {
        this.setLineItems(response);
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
        this.transitionToNextStep()
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
            }),
          );

          this.setState({ values: { ...this.state.values, lineItems: lineItemsBackendData } });

          this.setState({
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
   * Removes chosen item from requisition's items list.
   * @param {string} itemId
   * @public
   */
  removeItem(itemId) {
    const removeItemsUrl = `/openboxes/api/stockMovementItems/${itemId}/removeItem`;
    const payload = { stockMovementId: this.state.values.stockMovementId };

    return apiClient.delete(removeItemsUrl, { data: payload })
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
   * @public
   */
  transitionToNextStep() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'REQUESTED' };

    if (this.state.values.statusCode === 'CREATED') {
      return apiClient.post(url, payload);
    }
    return Promise.resolve();
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

  isRowLoaded({ index }) {
    return !!this.state.values.lineItems[index];
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
        this.fetchLineItems();
        if (_.isNil(_.last(this.state.values.lineItems).product)) {
          this.setState({
            values: {
              ...this.state.values,
              lineItems: [],
            },
          });
        }
      })
      .catch(() => {
        this.props.hideSpinner();
      });
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

  render() {
    const { showOnly } = this.props;
    return (
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            { !showOnly ?
              <span>
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
              :
              <button
                type="button"
                disabled={invalid}
                onClick={() => { window.location = '/openboxes/stockMovement/list?direction=OUTBOUND'; }}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
              >
                <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.exit.label" defaultMessage="Exit" /></span>
              </button> }
            <form onSubmit={handleSubmit}>
              {_.map(this.getFields(), (fieldConfig, fieldName) =>
                renderFormField(fieldConfig, fieldName, {
                  stocklist: values.stocklist,
                  recipients: this.props.recipients,
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
                  showOnly,
                  updateRow: this.updateRow,
                  values,
                }))}
              <div>
                <button
                  type="submit"
                  disabled={invalid || showOnly}
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
                  disabled={
                    !_.some(values.lineItems, item => !_.isEmpty(item)) || invalid || showOnly
                  }
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
  /** Return true if show only */
  showOnly: PropTypes.bool.isRequired,
};
