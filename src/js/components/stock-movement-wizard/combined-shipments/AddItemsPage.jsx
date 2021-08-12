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
import fileDownload from 'js-file-download';

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
import CombinedShipmentItemsModal from '../modals/CombinedShipmentItemsModal';

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
    disabled: fieldValue && fieldValue.statusCode === 'SUBSTITUTED',
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const FIELDS = {
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
      values, onResponse, saveItems, invalid,
    }) => (
      <CombinedShipmentItemsModal
        shipment={values.stockMovementId}
        vendor={values.origin.id}
        destination={values.destination.id}
        onResponse={onResponse}
        btnOpenText="react.default.button.addLines.label"
        btnOpenDefaultText="Add lines"
        onOpen={() => saveItems(values.lineItems)}
        btnOpenDisabled={invalid}
      >
        <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
      </CombinedShipmentItemsModal>
    ),
    fields: {
      orderNumber: {
        type: LabelField,
        label: 'react.stockMovement.orderNumber.label',
        defaultMessage: 'Order number',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({
          fieldValue,
        }) => ({
          url: fieldValue && fieldValue.orderId ? `/openboxes/order/show/${fieldValue.orderId}` : '',
        }),
        attributes: {
          formatValue: fieldValue => fieldValue && fieldValue.orderNumber,
        },
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
      quantityRequested: {
        type: TextField,
        label: 'react.stockMovement.quantity.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
        required: true,
        attributes: {
          type: 'number',
          showError: true,
        },
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
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
              referenceId: fieldValue.orderItemId,
              orderNumber: fieldValue.orderNumber,
              packSize: fieldValue.packSize,
              quantityAvailable: fieldValue.quantityAvailable,
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
      isFirstPageLoaded: false,
    };

    this.props.showSpinner();
    this.confirmSave = this.confirmSave.bind(this);
    this.validate = this.validate.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateTotalCount = this.updateTotalCount.bind(this);
    this.updateRow = this.updateRow.bind(this);
    this.removeItem = this.removeItem.bind(this);
    this.fetchLineItems = this.fetchLineItems.bind(this);
    this.saveRequisitionItemsInCurrentStep = this.saveRequisitionItemsInCurrentStep.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
    this.toggleDropdown = this.toggleDropdown.bind(this);

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

    return _.map(items, item => ({
      id: item.id || null,
      'product.id': item.product.id,
      quantityRequested: item.quantityRequested,
      palletName: item.palletName,
      boxName: item.boxName,
      lotNumber: item.lotNumber,
      expirationDate: item.expirationDate,
      'recipient.id': _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '',
      sortOrder: item.sortOrder,
      orderItemId: item.orderItemId,
    }));
  }

  setLineItems(response, startIndex) {
    const { data } = response.data;
    const lineItemsData = _.map(
      data,
      val => ({
        ...val,
        disabled: true,
        product: {
          ...val.product,
          label: `${val.productCode} ${val.product.name}`,
        },
        referenceId: val.orderItemId,
      }),
    );

    this.setState({
      values: {
        ...this.state.values,
        lineItems: this.props.isPaginated && !_.isNull(startIndex) ?
          _.uniqBy(_.concat(this.state.values.lineItems, lineItemsData), 'id') : lineItemsData,
      },
    }, () => {
      if (!_.isNull(startIndex) && this.state.values.lineItems.length !== this.state.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      }
      this.props.hideSpinner();
    });
  }

  updateTotalCount(value) {
    this.setState({
      totalCount: this.state.totalCount + value,
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
      if (!_.isNil(item.product) && (!item.quantityRequested || item.quantityRequested <= 0)) {
        errors.lineItems[key] = { quantityRequested: 'react.stockMovement.error.enterQuantity.label' };
      }
      if (_.toInteger(item.quantityRequested) % item.packSize !== 0) {
        errors.lineItems[key] = { quantityRequested: 'react.stockMovement.error.multipleOfPackSize.label' };
      }
      if (!_.isEmpty(item.boxName) && _.isEmpty(item.palletName)) {
        errors.lineItems[key] = { boxName: 'react.stockMovement.error.boxWithoutPallet.label' };
      }
      const dateRequested = moment(item.expirationDate, 'MM/DD/YYYY');
      if (date.diff(dateRequested) > 0) {
        errors.lineItems[key] = { expirationDate: 'react.stockMovement.error.invalidDate.label' };
      }
      if (moment().startOf('day').diff(dateRequested) > 0) {
        errors.lineItems[key] = { expirationDate: 'react.stockMovement.error.pastDate.label' };
      }
      if (item.expirationDate && (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber))) {
        errors.lineItems[key] = { lotNumber: 'react.stockMovement.error.expiryWithoutLot.label' };
      }
      const splitItems = _.filter(values.lineItems, lineItem =>
        lineItem.referenceId === item.referenceId);
      if (!item.id || splitItems.length > 1) {
        const requestedQuantity = _.reduce(
          splitItems, (sum, val) =>
            (sum + (val.quantityRequested ? _.toInteger(val.quantityRequested) : 0)),
          0,
        );
        if (requestedQuantity > item.quantityAvailable) {
          _.forEach(values.lineItems, (lineItem, lineItemKey) => {
            _.forEach(splitItems, (splitItem) => {
              if (lineItem === splitItem) {
                errors.lineItems[lineItemKey] = { quantityRequested: 'react.stockMovement.error.higherSplitQuantity.label' };
              }
            });
          });
        }
      } else if (splitItems.length === 1 &&
        item && item.quantityAvailable < _.toInteger(item.quantityRequested)) {
        errors.lineItems[key] = { quantityRequested: 'react.stockMovement.error.higherQuantity.label' };
      }
      if (!_.isNil(item.product) && item.product.lotAndExpiryControl) {
        if (!item.expirationDate && (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber))) {
          errors.lineItems[key] = {
            expirationDate: 'react.stockMovement.error.lotAndExpiryControl.label',
            lotNumber: 'react.stockMovement.error.lotAndExpiryControl.label',
          };
        } else if (!item.expirationDate) {
          errors.lineItems[key] = { expirationDate: 'react.stockMovement.error.lotAndExpiryControl.label' };
        } else if (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber)) {
          errors.lineItems[key] = { lotNumber: 'react.stockMovement.error.lotAndExpiryControl.label' };
        }
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
   * Shows Inventory item expiration date update confirmation dialog.
   * @param {function} onConfirm
   * @public
   */
  confirmInventoryItemExpirationDateUpdate(onConfirm) {
    confirmAlert({
      title: this.props.translate('react.stockMovement.message.confirmSave.label', 'Confirm save'),
      message: this.props.translate(
        'react.stockMovement.confirmExpiryDateUpdate.message',
        'This will update the expiry date across all depots in the system. Are you sure you want to proceed?',
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
        this.setState({ totalCount: response.data.data.length });
        this.setLineItems(response, null);
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
          totalCount,
        }, () => this.props.hideSpinner());
      });
  }

  isRowLoaded({ index }) {
    return !!this.state.values.lineItems[index];
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
        this.saveAndTransitionToNextStep(formValues, lineItems));
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
    if (_.some(lineItems, item => item.inventoryItem
      && item.expirationDate !== item.inventoryItem.expirationDate)) {
      if (_.some(lineItems, item => item.inventoryItem && item.inventoryItem.quantity && item.inventoryItem.quantity !== '0')) {
        this.confirmInventoryItemExpirationDateUpdate(() =>
          this.saveRequisitionItemsAndTransitionToNextStep(formValues, lineItems));
      } else {
        this.saveRequisitionItemsAndTransitionToNextStep(formValues, lineItems);
      }
    } else {
      this.saveRequisitionItemsAndTransitionToNextStep(formValues, lineItems);
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
   * Saves list of stock movement items and transition to next step.
   * @param {object} formValues
   * @param {object} lineItems
   * @public
   */
  saveRequisitionItemsAndTransitionToNextStep(formValues, lineItems) {
    this.props.showSpinner();

    this.saveRequisitionItems(lineItems)
      .then((resp) => {
        let values = formValues;
        if (resp) {
          values = {
            ...formValues,
            lineItems: _.map(resp.data.data.lineItems, item => ({
              ...item,
              referenceId: item.orderItemId,
            })),
          };
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
            _.sortBy(lineItems, ['sortOrder']),
            val => ({
              ...val,
              product: {
                ...val.product,
                label: `${val.productCode} ${val.product.name}`,
              },
              referenceId: val.orderItemId,
            }),
          );

          this.setState({ values: { ...this.state.values, lineItems: lineItemsBackendData } });
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
   * Removes chosen item from items list.
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
          totalCount: 0,
          values: {
            ...this.state.values,
            lineItems: [],
          },
        });
      })
      .catch(() => {
        this.fetchLineItems();
        return Promise.reject(new Error('react.stockMovement.error.deleteRequisitionItem.label'));
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
  transitionToNextStep() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'CHECKING' };

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

    const url = `/openboxes/api/combinedShipmentItems/importTemplate/${stockMovementId}`;

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

  exportTemplate(blank) {
    const url = `/openboxes/api/combinedShipmentItems/exportTemplate?vendor=${this.state.values.origin.id}&destination=${this.state.values.destination.id}${blank ? '&blank=true' : ''}`;
    apiClient.get(url, { responseType: 'blob' })
      .then((response) => {
        fileDownload(response.data, 'Order-items-template.csv', 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Toggle the downloadable files
   */
  toggleDropdown() {
    this.setState({
      isDropdownVisible: !this.state.isDropdownVisible,
    });
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
              <div className="dropdown">
                <button
                  type="button"
                  onClick={this.toggleDropdown}
                  className="dropdown-button float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                >
                  <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.download.label" defaultMessage="Download" /></span>
                </button>
                <div className={`dropdown-content print-buttons-container col-md-3 flex-grow-1 
                        ${this.state.isDropdownVisible ? 'visible' : ''}`}
                >
                  <a
                    href="#"
                    className="py-1 mb-1 btn btn-outline-secondary"
                    onClick={() => { this.exportTemplate(false); }}
                  >
                    <span><i className="pr-2 fa fa-download" /><Translate id="react.combinedShipments.availableItems.label" defaultMessage="Available order items" /></span>
                  </a>
                  <a
                    href="#"
                    className="py-1 mb-1 btn btn-outline-secondary"
                    onClick={() => { this.exportTemplate(true); }}
                  >
                    <span><i className="pr-2 fa fa-download" /><Translate id="react.combinedShipments.blankTemplate.label" defaultMessage="Blank import template" /></span>
                  </a>
                </div>
              </div>
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
                {_.map(FIELDS, (fieldConfig, fieldName) =>
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
                    isFirstPageLoaded: this.state.isFirstPageLoaded,
                    removeItem: this.removeItem,
                    onResponse: this.fetchLineItems,
                    saveItems: this.saveRequisitionItemsInCurrentStep,
                    invalid,
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
                      this.nextPage(values);
                    }
                  }}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                  disabled={!_.some(values.lineItems, item => !_.isEmpty(item))}
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
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  pageSize: PropTypes.number.isRequired,
};
