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
import Alert from 'react-s-alert';

import { fetchUsers, hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import DateField from 'components/form-elements/DateField';
import ProductSelectField from 'components/form-elements/ProductSelectField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import apiClient from 'utils/apiClient';
import { renderFormField, setColumnValue } from 'utils/form-utils';
import Select from 'utils/Select';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

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
    disabled: fieldValue && fieldValue.statusCode === 'SUBSTITUTED',
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const VENDOR_FIELDS = {
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
      addRow, getSortOrder, isFromOrder, updateTotalCount,
    }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        hidden={isFromOrder}
        onClick={() => {
          updateTotalCount(1);
          addRow({
            sortOrder: getSortOrder(),
          });
        }}
      >
        <span>
          <i className="fa fa-plus pr-2" />
          <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
        </span>
      </button>
    ),
    fields: {
      palletName: {
        type: TextField,
        label: 'react.stockMovement.packLevel1.label',
        defaultMessage: 'Pack level 1',
        flexWidth: '1',
        getDynamicAttr: ({
          rowIndex, rowCount, updateRow, values,
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
        type: ProductSelectField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        flexWidth: '4',
        required: true,
        attributes: {
          showSelectedOptionColor: true,
        },
        getDynamicAttr: ({
          updateRow, rowIndex, values, originId, focusField,
        }) => ({
          locationId: originId,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityRequested');
            }
          },
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
          localizeDate: true,
          showLocalizedPlaceholder: true,
          autoComplete: 'off',
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
          recipients, addRow, rowCount, rowIndex, getSortOrder,
          updateTotalCount, updateRow, values, setRecipientValue, translate,
        }) => ({
          headerHtml: () => (
            <Select
              placeholder={translate('react.stockMovement.recipient.label', 'Recipient')}
              className="select-xs my-2"
              classNamePrefix="react-select"
              options={recipients}
              onChange={(val) => {
                if (val) {
                  setRecipientValue(val);
                }
              }}
            />
          ),
          options: recipients,
          onTabPress: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder() });
          } : null,
          arrowRight: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder() });
          } : null,
          arrowDown: rowCount === rowIndex + 1 ? () => {
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

/* eslint class-methods-use-this: ["error",{ "exceptMethods": ["getLineItemsToBeSaved"] }] */
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
    this.isValidForSave = this.isValidForSave.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateTotalCount = this.updateTotalCount.bind(this);
    this.updateRow = this.updateRow.bind(this);
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
   * Returns an array of new stock movement's items and items to be
   * updated (comparing to previous state of line items).
   * @param {object} lineItems
   * @public
   */
  getLineItemsToBeSaved(lineItems) {
    const lineItemsToBeAdded = _.filter(lineItems, (item) =>
      !item.statusCode && item.quantityRequested && item.quantityRequested !== '0' && item.product);
    const lineItemsWithStatus = _.filter(lineItems, (item) => item.statusCode);
    const lineItemsToBeUpdated = [];
    _.forEach(lineItemsWithStatus, (item) => {
      const oldItem = _.find(this.state.currentLineItems, (old) => old.id === item.id);
      const oldQty = parseInt(oldItem.quantityRequested, 10);
      const newQty = parseInt(item.quantityRequested, 10);
      const oldRecipient = oldItem.recipient && _.isObject(oldItem.recipient)
        ? oldItem.recipient.id : oldItem.recipient;
      const newRecipient = item.recipient && _.isObject(item.recipient)
        ? item.recipient.id : item.recipient;

      // Intersection of keys common to both objects (excluding product key)
      const keyIntersection = _.remove(
        _.intersection(
          _.keys(oldItem),
          _.keys(item),
        ),
        (key) => key !== 'product',
      );

      if (
        !_.isEqual(_.pick(item, keyIntersection), _.pick(oldItem, keyIntersection))
        || (item.product.id !== oldItem.product.id)
      ) {
        lineItemsToBeUpdated.push(item);
      } else if (newQty !== oldQty || newRecipient !== oldRecipient) {
        lineItemsToBeUpdated.push(item);
      } else if (item.inventoryItem?.expirationDate && item.expirationDate
        && item.inventoryItem?.expirationDate !== item.expirationDate) {
        lineItemsToBeUpdated.push(item);
      }
    });

    return [].concat(
      _.map(lineItemsToBeAdded, (item) => ({
        product: { id: item.product.id },
        quantityRequested: item.quantityRequested,
        palletName: item.palletName,
        boxName: item.boxName,
        lotNumber: item.lotNumber,
        expirationDate: item.expirationDate,
        recipient: { id: _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '' },
        sortOrder: item.sortOrder,
      })),
      _.map(lineItemsToBeUpdated, (item) => ({
        id: item.id,
        product: { id: item.product.id },
        quantityRequested: item.quantityRequested,
        palletName: item.palletName,
        boxName: item.boxName,
        lotNumber: item.lotNumber,
        expirationDate: item.expirationDate,
        recipient: { id: _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '' },
        sortOrder: item.sortOrder,
      })),
    );
  }

  getSortOrder() {
    this.setState((prev) => ({
      sortOrder: prev.sortOrder + 100,
    }));

    return this.state.sortOrder;
  }

  setLineItems(response, startIndex) {
    const { data } = response.data;
    let lineItemsData;

    if (this.state.values.lineItems.length === 0 && !data.length) {
      lineItemsData = new Array(1).fill({ sortOrder: 100 });
    } else {
      lineItemsData = _.map(
        data,
        (val) => ({
          ...val,
          disabled: true,
        }),
      );
    }

    const sortOrder = _.toInteger(_.last(lineItemsData).sortOrder) + 100;
    this.setState((prev) => ({
      currentLineItems: this.props.isPaginated
        ? _.uniqBy(_.concat(prev.currentLineItems, data), 'id') : data,
      values: {
        ...prev.values,
        lineItems: this.props.isPaginated
          ? _.uniqBy(_.concat(prev.values.lineItems, lineItemsData), 'id') : lineItemsData,
      },
      sortOrder,
    }), () => {
      if (!_.isNull(startIndex) && this.state.values.lineItems.length !== this.state.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      }
      this.props.hideSpinner();
    });
  }

  updateTotalCount(value) {
    this.setState((prev) => ({
      totalCount: prev.totalCount + value === 0 ? 1 : prev.totalCount + value,
    }));
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

  validate(values, ignoreLotAndExpiry) {
    const errors = {};
    errors.lineItems = [];
    const date = moment(this.props.minimumExpirationDate, 'MM/DD/YYYY');

    _.forEach(values.lineItems, (item, key) => {
      errors.lineItems[key] = {};
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
      if (!ignoreLotAndExpiry) {
        if (item.expirationDate && (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber))) {
          errors.lineItems[key] = { lotNumber: 'react.stockMovement.error.expiryWithoutLot.label' };
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
      }
    });
    return errors;
  }

  isValidForSave(values) {
    const errors = this.validate(values, true);
    return !errors.lineItems || errors.lineItems.every(_.isEmpty);
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
      message: _.map(items, (item) => (
        <p key={item.sortOrder}>
          {`${item.product.productCode} ${item.product.displayNames?.default || item.product.name} ${item.quantityRequested}`}
        </p>
      )),
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
   * @public
   */
  fetchAllData() {
    this.props.fetchUsers();
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
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=2`;

    return apiClient.get(url)
      .then((response) => {
        this.setState({
          totalCount: response.data.data.length,
        }, () => this.setLineItems(response, null));
      })
      .catch((err) => err);
  }

  /**
   * Fetches stock movement's line items and sets them in redux form and in
   * state as current line items.
   * @public
   */
  fetchAddItemsPageData() {
    this.props.showSpinner();

    const url = `/api/stockMovements/${this.state.values.stockMovementId}`;
    apiClient.get(url)
      .then((resp) => {
        const { hasManageInventory } = resp.data.data;
        const { statusCode } = resp.data.data;
        const { totalCount } = resp.data;

        this.setState((prev) => ({
          values: {
            ...prev.values,
            hasManageInventory,
            statusCode,
          },
          totalCount: totalCount === 0 ? 1 : totalCount,
        }), () => this.props.hideSpinner());
      });
  }

  loadMoreRows({ startIndex }) {
    this.setState({
      isFirstPageLoaded: true,
    });
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${this.props.pageSize}&stepNumber=2`;
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
    const lineItems = _.filter(formValues.lineItems, (val) => !_.isEmpty(val) && val.product);

    if (_.some(lineItems, (item) => !item.quantityRequested || item.quantityRequested === '0')) {
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
    const itemsWithSameCode = _.filter(itemsMap, (item) => item.length > 1);

    if (_.some(itemsMap, (item) => item.length > 1) && !(this.state.values.origin.type === 'SUPPLIER' || !this.state.values.hasManageInventory)) {
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

    this.saveRequisitionItemsInCurrentStep(lineItems)
      .then((resp) => {
        let values = formValues;
        if (resp) {
          values = { ...formValues, lineItems: resp.data.data.lineItems };
        }

        if (_.some(values.lineItems, (item) => item.inventoryItem
          && item.expirationDate !== item.inventoryItem.expirationDate)) {
          if (_.some(values.lineItems, (item) => item.inventoryItem.quantity && item.inventoryItem.quantity !== '0')) {
            this.props.hideSpinner();
            this.confirmInventoryItemExpirationDateUpdate(() =>
              this.updateInventoryItemsAndTransitionToNextStep(values, lineItems));
          } else {
            this.updateInventoryItemsAndTransitionToNextStep(values, lineItems);
          }
        } else {
          this.transitionToNextStepAndChangePage(formValues);
        }
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Updates stock movement inventory items.
   * @param {object} formValues
   * @param {object} lineItems
   * @public
   */
  updateInventoryItemsAndTransitionToNextStep(formValues, lineItems) {
    const itemsToSave = this.getLineItemsToBeSaved(lineItems);
    const updateItemsUrl = `/api/stockMovements/${this.state.values.stockMovementId}/updateInventoryItems`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };

    this.props.showSpinner();

    apiClient.post(updateItemsUrl, payload)
      .then(() => this.transitionToNextStepAndChangePage(formValues))
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Saves list of requisition items in current step (without step change). Used to export template.
   * @param {object} itemCandidatesToSave
   * @public
   */
  saveRequisitionItemsInCurrentStep(itemCandidatesToSave) {
    const itemsToSave = this.getLineItemsToBeSaved(itemCandidatesToSave);
    const updateItemsUrl = `/api/stockMovements/${this.state.values.stockMovementId}/updateItems`;
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
            (val) => ({
              ...val,
            }),
          );

          this.setState((prev) => ({
            currentLineItems: lineItemsBackendData,
            values: {
              ...prev.values,
              lineItems: lineItemsBackendData,
            },
          }));
          return resp;
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
    const lineItems = _.filter(formValues.lineItems, (item) => !_.isEmpty(item));

    if (_.some(lineItems, (item) => !item.quantityRequested || item.quantityRequested === '0')) {
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
    if (errors.length && errors.every((obj) => typeof obj === 'object' && _.isEmpty(obj))) {
      this.saveRequisitionItemsInCurrentStep(formValues.lineItems)
        .then(() => {
          window.location = STOCK_MOVEMENT_URL.show(formValues.stockMovementId);
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
            onClick: () => {
              window.location = STOCK_MOVEMENT_URL.show(formValues.stockMovementId);
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
    const removeItemsUrl = `/api/stockMovementItems/${itemId}/removeItem`;

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
    const removeItemsUrl = `/api/stockMovements/${this.state.values.stockMovementId}/removeAllItems`;

    return apiClient.delete(removeItemsUrl)
      .then(() => {
        this.setState((prev) => ({
          totalCount: 1,
          currentLineItems: [],
          values: {
            ...prev.values,
            lineItems: new Array(1).fill({ sortOrder: 100 }),
          },
        }));
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
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'CHECKING' };

    if (this.state.values.statusCode === 'CREATED') {
      return apiClient.post(url, payload);
    }
    return Promise.resolve();
  }

  /**
   * Transition to next stock movement status and go to next form page.
   * @param {object} formValues
   * @public
   */
  transitionToNextStepAndChangePage(formValues) {
    this.transitionToNextStep()
      .then(() => {
        this.props.nextPage(formValues);
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Exports current state of stock movement's to csv file.
   * @param {object} formValues
   * @public
   */
  exportTemplate(formValues) {
    const lineItems = _.filter(formValues.lineItems, (item) => !_.isEmpty(item));

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
    const url = `/stockMovement/exportCsv/${stockMovementId}`;
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

    const url = `/stockMovement/importCsv/${stockMovementId}`;

    return apiClient.post(url, formData, config)
      .then(() => {
        this.fetchLineItems();
        if (_.isNil(_.last(this.state.values.lineItems).product)) {
          this.setState((prev) => ({
            values: {
              ...prev.values,
              lineItems: [],
            },
          }));
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
    return (
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{
          ...arrayMutators,
          setColumnValue,
        }}
        initialValues={this.state.values}
        render={({
          handleSubmit,
          values,
          invalid,
          form: { mutators },
        }) => (
          <div className="d-flex flex-column">
            <span className="buttons-container">
              <label
                htmlFor="csvInput"
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span>
                  <i className="fa fa-download pr-2" />
                  <Translate id="react.default.button.importTemplate.label" defaultMessage="Import template" />
                </span>
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
                disabled={!this.isValidForSave(values)}
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
                disabled={!this.isValidForSave(values)}
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
                onClick={() => this.removeAll().then(() => this.fetchAndSetLineItems())}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs"
              >
                <span>
                  <i className="fa fa-remove pr-2" />
                  <Translate id="react.default.button.deleteAll.label" defaultMessage="Delete all" />
                </span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              <div className="table-form" data-testid="items-table">
                {_.map(VENDOR_FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    stocklist: values.stocklist,
                    recipients: this.props.recipients,
                    removeItem: this.removeItem,
                    originId: this.state.values.origin.id,
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
                    setRecipientValue: (val) => mutators.setColumnValue('lineItems', 'recipient', val),
                    translate: this.props.translate,
                  }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="button"
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
                  disabled={
                    !_.some(values.lineItems, (item) =>
                      item.product && _.parseInt(item.quantityRequested))
                  }
                >
                  <Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = (state) => ({
  recipients: state.users.data,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
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
  /** Function fetching users */
  fetchUsers: PropTypes.func.isRequired,
  /** Array of available recipients  */
  recipients: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  pageSize: PropTypes.number.isRequired,
};
