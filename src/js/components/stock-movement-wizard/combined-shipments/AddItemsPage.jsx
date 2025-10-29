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
import ProductApi from 'api/services/ProductApi';
import {
  COMBINED_SHIPMENT_ITEMS_EXPORT_TEMPLATE,
  COMBINED_SHIPMENT_ITEMS_IMPORT_TEMPLATE,
  STOCK_MOVEMENT_BY_ID,
  STOCK_MOVEMENT_ITEM_REMOVE,
  STOCK_MOVEMENT_ITEMS,
  STOCK_MOVEMENT_REMOVE_ALL_ITEMS,
  STOCK_MOVEMENT_STATUS,
  STOCK_MOVEMENT_UPDATE_ITEMS,
} from 'api/urls';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import DateField from 'components/form-elements/DateField';
import LabelField from 'components/form-elements/LabelField';
import ProductSelectField from 'components/form-elements/ProductSelectField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import ConfirmExpirationDateModal from 'components/modals/ConfirmExpirationDateModal';
import CombinedShipmentItemsModal from 'components/stock-movement-wizard/modals/CombinedShipmentItemsModal';
import { ORDER_URL, STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import AlertMessage from 'utils/AlertMessage';
import apiClient from 'utils/apiClient';
import { renderFormField, setColumnValue } from 'utils/form-utils';
import { formatProductSupplierSubtext } from 'utils/form-values-utils';
import { debounceProductsFetch } from 'utils/option-utils';
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
        removeRow();
        updateTotalCount(-1);
      });
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
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    // eslint-disable-next-line react/prop-types
    addButton: ({
      // eslint-disable-next-line react/prop-types
      values, onResponse, saveItems, invalid, overrideFormValue,
    }) => (
      <CombinedShipmentItemsModal
        shipment={values.stockMovementId}
        vendor={values.origin.id}
        destination={values.destination.id}
        onResponse={onResponse}
        overrideFormValue={overrideFormValue}
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
          url: fieldValue?.orderId ? ORDER_URL.show(fieldValue.orderId) : '',
        }),
        attributes: {
          formatValue: (fieldValue) => fieldValue && fieldValue.orderNumber,
        },
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
          showValueTooltip: true,
          disabled: true,
        },
        getDynamicAttr: ({ rowIndex, values }) => {
          const row = values.lineItems[rowIndex] || {};
          const productSupplierNameLabel = formatProductSupplierSubtext(row?.productSupplier);

          return {
            tooltipValue: [row?.product?.name, productSupplierNameLabel].join(' '),
          };
        },
      },
      lotNumber: {
        type: TextField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
        getDynamicAttr: ({
          rowIndex,
          values,
          fetchInventoryItem,
          debouncedInventoryItemFetch,
        }) => ({
          onBlur: () => {
            fetchInventoryItem(values, rowIndex);
          },
          onChange: () => {
            debouncedInventoryItemFetch(values, rowIndex);
          },
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
        getDynamicAttr: ({
          rowIndex,
          values,
          validateExpirationDate,
        }) => ({
          onBlur: () => {
            validateExpirationDate(values?.lineItems, rowIndex);
          },
        }),
      },
      packsRequested: {
        type: TextField,
        label: 'react.stockMovement.quantityPOUom.label',
        defaultMessage: 'Quantity (in PO UoM)',
        fixedWidth: '120px',
        required: true,
        headerTooltip: 'react.stockMovement.quantityPerUom.InputTooltip.label',
        multilineHeader: true,
        attributes: {
          type: 'number',
          showError: true,
        },
      },
      unitOfMeasure: {
        type: TextField,
        label: 'react.stockMovement.POUom.label',
        defaultMessage: 'PO UoM',
        fixedWidth: '110px',
        attributes: {
          disabled: true,
        },
      },
      calculatedQuantityRequested: {
        type: TextField,
        label: 'react.stockMovement.quantityEach.label',
        defaultMessage: 'Quantity (each)',
        multilineHeader: true,
        flexWidth: 1,
        attributes: {
          disabled: true,
        },
        getDynamicAttr: ({ rowIndex, values }) => ({
          formatValue: () => {
            const row = values.lineItems[rowIndex] || {};
            const packsRequested = _.toInteger(row.packsRequested);
            const packSize = _.toInteger(row.packSize);
            return packsRequested * packSize;
          },
        }),
      },
      palletName: {
        type: TextField,
        label: 'react.stockMovement.packLevel1.label',
        defaultMessage: 'Pack level 1',
        flexWidth: '1',
        getDynamicAttr: ({
          rowIndex, rowCount,
        }) => ({
          autoFocus: rowIndex === rowCount - 1,
        }),
      },
      boxName: {
        type: TextField,
        label: 'react.stockMovement.packLevel2.label',
        defaultMessage: 'Pack level 2',
        flexWidth: '1',
      },
      recipient: {
        type: SelectField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1.5',
        getDynamicAttr: ({
          recipients,
          translate,
          setRecipientValue,
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
              product: fieldValue.product,
              recipient: fieldValue.recipient,
              sortOrder: fieldValue.sortOrder + 1,
              orderItemId: fieldValue.orderItemId,
              referenceId: fieldValue.orderItemId,
              orderNumber: fieldValue.orderNumber,
              packSize: fieldValue.packSize,
              unitOfMeasure: fieldValue.unitOfMeasure,
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

const LOT_AND_EXPIRY_ERROR = 'react.stockMovement.error.lotAndExpiryControl.label';

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
      values: this.props.initialValues,
      totalCount: 0,
      isFirstPageLoaded: false,
      showAlert: false,
      alertMessage: '',
      isExpirationModalOpen: false,
      // Stores the resolve function for the ConfirmExpirationDateModal promise
      resolveExpirationModal: null,
      itemsWithMismatchedExpiry: [],
    };

    this.props.showSpinner();
    this.confirmSave = this.confirmSave.bind(this);
    this.validate = this.validate.bind(this);
    this.validateWithAlertMessage = this.validateWithAlertMessage.bind(this);
    this.isValidForSave = this.isValidForSave.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.updateTotalCount = this.updateTotalCount.bind(this);
    this.removeItem = this.removeItem.bind(this);
    this.fetchLineItems = this.fetchLineItems.bind(this);
    this.saveRequisitionItemsInCurrentStep = this.saveRequisitionItemsInCurrentStep.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
    this.toggleDropdown = this.toggleDropdown.bind(this);
    this.fetchInventoryItem = this.fetchInventoryItem.bind(this);
    this.validateExpirationDate = this.validateExpirationDate.bind(this);
    this.cancelSavingRequisitionItem = this.cancelSavingRequisitionItem.bind(this);
    this.changeExpirationDate = this.changeExpirationDate.bind(this);

    this.debouncedInventoryItemFetch = _.debounce((lineItems, rowIndex) => {
      this.fetchInventoryItem(lineItems, rowIndex);
    }, 1000);

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
   * Returns an array of new stock movement's items and items to be
   * updated (comparing to previous state of line items).
   * @param {object} lineItems
   * @public
   */
  getLineItemsToBeSaved(lineItems) {
    const items = AddItemsPage.updateSortOrder(lineItems);

    return _.map(items, (item) => ({
      id: item.id || null,
      product: { id: item.product.id },
      quantityRequested: item.packsRequested * item.packSize,
      palletName: item.palletName,
      boxName: item.boxName,
      lotNumber: item.lotNumber,
      expirationDate: item.expirationDate,
      recipient: { id: _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '' },
      sortOrder: item.sortOrder,
      orderItemId: item.orderItemId,
    }));
  }

  setLineItems({
    response,
    setTableData,
  }) {
    const { data } = response.data;
    const lineItemsData = _.map(
      data,
      (val) => ({
        ...val,
        disabled: true,
        referenceId: val.orderItemId,
      }),
    );

    setTableData?.('lineItems', lineItemsData);

    this.props.hideSpinner();
  }

  updateTotalCount(value) {
    this.setState((prev) => ({
      totalCount: prev.totalCount + value,
    }));
  }

  dataFetched = false;

  validate(values, ignoreLotAndExpiry) {
    if (!this.dataFetched) {
      return {};
    }

    const errors = {};
    errors.lineItems = [];
    const date = moment(this.props.minimumExpirationDate, 'MM/DD/YYYY');

    _.forEach(values.lineItems, (item, key) => {
      if (!_.isNil(item.product) && (!item.packsRequested || item.packsRequested <= 0)) {
        errors.lineItems[key] = { packsRequested: 'react.stockMovement.error.enterQuantity.label' };
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
      const splitItems = _.filter(values.lineItems, (lineItem) =>
        lineItem.referenceId === item.referenceId);
      if (!item.id || splitItems.length > 1) {
        const requestedQuantity = _.reduce(
          splitItems, (sum, val) =>
            (sum + (val.packsRequested
              ? _.toInteger(val.packsRequested * val.packSize)
              : 0
            )),
          0,
        );
        if (requestedQuantity > item.quantityAvailable) {
          _.forEach(values.lineItems, (lineItem, lineItemKey) => {
            _.forEach(splitItems, (splitItem) => {
              if (lineItem === splitItem) {
                errors.lineItems[lineItemKey] = { packsRequested: 'react.stockMovement.error.higherSplitQuantity.label' };
              }
            });
          });
        }
      } else if (splitItems.length === 1
        && item && item.quantityAvailable < _.toInteger(item.packsRequested * item.packSize)) {
        errors.lineItems[key] = { packsRequested: 'react.stockMovement.error.higherQuantity.label' };
      }
      if (!ignoreLotAndExpiry) {
        if (item.expirationDate && (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber))) {
          errors.lineItems[key] = { lotNumber: 'react.stockMovement.error.expiryWithoutLot.label' };
        }
        if (!_.isNil(item.product) && item.product.lotAndExpiryControl) {
          if (!item.expirationDate && (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber))) {
            errors.lineItems[key] = {
              expirationDate: LOT_AND_EXPIRY_ERROR,
              lotNumber: LOT_AND_EXPIRY_ERROR,
            };
          } else if (!item.expirationDate) {
            errors.lineItems[key] = { expirationDate: LOT_AND_EXPIRY_ERROR };
          } else if (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber)) {
            errors.lineItems[key] = { lotNumber: LOT_AND_EXPIRY_ERROR };
          }
        }
      }
    });

    return errors;
  }

  validateWithAlertMessage(values) {
    const errors = this.validate(values);
    let alertMessage = '';

    _.forEach(errors.lineItems, (error, index) => {
      if (error) {
        const { productCode } = values.lineItems[index];
        if (!alertMessage) {
          alertMessage = `${this.props.translate('react.stockMovement.errors.followingRowsContainValidationError.label', 'Following rows contain validation errors: Row')} ${index + 1}: ${productCode}`;
        } else {
          alertMessage += `, Row ${index + 1}: ${productCode}`;
        }
      }
    });

    const { showAlert } = this.state;
    this.setState({ alertMessage, showAlert: showAlert && !alertMessage ? false : showAlert });

    return errors;
  }

  isValidForSave(values) {
    const errors = this.validate(values, true);
    return !errors.lineItems || errors.lineItems.every(_.isEmpty);
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
   * Fetches all required data.
   * @public
   */
  fetchAllData() {
    this.props.fetchUsers();
    this.fetchAddItemsPageData();
    if (!this.props.isPaginated) {
      this.fetchLineItems();
    }
    this.props.hideSpinner();
  }

  /**
   * Fetches 2nd step data from current stock movement.
   * @public
   */
  fetchLineItems(mutateTableData) {
    const url = `${STOCK_MOVEMENT_ITEMS(this.state.values.stockMovementId)}?stepNumber=2`;

    return apiClient.get(url)
      .then((response) => {
        this.setState({ totalCount: response.data.data.length });
        this.setLineItems({
          response,
          setTableData: mutateTableData,
        });
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

    apiClient.get(STOCK_MOVEMENT_BY_ID(this.state.values.stockMovementId))
      .then((resp) => {
        const { hasManageInventory } = resp.data.data;
        const { statusCode, lineItems } = resp.data.data;
        const { totalCount } = resp.data;

        const sortedLineItems = _.map(
          _.sortBy(lineItems, ['sortOrder']),
          (val) => ({
            ...val,
            disabled: true,
            referenceId: val.orderItemId,
          }),
        );

        this.setState((prev) => ({
          values: {
            ...prev.values,
            hasManageInventory,
            statusCode,
            // setting initial values for the form
            lineItems: sortedLineItems,
          },
          totalCount,
        }), () => this.props.hideSpinner());
      });
  }

  isRowLoaded = (values) => ({ index }) => !!values.lineItems[index]

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    const errors = this.validateWithAlertMessage(formValues).lineItems;
    if (errors.length) {
      this.setState({ showAlert: true });
      return;
    }

    const lineItems = _.filter(formValues.lineItems, (val) => !_.isEmpty(val) && val.product);

    if (_.some(lineItems, (item) => !item.packsRequested || item.packsRequested === '0')) {
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
    this.saveRequisitionItemsAndTransitionToNextStep(formValues, lineItems);
  }

  async fetchInventoryItem(values, rowIndex, shouldValidateExpirationDate = true) {
    this.debouncedInventoryItemFetch.cancel();
    const lotNumber = values?.lineItems[rowIndex]?.lotNumber;
    const productId = values?.lineItems[rowIndex]?.product?.id;

    if (!lotNumber || !productId) {
      return;
    }

    const { data } = await ProductApi.getInventoryItem(productId, lotNumber);
    const mappedLineItems = values?.lineItems.map((lineItem, index) => {
      if (rowIndex === index) {
        return {
          ...lineItem,
          fetchedInventoryItem: {
            inventoryItem: data.inventoryItem,
            quantity: data?.quantityOnHand,
          },
        };
      }
      return lineItem;
    });

    this.setState((previousState) => ({
      ...previousState,
      values: {
        ...previousState.values,
        lineItems: mappedLineItems,
      },
    }), () => {
      if (!values.lineItems?.[rowIndex]?.expirationDate) {
        this.changeExpirationDate(mappedLineItems, rowIndex, data?.inventoryItem?.expirationDate);
      }

      // Prevent an infinite loop between fetchInventoryItem() and validateExpirationDate()
      if (shouldValidateExpirationDate) {
        this.validateExpirationDate(mappedLineItems, rowIndex);
      }
    });
  }

  /**
   * Shows Inventory item expiration date update confirmation modal.
   * @param {Array} itemsWithMismatchedExpiry - Array of elements with mismatched expiration dates.
   * @returns {Promise} - Resolves to true if user confirms the update, false if not.
   * @public
   */
  confirmExpirationDateSave(itemsWithMismatchedExpiry) {
    return new Promise((resolve) => {
      this.setState({
        isExpirationModalOpen: true,
        resolveExpirationModal: resolve,
        itemsWithMismatchedExpiry,
      });
    });
  }

  async validateExpirationDate(lineItems, rowIndex) {
    const lineItem = lineItems?.[rowIndex];
    const inventoryItem = lineItem?.fetchedInventoryItem?.inventoryItem
      || lineItem?.inventoryItem;
    const quantity = (lineItem?.fetchedInventoryItem
      ? lineItem?.fetchedInventoryItem?.quantity : lineItem?.inventoryItem?.quantity) || 0;
    const expirationDateHasChanged = inventoryItem?.expirationDate
      && lineItem?.expirationDate
      && lineItem?.lotNumber
      && lineItem?.expirationDate !== inventoryItem?.expirationDate
      && quantity > 0;

    if (expirationDateHasChanged) {
      // Despite we have only one item here, we are place it in an array
      // because the ConfirmExpirationDateModal expects an array
      const itemsWithMismatchedExpiry = [{
        code: lineItem?.product?.productCode,
        product: lineItem?.product,
        lotNumber: lineItem?.lotNumber,
        previousExpiry: inventoryItem?.expirationDate,
        newExpiry: lineItem?.expirationDate,
      }];

      const shouldUpdateLotExpirationDate =
        await this.confirmExpirationDateSave(itemsWithMismatchedExpiry);
      if (!shouldUpdateLotExpirationDate) {
        this.cancelSavingRequisitionItem(lineItems, rowIndex);
        return Promise.reject();
      }

      return this.saveRequisitionItems([lineItems[rowIndex]])
        .then((response) => this.fetchInventoryItem(response.data.data, rowIndex, false));
    }
    return null;
  }

  changeExpirationDate(lineItems, rowIndex, newDate) {
    const updatedLineItem = { ...lineItems?.[rowIndex], expirationDate: newDate };
    this.setState((previousState) => ({
      ...previousState,
      values: {
        ...previousState.values,
        lineItems: update(previousState?.values?.lineItems, {
          [rowIndex]: { $set: updatedLineItem },
        }),
      },
    }));
  }

  cancelSavingRequisitionItem(lineItems, rowIndex) {
    const mappedLineItems = lineItems?.map((item, index) => {
      if (index === rowIndex) {
        const expirationDate = item?.fetchedInventoryItem?.inventoryItem?.expirationDate
          || item?.inventoryItem?.expirationDate;
        return { ...item, expirationDate };
      }
      return item;
    });

    this.setState((prev) => ({
      ...prev,
      values: {
        ...prev.values,
        lineItems: mappedLineItems,
      },
    }));
  }

  /**
   * Saves list of stock movement items with post method.
   * @param {object} lineItems
   * @public
   */
  saveRequisitionItems(lineItems) {
    const itemsToSave = this.getLineItemsToBeSaved(lineItems);
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };

    if (payload.lineItems.length) {
      return apiClient.post(STOCK_MOVEMENT_UPDATE_ITEMS(this.state.values.stockMovementId), payload)
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
            lineItems: _.map(resp.data.data.lineItems, (item) => ({
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
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };

    if (payload.lineItems.length) {
      return apiClient.post(STOCK_MOVEMENT_UPDATE_ITEMS(this.state.values.stockMovementId), payload)
        .then((resp) => {
          const { lineItems } = resp.data.data;

          const lineItemsBackendData = _.map(
            _.sortBy(lineItems, ['sortOrder']),
            (val) => ({ ...val, referenceId: val.orderItemId }),
          );

          this.setState((prev) => ({
            values: {
              ...prev.values,
              lineItems: lineItemsBackendData,
            },
          }));
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
    if (lineItems.length > 0) {
      if (_.some(lineItems, (item) => !item.packsRequested || item.packsRequested === '0')) {
        this.confirmSave(() => this.saveItems(lineItems));
      } else {
        this.saveItems(lineItems);
      }
    } else {
      Alert.error(this.props.translate('react.stockMovement.error.noShipmentItems.label', 'Cannot save shipment from PO with no items.'), { timeout: 2000 });
    }
  }

  /**
   * Saves changes made by user in this step and redirects to the shipment view page
   * @param {object} formValues
   * @public
   */
  saveAndExit(formValues) {
    if (formValues.lineItems.length > 0) {
      const errors = this.validateWithAlertMessage(formValues).lineItems;
      if (!errors.length) {
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
    } else {
      Alert.error(this.props.translate('react.stockMovement.error.noShipmentItems.label', 'Cannot save shipment from PO with no items.'), { timeout: 2000 });
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
    this.props.showSpinner();
    return apiClient.delete(STOCK_MOVEMENT_ITEM_REMOVE(itemId))
      .then(() => this.props.hideSpinner())
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
    this.props.showSpinner();

    return apiClient.delete(STOCK_MOVEMENT_REMOVE_ALL_ITEMS(this.state.values.stockMovementId))
      .then(() => {
        this.setState((prev) => ({
          totalCount: 0,
          values: {
            ...prev.values,
            lineItems: [],
          },
        }), () => this.props.hideSpinner());
      })
      .catch(() => {
        this.fetchLineItems();
        this.props.hideSpinner();
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
          onClick: () => this.fetchAllData(),
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
    const payload = { status: 'CHECKING' };

    if (this.state.values.statusCode === 'CREATED') {
      return apiClient.post(STOCK_MOVEMENT_STATUS(this.state.values.stockMovementId), payload);
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
   * @param mutateTableData
   */
  importTemplate = (mutateTableData) => (event) => {
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

    return apiClient
      .post(COMBINED_SHIPMENT_ITEMS_IMPORT_TEMPLATE(stockMovementId), formData, config)
      .then(() => {
        this.fetchLineItems(mutateTableData);
        if (_.isNil(_.last(this.state.values.lineItems).product)) {
          this.setState((prev) => ({
            values: {
              ...prev.values,
            },
          }));
        }
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  exportTemplate(blank) {
    const url = `${COMBINED_SHIPMENT_ITEMS_EXPORT_TEMPLATE}?vendor=${this.state.values.origin.id}&destination=${this.state.values.destination.id}${blank ? '&blank=true' : ''}`;
    apiClient.get(url, { responseType: 'blob' })
      .then((response) => {
        fileDownload(response.data, `${this.props.translate('react.combinedShipments.template.fileName.label', 'Order-items-template')}.csv`, 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Toggle the downloadable files
   */
  toggleDropdown() {
    this.setState((prev) => ({
      isDropdownVisible: !prev.isDropdownVisible,
    }));
  }

  /**
   * Handles the response from the expiration date confirmation modal.
   * @param {boolean} shouldUpdate - True if the user confirmed the update, false if not.
   * @public
   */
  handleExpirationModalResponse(shouldUpdate) {
    // Resolve the promise returned by confirmExpirationDateSave.
    if (this.state.resolveExpirationModal) {
      this.state.resolveExpirationModal(shouldUpdate);
    }

    // Close the modal and reset its state.
    this.setState({
      isExpirationModalOpen: false,
      resolveExpirationModal: null,
      itemsWithMismatchedExpiry: [],
    });
  }

  render() {
    const { showAlert, alertMessage } = this.state;
    return (
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{
          ...arrayMutators,
          override: ([field, value], state, { changeValue }) => {
            changeValue(state, field, () => value);
          },
          setColumnValue,
        }}
        initialValues={this.state.values}
        render={({
          form, handleSubmit, values, invalid,
        }) => (
          <div className="d-flex flex-column">
            <AlertMessage show={showAlert} message={alertMessage} danger />
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
                  onChange={this.importTemplate(form.mutators.override)}
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
                  className="dropdown-button float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
                >
                  <span>
                    <i className="fa fa-sign-out pr-2" />
                    <Translate id="react.default.button.download.label" defaultMessage="Download" />
                  </span>
                </button>
                <div className={`dropdown-content print-buttons-container col-md-3 flex-grow-1 
                        ${this.state.isDropdownVisible ? 'visible' : ''}`}
                >
                  <a
                    href="#"
                    className="py-1 mb-1 btn btn-outline-secondary"
                    onClick={() => { this.exportTemplate(false); }}
                  >
                    <span>
                      <i className="pr-2 fa fa-download" />
                      <Translate id="react.combinedShipments.availableItems.label" defaultMessage="Available order items" />
                    </span>
                  </a>
                  <a
                    href="#"
                    className="py-1 mb-1 btn btn-outline-secondary"
                    onClick={() => { this.exportTemplate(true); }}
                  >
                    <span>
                      <i className="pr-2 fa fa-download" />
                      <Translate id="react.combinedShipments.blankTemplate.label" defaultMessage="Blank import template" />
                    </span>
                  </a>
                </div>
              </div>
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
                onClick={() => this.removeAll()}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs ml-1"
              >
                <span>
                  <i className="fa fa-remove pr-2" />
                  <Translate id="react.default.button.deleteAll.label" defaultMessage="Delete all" />
                </span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    stocklist: values.stocklist,
                    recipients: this.props.recipients,
                    loadMoreRows: () => {},
                    debouncedProductsFetch: this.debouncedProductsFetch,
                    totalCount: this.state.totalCount,
                    isRowLoaded: this.isRowLoaded(values),
                    updateTotalCount: this.updateTotalCount,
                    isPaginated: this.props.isPaginated,
                    isFromOrder: this.state.values?.isFromOrder,
                    values,
                    isFirstPageLoaded: this.state.isFirstPageLoaded,
                    removeItem: this.removeItem,
                    onResponse: this.fetchLineItems,
                    saveItems: this.saveRequisitionItemsInCurrentStep,
                    invalid,
                    fetchInventoryItem: this.fetchInventoryItem,
                    debouncedInventoryItemFetch: this.debouncedInventoryItemFetch,
                    validateExpirationDate: this.validateExpirationDate,
                    setRecipientValue: (val) => form.mutators.setColumnValue('lineItems', 'recipient', val),
                    translate: this.props.translate,
                    overrideFormValue: form.mutators.override,
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
                  onClick={() => this.nextPage(values)}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                  disabled={!_.some(values.lineItems, (item) => !_.isEmpty(item))}
                >
                  <Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
            <ConfirmExpirationDateModal
              isOpen={this.state.isExpirationModalOpen}
              itemsWithMismatchedExpiry={this.state.itemsWithMismatchedExpiry}
              onConfirm={() => this.handleExpirationModalResponse(true)}
              onCancel={() => this.handleExpirationModalResponse(false)}
            />
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
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
};
