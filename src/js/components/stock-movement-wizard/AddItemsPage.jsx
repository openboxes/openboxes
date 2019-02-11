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

import 'react-confirm-alert/src/react-confirm-alert.css';

import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import ArrayField from '../form-elements/ArrayField';
import ButtonField from '../form-elements/ButtonField';
import LabelField from '../form-elements/LabelField';
import DateField from '../form-elements/DateField';
import { renderFormField } from '../../utils/form-utils';
import { showSpinner, hideSpinner, fetchUsers } from '../../actions';
import apiClient from '../../utils/apiClient';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

const DELETE_BUTTON_FIELD = {
  type: ButtonField,
  label: 'default.button.delete.label',
  defaultMessage: 'Delete',
  flexWidth: '1',
  fieldKey: '',
  buttonLabel: 'default.button.delete.label',
  buttonDefaultMessage: 'Delete',
  getDynamicAttr: ({ fieldValue, removeItem, removeRow }) => ({
    onClick: fieldValue.id ? () => removeItem(fieldValue.id).then(() => removeRow()) : removeRow,
    disabled: fieldValue.statusCode === 'SUBSTITUTED',
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const NO_STOCKLIST_FIELDS = {
  lineItems: {
    type: ArrayField,
    virtualized: true,
    arrowsNavigation: true,
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow, getSortOrder }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({
          sortOrder: getSortOrder(),
        })}
      ><Translate id="default.button.addLine.label" defaultMessage="Add line" />
      </button>
    ),
    fields: {
      product: {
        fieldKey: 'disabled',
        type: SelectField,
        label: 'stockMovement.requisitionItems.label',
        defaultMessage: 'Requisition items',
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
        },
        getDynamicAttr: ({
          fieldValue, productsFetch, rowIndex, rowCount,
        }) => ({
          disabled: !!fieldValue,
          loadOptions: _.debounce(productsFetch, 500),
          autoFocus: rowIndex === rowCount - 1,
        }),
      },
      quantityRequested: {
        type: TextField,
        label: 'stockMovement.quantity.label',
        defaultMessage: 'Quantity',
        flexWidth: '2.5',
        attributes: {
          type: 'number',
        },
        fieldKey: '',
        getDynamicAttr: ({
          fieldValue,
        }) => ({
          disabled: fieldValue.statusCode === 'SUBSTITUTED' || _.isNil(fieldValue.product),
        }),
      },
      recipient: {
        type: SelectField,
        label: 'stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '2.5',
        fieldKey: '',
        getDynamicAttr: ({
          fieldValue, recipients, addRow, rowCount, rowIndex, getSortOrder,
        }) => ({
          options: recipients,
          disabled: fieldValue.statusCode === 'SUBSTITUTED' || _.isNil(fieldValue.product),
          onTabPress: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
          arrowRight: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
          arrowDown: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
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
    virtualized: true,
    arrowsNavigation: true,
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow, getSortOrder, newItemAdded }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => {
          addRow({ sortOrder: getSortOrder() });
          newItemAdded();
        }}
      ><Translate id="default.button.addLine.label" defaultMessage="Add line" />
      </button>
    ),
    fields: {
      product: {
        fieldKey: 'disabled',
        type: SelectField,
        label: 'stockMovement.requisitionItems.label',
        defaultMessage: 'Requisition items',
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
        },
        getDynamicAttr: ({
          fieldValue, productsFetch, rowIndex, rowCount, newItem,
        }) => ({
          disabled: !!fieldValue,
          loadOptions: _.debounce(productsFetch, 500),
          autoFocus: newItem && rowIndex === rowCount - 1,
        }),
      },
      quantityAllowed: {
        type: LabelField,
        label: 'stockMovement.maxQuantity.label',
        defaultMessage: 'Max Qty',
        flexWidth: '1.7',
        attributes: {
          type: 'number',
        },
      },
      quantityRequested: {
        type: TextField,
        label: 'stockMovement.neededQuantity.label',
        defaultMessage: 'Needed Qty',
        flexWidth: '1.7',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          addRow, rowCount, rowIndex, getSortOrder,
        }) => ({
          onTabPress: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
          arrowRight: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
          arrowDown: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
        }),
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const VENDOR_FIELDS = {
  lineItems: {
    type: ArrayField,
    virtualized: true,
    arrowsNavigation: true,
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow, getSortOrder }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({
          sortOrder: getSortOrder(),
        })}
      ><Translate id="default.button.addLine.label" defaultMessage="Add line" />
      </button>
    ),
    fields: {
      palletName: {
        type: TextField,
        label: 'stockMovement.pallet.label',
        defaultMessage: 'Pallet',
        flexWidth: '1',
        getDynamicAttr: ({ rowIndex, rowCount }) => ({
          autoFocus: rowIndex === rowCount - 1,
        }),
      },
      boxName: {
        type: TextField,
        label: 'stockMovement.box.label',
        defaultMessage: 'Box',
        flexWidth: '1',
      },
      product: {
        type: SelectField,
        label: 'stockMovement.item.label',
        defaultMessage: 'Item',
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
          showValueTooltip: true,
        },
        getDynamicAttr: ({ productsFetch }) => ({
          loadOptions: _.debounce(productsFetch, 500),
        }),
      },
      lotNumber: {
        type: TextField,
        label: 'stockMovement.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: DateField,
        label: 'stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1.5',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
          autoComplete: 'off',
          placeholderText: 'MM/DD/YYYY',
        },
      },
      quantityRequested: {
        type: TextField,
        label: 'stockMovement.quantity.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
        required: true,
        attributes: {
          type: 'number',
        },
      },
      recipient: {
        type: SelectField,
        label: 'stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1.5',
        getDynamicAttr: ({
          recipients, addRow, rowCount, rowIndex, getSortOrder,
        }) => ({
          options: recipients,
          onTabPress: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
          arrowRight: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
          arrowDown: rowCount === rowIndex + 1 ? () => addRow({
            sortOrder: getSortOrder(),
          }) : null,
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

function validate(values) {
  const errors = {};
  errors.lineItems = [];

  _.forEach(values.lineItems, (item, key) => {
    if (!_.isNil(item.product) && item.quantityRequested < 0) {
      errors.lineItems[key] = { quantityRequested: 'error.enterQuantity.label' };
    }
  });
  return errors;
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
      values: this.props.initialValues,
      newItem: false,
    };

    this.props.showSpinner();
    this.removeItem = this.removeItem.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
    this.productsFetch = this.productsFetch.bind(this);
    this.getSortOrder = this.getSortOrder.bind(this);
    this.confirmSave = this.confirmSave.bind(this);
    this.confirmTransition = this.confirmTransition.bind(this);
    this.newItemAdded = this.newItemAdded.bind(this);
  }

  componentDidMount() {
    this.fetchAllData(false);
  }

  /**
   * Returns proper fields depending on origin type or if stock list is chosen.
   * @public
   */
  getFields() {
    if (this.state.values.origin.type === 'SUPPLIER') {
      return VENDOR_FIELDS;
    } else if (_.get(this.state.values.stocklist, 'id')) {
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
        this.state.values.origin.type === 'SUPPLIER' &&
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

    if (this.state.values.origin.type === 'SUPPLIER') {
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
          delete: item.quantityRequested && item.quantityRequested !== '0' ? 'false' : 'true',
        })),
      );
    }


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
        delete: item.quantityRequested && item.quantityRequested !== '0' ? 'false' : 'true',
      })),
    );
  }

  getSortOrder() {
    this.setState({
      sortOrder: this.state.sortOrder + 100,
    });

    return this.state.sortOrder;
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
      title: this.props.translate('message.confirmSave.label', 'Confirm save'),
      message: this.props.translate(
        'confirmSave.message',
        'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
      ),
      buttons: [
        {
          label: this.props.translate('default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        {
          label: this.props.translate('default.no.label', 'No'),
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
      title: this.props.translate('confirmTransition.label', 'You have entered the same code twice. Do you want to continue?'),
      message: _.map(items, item =>
        <p key={item.sortOrder}>{item.product.label} {item.quantityRequested}</p>),
      buttons: [
        {
          label: this.props.translate('default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        {
          label: this.props.translate('default.no.label', 'No'),
        },
      ],
    });
  }

  productsFetch(searchTerm, callback) {
    if (searchTerm) {
      apiClient.get(`/openboxes/api/products?name=${searchTerm}&productCode=${searchTerm}&location.id=${this.state.values.origin.id}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  id: obj.id,
                  name: obj.name,
                  productCode: obj.productCode,
                  label: `${obj.productCode} - ${obj.name}`,
                },
                label: `${obj.productCode} - ${obj.name}`,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }

  /**
   * Fetches all required data.
   * @param {boolean} forceFetch
   * @public
   */
  fetchAllData(forceFetch) {
    if (!this.props.recipientsFetched || forceFetch) {
      this.fetchData(this.props.fetchUsers);
    }

    this.fetchAndSetLineItems();
  }

  /**
   * Fetches stock movement's line items and sets them in redux form and in
   * state as current line items.
   * @public
   */
  fetchAndSetLineItems() {
    this.props.showSpinner();
    this.fetchLineItems().then((resp) => {
      const { lineItems } = resp.data.data;
      let lineItemsData;
      if (!lineItems.length) {
        lineItemsData = new Array(1).fill({ sortOrder: 100 });
      } else {
        lineItemsData = _.map(
          lineItems,
          val => ({
            ...val,
            disabled: true,
            rowKey: _.uniqueId('lineItem_'),
            product: {
              ...val.product,
              label: `${val.productCode} ${val.product.name}`,
            },
          }),
        );
      }

      const sortOrder = _.toInteger(_.last(lineItemsData).sortOrder) + 100;
      this.setState({
        currentLineItems: lineItems,
        values: { ...this.state.values, lineItems: lineItemsData },
        sortOrder,
      });

      this.props.hideSpinner();
    }).catch(() => this.props.hideSpinner());
  }

  /**
   * Fetches 2nd step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=2`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  /**
   * Fetches data using function given as an argument.
   * @param {function} fetchFunction
   * @public
   */
  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
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

    if (_.some(itemsMap, item => item.length > 1) && !(this.state.values.origin.type === 'SUPPLIER')) {
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

    if (formValues.origin.type === 'SUPPLIER') {
      this.saveRequisitionItems(lineItems)
        .then((resp) => {
          let values = formValues;
          if (resp) {
            values = { ...formValues, lineItems: resp.data.data.lineItems };
          }
          this.transitionToNextStep('CHECKING')
            .then(() => {
              this.props.goToPage(6, values);
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
              this.props.onSubmit(values);
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
    const updateItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };

    if (payload.lineItems.length) {
      return apiClient.post(updateItemsUrl, payload)
        .catch(() => Promise.reject(new Error('error.saveRequisitionItems.label')));
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
    const updateItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
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
        .catch(() => Promise.reject(new Error(this.props.translate('error.saveRequisitionItems.label', 'Could not save requisition items'))));
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
   * Saves list of requisition items in current step (without step change).
   * @param {object} lineItems
   * @public
   */
  saveItems(lineItems) {
    this.props.showSpinner();

    this.saveRequisitionItemsInCurrentStep(lineItems)
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('alert.saveSuccess.label', 'Changes saved successfully'));
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Refetch the data, all not saved changes will be lost.
   * @public
   */
  refresh() {
    confirmAlert({
      title: this.props.translate('message.confirmRefresh.label', 'Confirm refresh'),
      message: this.props.translate(
        'confirmRefresh.message',
        'Are you sure you want to refresh? Your progress since last save will be lost.',
      ),
      buttons: [
        {
          label: this.props.translate('default.yes.label', 'Yes'),
          onClick: () => this.fetchAllData(true),
        },
        {
          label: this.props.translate('default.no.label', 'No'),
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
    const removeItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: [{
        id: itemId,
        delete: 'true',
      }],
    };

    return apiClient.post(removeItemsUrl, payload)
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('error.deleteRequisitionItem.label'));
      });
  }

  /**
   * Removes all items from requisition's items list.
   * @public
   */
  removeAll() {
    const removeItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: _.map(this.state.values.lineItems, item => ({
        id: item.id,
        delete: 'true',
      })),
    };

    return apiClient.post(removeItemsUrl, payload)
      .catch(() => {
        this.fetchAndSetLineItems();
        this.props.hideSpinner();
        return Promise.reject(new Error('error.deleteRequisitionItem.label'));
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

    return apiClient.post(url, payload);
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
        this.props.hideSpinner();
        this.fetchAndSetLineItems();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  previousPage(values) {
    const errors = validate(values).lineItems;
    if (!errors.length) {
      this.saveRequisitionItemsInCurrentStep(values.lineItems)
        .then(() => this.props.previousPage(values));
    } else {
      confirmAlert({
        title: this.props.translate('confirmPreviousPage.label', 'Validation error'),
        message: this.props.translate('confirmPreviousPage.message.label', 'Cannot save due to validation error on page'),
        buttons: [
          {
            label: this.props.translate('confirmPreviousPage.correctError.label', 'Correct error'),
          },
          {
            label: this.props.translate('confirmPreviousPage.continue.label ', 'Continue (lose unsaved work)'),
            onClick: () => this.props.previousPage(values),
          },
        ],
      });
    }
  }

  render() {
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        validate={validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            <span>
              <label
                htmlFor="csvInput"
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-download pr-2" /><Translate id="default.button.importTemplate.label" defaultMessage="Import template" /></span>
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
                <span><i className="fa fa-upload pr-2" /><Translate id="default.button.exportTemplate.label" defaultMessage="Export template" /></span>
              </button>
              <button
                type="button"
                onClick={() => this.refresh()}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-refresh pr-2" /><Translate id="default.button.refresh.label" defaultMessage="Reload" /></span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.save(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-save pr-2" /><Translate id="default.button.save.label" defaultMessage="Save" /></span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.removeAll().then(() => this.fetchAndSetLineItems())}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs"
              >
                <span><i className="fa fa-remove pr-2" /><Translate id="default.button.deleteAll.label" defaultMessage="Delete all" /></span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              {_.map(this.getFields(), (fieldConfig, fieldName) =>
                renderFormField(fieldConfig, fieldName, {
                  stocklist: values.stocklist,
                  recipients: this.props.recipients,
                  removeItem: this.removeItem,
                  productsFetch: this.productsFetch,
                  getSortOrder: this.getSortOrder,
                  newItemAdded: this.newItemAdded,
                  newItem: this.state.newItem,
                }))}
              <div>
                <button type="button" className="btn btn-outline-primary btn-form btn-xs" onClick={() => this.previousPage(values)}>
                  <Translate id="default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                  disabled={!_.some(values.lineItems, item => !_.isEmpty(item))}
                ><Translate id="default.button.next.label" defaultMessage="Next" />
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
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchUsers,
})(AddItemsPage));

AddItemsPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function taking user to specified page */
  goToPage: PropTypes.func.isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  onSubmit: PropTypes.func.isRequired,
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
};
