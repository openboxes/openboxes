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

import {
  addStockMovementDraft,
  closeInfoBar,
  createInfoBar,
  fetchUsers,
  hideInfoBar,
  hideSpinner,
  removeStockMovementDraft,
  showInfoBar,
  showSpinner,
} from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import LabelField from 'components/form-elements/LabelField';
import ProductSelectField from 'components/form-elements/ProductSelectField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import notification from 'components/Layout/notifications/notification';
import Spinner from 'components/spinner/Spinner';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import { InfoBar, InfoBarConfigs } from 'consts/infoBar';
import NotificationType from 'consts/notificationTypes';
import RowSaveStatus from 'consts/rowSaveStatus';
import apiClient from 'utils/apiClient';
import {
  shouldCreateFullOutboundImportFeatureBar,
  shouldShowFullOutboundImportFeatureBar,
} from 'utils/featureBarUtils';
import { renderFormField, setColumnValue } from 'utils/form-utils';
import requestsQueue from 'utils/requestsQueue';
import RowSaveIconIndicator from 'utils/RowSaveIconIndicator';
import Select from 'utils/Select';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const DELETE_BUTTON_FIELD = {
  type: ButtonField,
  defaultMessage: 'Delete',
  flexWidth: '1',
  fieldKey: '',
  buttonLabel: 'react.default.button.delete.label',
  buttonDefaultMessage: 'Delete',
  getDynamicAttr: ({
    fieldValue, removeItem, removeRow, showOnly, updateTotalCount,
  }) => ({
    // onClick -> onMouseDown, because we can't cancel
    // function triggered in onBlur (request with items to save).
    // onClick doesn't work in this case, because element on
    // which we want to trigger this function has to have focus
    // but if onBlur takes a little more time, onClick will not be executed
    onMouseDown: fieldValue && fieldValue.id ? () => {
      removeItem(fieldValue.id).then(() => {
        updateTotalCount(-1);
        removeRow();
      });
    } : () => { updateTotalCount(-1); removeRow(); },
    disabled: (fieldValue && fieldValue.statusCode === 'SUBSTITUTED') || showOnly,
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const ROW_SAVE_ICON_FIELD = {
  type: (params) => <RowSaveIconIndicator lineItemSaveStatus={params.fieldValue} />,
  flexWidth: '0.2',
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
    addButton: ({
      // eslint-disable-next-line react/prop-types
      addRow, getSortOrder, showOnly, updateTotalCount, getStockMovementDraft, isDraftAvailable,
    }) => (
      <>
        <button
          type="button"
          id="addButton"
          className="btn btn-outline-success btn-xs"
          disabled={showOnly}
        // onClick -> onMouseDown (see comment for DELETE_BUTTON_FIELD)
          onMouseDown={() => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder(), rowSaveStatus: RowSaveStatus.PENDING });
          }}
        >
          <span>
            <i className="fa fa-plus pr-2" />
            <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
          </span>
        </button>
        {isDraftAvailable
          && (
          <button
            type="button"
            className="btn btn-outline-primary btn-xs draft-button ml-1"
            onMouseDown={() => getStockMovementDraft()}
          >
            <Translate id="react.default.button.availableDraft.label" defaultMessage="Available draft" />
          </button>
          )}
      </>
    ),
    fields: {
      product: {
        type: ProductSelectField,
        fieldKey: 'disabled',
        label: 'react.stockMovement.requestedProduct.label',
        defaultMessage: 'Requested product',
        headerAlign: 'left',
        flexWidth: '9.5',
        attributes: {
          showSelectedOptionColor: true,
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, rowCount, originId, focusField,
        }) => ({
          disabled: !!fieldValue,
          autoFocus: rowIndex === rowCount - 1,
          locationId: originId,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityRequested');
            }
          },
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
          fieldValue, updateRow, values, rowIndex, saveProgress,
        }) => ({
          disabled: fieldValue?.rowSaveStatus === RowSaveStatus.SAVING
               || (fieldValue && fieldValue.statusCode === 'SUBSTITUTED')
                || _.isNil(fieldValue && fieldValue.product),
          onBlur: () => {
            updateRow(values, rowIndex);
            saveProgress({ values });
          },
          onChange: (value) => {
            saveProgress({
              values,
              rowIndex,
              fieldValue: { ...fieldValue, quantityRequested: value },
            });
          },
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
          updateTotalCount, updateRow, values, saveProgress, setRecipientValue, translate,
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
                  saveProgress({
                    editAll: true,
                    values: update(values, {
                      lineItems: {
                        $set: values.lineItems
                          .map((item) => update(item, { recipient: { $set: val } })),
                      },
                    }),
                  });
                }
              }}
            />
          ),
          options: recipients,
          disabled: fieldValue?.rowSaveStatus === RowSaveStatus.SAVING
               || (fieldValue && fieldValue.statusCode === 'SUBSTITUTED')
                || _.isNil(fieldValue && fieldValue.product),
          onTabPress: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder(), rowSaveStatus: RowSaveStatus.PENDING });
          } : null,
          arrowRight: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder(), rowSaveStatus: RowSaveStatus.PENDING });
          } : null,
          arrowDown: rowCount === rowIndex + 1 ? () => () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder(), rowSaveStatus: RowSaveStatus.PENDING });
          } : null,
          onBlur: () => {
            updateRow(values, rowIndex);
            saveProgress({ values });
          },
          onChange: (event) => {
            saveProgress({
              values,
              rowIndex,
              fieldValue: { ...fieldValue, recipient: event },
            });
          },
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
    showRowSaveIndicator: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    addButton: ({
      // eslint-disable-next-line react/prop-types
      addRow, getSortOrder, newItemAdded, updateTotalCount, isDraftAvailable, getStockMovementDraft,
    }) => (
      <>
        <button
          type="button"
          id="addButton"
          className="btn btn-outline-success btn-xs"
          // onClick -> onMouseDown (see comment for DELETE_BUTTON_FIELD)
          onMouseDown={() => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder(), rowSaveStatus: RowSaveStatus.PENDING });
            newItemAdded();
          }}
        >
          <span>
            <i className="fa fa-plus pr-2" />
            <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
          </span>
        </button>
        {isDraftAvailable
          && (
          <button
            type="button"
            className="btn btn-outline-primary btn-xs draft-button ml-1"
            onMouseDown={() => getStockMovementDraft()}
          >
            <Translate id="react.default.button.availableDraft.label" defaultMessage="Available draft" />
          </button>
          )}
      </>

    ),
    fields: {
      product: {
        fieldKey: 'disabled',
        type: ProductSelectField,
        label: 'react.stockMovement.requestedProduct.label',
        defaultMessage: 'Requested product',
        headerAlign: 'left',
        flexWidth: '9',
        attributes: {
          showSelectedOptionColor: true,
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, rowCount, newItem, originId, focusField,
        }) => ({
          disabled: !!fieldValue,
          autoFocus: newItem && rowIndex === rowCount - 1,
          locationId: originId,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantityRequested');
            }
          },
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
          addRow, rowCount, rowIndex, getSortOrder, updateTotalCount,
          updateRow, values, saveProgress,
        }) => ({
          disabled: values.lineItems[rowIndex]?.rowSaveStatus === RowSaveStatus.SAVING,
          onTabPress: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder(), rowSaveStatus: RowSaveStatus.PENDING });
          } : null,
          arrowRight: rowCount === rowIndex + 1 ? () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder(), rowSaveStatus: RowSaveStatus.PENDING });
          } : null,
          arrowDown: rowCount === rowIndex + 1 ? () => () => {
            updateTotalCount(1);
            addRow({ sortOrder: getSortOrder(), rowSaveStatus: RowSaveStatus.PENDING });
          } : null,
          onBlur: () => {
            updateRow(values, rowIndex);
            saveProgress({ values });
          },
          onChange: (event) => {
            saveProgress({
              values,
              rowIndex,
              fieldValue: { ...values.lineItems[rowIndex], quantityRequested: event },
            });
          },
        }),
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

// This variable is an indicator
// if action is in progress to avoid
// triggering the same transaction twice
// for example triggering save button during the autosave
let actionInProgress = false;

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
      isDraftAvailable: false,
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
    this.getStockMovementDraft = this.getStockMovementDraft.bind(this);
    this.transitionToNextStep = this.transitionToNextStep.bind(this);
    this.saveAndTransitionToNextStep = this.saveAndTransitionToNextStep.bind(this);
    this.didUserConfirmAlert = false;
    this.debouncedSave = _.debounce(() => {
      this.saveRequisitionItemsInCurrentStep(this.state.values.lineItems, false);
    }, 1000);
    this.requestsQueue = requestsQueue();
  }

  componentDidMount() {
    if (this.props.stockMovementTranslationsFetched) {
      this.dataFetched = true;
      this.fetchAllData();
    }
    const { bars } = this.props;
    // If the feature bar has not yet been triggered, try to add it to the redux store
    if (shouldCreateFullOutboundImportFeatureBar(bars)) {
      this.props.createInfoBar(InfoBarConfigs[InfoBar.FULL_OUTBOUND_IMPORT]);
    }
    // If the feature bar has not yet been closed by the user, show it
    if (shouldShowFullOutboundImportFeatureBar(bars)) {
      this.props.showInfoBar(InfoBar.FULL_OUTBOUND_IMPORT);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchAllData();
    }
  }

  componentWillUnmount() {
    // We want to hide the feature bar when unmounting the component
    // not to show it on any other page
    this.props.hideInfoBar(InfoBar.FULL_OUTBOUND_IMPORT);
  }

  /**
   * Returns proper fields depending on origin type or if stock list is chosen.
   * @public
   */
  getFields() {
    const fields = _.get(this.state.values.stocklist, 'id') ? STOCKLIST_FIELDS : NO_STOCKLIST_FIELDS;
    const fieldsWithRowSaveIcon = {
      lineItems: {
        ...fields.lineItems,
        fields: {
          ...fields.lineItems.fields,
          rowSaveStatus: ROW_SAVE_ICON_FIELD,
        },
      },
    };
    return this.props.isAutosaveEnabled ? fieldsWithRowSaveIcon : fields;
  }

  /**
   * Returns an array of new stock movement's items and items to be
   * updated (comparing to previous state of line items).
   * @param {object} lineItems
   * @public
   */

  getLineItemsToBeSaved(lineItems) {
    const lineItemsToBeAdded = _.filter(lineItems, (item) =>
      !item.statusCode
      && parseInt(item.quantityRequested, 10) > 0
      && item.product);
    const lineItemsWithStatus = _.filter(lineItems, (item) => item.statusCode);
    const lineItemsToBeUpdated = [];
    _.forEach(lineItemsWithStatus, (item) => {
      // We wouldn't update items with quantity requested < 0
      if ((item.quantityRequested !== 0 && !item.quantityRequested)
        || parseInt(item.quantityRequested, 10) < 0) {
        return; // lodash continue
      }
      const oldItem = _.find(this.state.currentLineItems, (old) => old.id === item.id);
      const oldQty = parseInt(oldItem?.quantityRequested, 10);
      const newQty = parseInt(item?.quantityRequested, 10);
      const oldRecipient = oldItem?.recipient && _.isObject(oldItem?.recipient)
        ? oldItem?.recipient.id : oldItem?.recipient;
      const newRecipient = item?.recipient && _.isObject(item?.recipient)
        ? item?.recipient.id : item?.recipient;

      // Intersection of keys common to both objects (excluding product key)
      const keyIntersection = _.remove(
        _.intersection(
          _.keys(oldItem),
          _.keys(item),
        ),
        (key) => key !== 'product',
      );

      if (
        newQty === oldQty
        && newRecipient === oldRecipient
        && this.props.isAutosaveEnabled
      ) {
        this.setState((prev) => ({
          values: {
            ...prev.values,
            lineItems: prev.values.lineItems.map((lineItem) => {
              if (lineItem.id === item.id) {
                return { ...lineItem, rowSaveStatus: RowSaveStatus.SAVED };
              }
              return lineItem;
            }),
          },
        }));
      }

      // We want to delete items with 0 qty after first save using stock list
      if (item.id && newQty === oldQty && oldQty === 0) {
        lineItemsToBeUpdated.push(item);
        return;
      }

      if (
        (this.state.values.origin.type === 'SUPPLIER' || !this.state.values.hasManageInventory)
        && (
          !_.isEqual(_.pick(item, keyIntersection), _.pick(oldItem, keyIntersection))
          || (item.product.id !== oldItem.product.id)
        ) && item.id
      ) {
        lineItemsToBeUpdated.push(item);
      } else if ((newQty !== oldQty || newRecipient !== oldRecipient) && item.id) {
        lineItemsToBeUpdated.push(item);
      }
    });

    const lineItemsToSave = [].concat(
      _.map(lineItemsToBeAdded, (item) => ({
        product: { id: item.product.id },
        quantityRequested: item.quantityRequested,
        recipient: { id: _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '' },
        sortOrder: item.sortOrder,
      })),
      _.map(lineItemsToBeUpdated, (item) => ({
        id: item.id,
        product: { id: item.product.id },
        quantityRequested: item.quantityRequested,
        recipient: { id: _.isObject(item.recipient) ? item.recipient.id || '' : item.recipient || '' },
        sortOrder: item.sortOrder,
      })),
    );

    if (this.props.isAutosaveEnabled && lineItemsToSave.length) {
      // Here I am changing rowSaveStatus from PENDING to SAVING
      // because all of these lines were sent to save
      this.setState((previousState) => ({
        values: {
          ...previousState.values,
          lineItems: previousState.values.lineItems.map((item) => {
            if (item.rowSaveStatus === RowSaveStatus.PENDING
                && item.product
                && item.quantityRequested) {
              return { ...item, rowSaveStatus: RowSaveStatus.SAVING };
            }
            return item;
          }),
        },
      }));
    }

    return lineItemsToSave;
  }

  getSortOrder() {
    this.setState((prev) => ({
      sortOrder: prev.sortOrder + 100,
    }));

    return this.state.sortOrder;
  }

  setLineItems(response, startIndex) {
    const { data } = response.data;
    const lineItemsData = data.length ? _.map(data, (val) => ({ ...val, disabled: true }))
      : new Array(1).fill({ sortOrder: 100, rowSaveStatus: RowSaveStatus.PENDING });
    const sortOrder = _.toInteger(_.last(lineItemsData).sortOrder) + 100;
    // check if stock list has items with qty 0
    if (
      this.props.isAutosaveEnabled
      && _.get(this.state.values.stocklist, 'id')
      && !this.state.isDraftAvailable
    ) {
      this.saveRequisitionItemsInCurrentStep(data, false);
    }
    this.setState((prev) => ({
      currentLineItems: startIndex !== null && this.props.isPaginated
        ? _.uniqBy(_.concat(prev.currentLineItems, data), 'id') : data,
      values: {
        ...prev.values,
        lineItems: startIndex !== null && this.props.isPaginated
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

  getStockMovementDraft() {
    this.setState((prev) => ({
      values: {
        ...prev.values,
        lineItems: this.props.savedStockMovement.lineItems
          .map((item) => ({ ...item, rowSaveStatus: RowSaveStatus.PENDING })),
      },
      totalCount: this.props.savedStockMovement.lineItems.length,
      isDraftAvailable: false,
    }));
    this.saveRequisitionItemsInCurrentStep(this.props.savedStockMovement.lineItems, true);
    this.props.hideSpinner();
  }

  updateTotalCount(value) {
    this.setState((prev) => ({
      totalCount: prev.totalCount + value === 0 ? 1 : prev.totalCount + value,
    }));
  }

  dataFetched = false;

  validate(values) {
    const errors = {};
    errors.lineItems = [];
    const date = moment(this.props.minimumExpirationDate, 'MM/DD/YYYY');

    _.forEach(values.lineItems, (item, key) => {
      if (
        !_.isNil(item.product)
        && ((item.quantityRequested !== 0 && !item.quantityRequested)
          || item?.quantityRequested < 0)
      ) {
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
          onClick: () => {
            this.didUserConfirmAlert = true;
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
      afterClose: () => {
        if (this.didUserConfirmAlert) {
          onConfirm();
          this.didUserConfirmAlert = false;
        }
      },
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
          totalCount: response.data.data.length || 1,
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
    const {
      lastUpdated: lastSaved,
      statusCode: savedStatusCode,
      id,
    } = this.props.savedStockMovement;
    const { stockMovementId } = this.state.values;

    const url = `/api/stockMovements/${stockMovementId}`;
    apiClient.get(url)
      .then((resp) => {
        const { hasManageInventory, statusCode, lastUpdated } = resp.data.data;
        const { totalCount } = resp.data;
        // if data from backend is older than the version from local storage
        // we want to allow users use their version
        const isDraftAvailable = this.props.isAutosaveEnabled
                                 && (stockMovementId === id)
                                 && (lastUpdated < lastSaved)
                                 && (savedStatusCode === statusCode);

        this.setState((prev) => ({
          values: {
            ...prev.values,
            hasManageInventory,
            statusCode,
          },
          totalCount: totalCount === 0 ? 1 : totalCount,
          isDraftAvailable,
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
        this.checkDuplicatesAndTransitionToNextStep(lineItems, formValues));
    } else {
      this.checkDuplicatesAndTransitionToNextStep(lineItems, formValues);
    }
  }

  checkDuplicatesAndTransitionToNextStep(lineItems, formValues) {
    const transitionFunction = this.props.isAutosaveEnabled
      ? this.transitionToNextStep : this.saveAndTransitionToNextStep;
    const itemsMap = {};
    _.forEach(lineItems, (item) => {
      if (parseInt(item.quantityRequested, 10) === 0) {
        return;
      }
      if (itemsMap[item.product.productCode]) {
        itemsMap[item.product.productCode].push(item);
      } else {
        itemsMap[item.product.productCode] = [item];
      }
    });
    const itemsWithSameCode = _.filter(itemsMap, (item) => item.length > 1);
    if (_.some(itemsMap, (item) => item.length > 1) && !(this.state.values.origin.type === 'SUPPLIER' || !this.state.values.hasManageInventory)) {
      this.confirmTransition(
        () => transitionFunction(formValues, lineItems),
        _.reduce(itemsWithSameCode, (a, b) => a.concat(b), []),
      );
    } else {
      transitionFunction(formValues, lineItems);
    }
  }

  saveAndTransitionToNextStep(formValues, lineItems) {
    this.props.showSpinner();

    this.saveRequisitionItems(lineItems)
      .then((resp) => {
        if (resp) {
          this.transitionToNextStep({
            values: { ...formValues, lineItems: resp.data.data.lineItems },
          });
          return;
        }
        this.transitionToNextStep({ values: formValues });
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
    const updateItemsUrl = `/api/stockMovements/${this.state.values.stockMovementId}/updateItems`;
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
   * @param {boolean} withStateChange
   * @public
   */
  async saveRequisitionItemsInCurrentStep(itemCandidatesToSave, withStateChange = true) {
    // We filter out items which were already sent to save
    const filteredCandidates = itemCandidatesToSave
      .filter((item) => item.rowSaveStatus !== RowSaveStatus.SAVING);
    const itemsToSave = this.getLineItemsToBeSaved(filteredCandidates);
    const updateItemsUrl = `/api/stockMovements/${this.state.values.stockMovementId}/updateItems`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };

    if (payload.lineItems.length) {
      if (!this.props.isOnline && this.props.isAutosaveEnabled) {
        this.props.addStockMovementDraft({
          lineItems: itemCandidatesToSave,
          id: this.state.values.stockMovementId,
          statusCode: this.state.values.statusCode,
        });
      }

      const saveItemsRequest = (data) => () => apiClient.post(updateItemsUrl, data)
        .then((resp) => {
          const { lineItems } = resp.data.data;
          const lineItemsBackendData = _.map(lineItems, (val) => ({ ...val, disabled: true }));
          // In autosave, we don't modify the state, because
          // lines which have not passed the validation will be
          // deleted during users work
          if (!withStateChange) {
            // We want to disable saved line, so I am looking for product with the same
            // code and quantity higher than 0 in response
            // (to avoid disabling new line with the same productCode)

            // TODO: Add new api endpoints in StockMovementItemApiController
            // for POST and PUT (create and update) that returns only updated items data
            // and separate autosave logic from save button logic
            const savedItemsIds = data?.lineItems?.map((item) => item.id);
            const backendResponseIds = lineItemsBackendData.map((item) => item.id);
            const backendResponseProductCodes = lineItemsBackendData.map(
              (item) => item.productCode,
            );
            // We are sending item by item to API. Here we have to find
            // newly saved item to replace its equivalent in state
            const itemToChange = _.last(_.differenceBy(lineItemsBackendData, itemCandidatesToSave, 'id'));
            const lineItemsAfterSave = this.state.values.lineItems.map((item) => {
              if (
                parseInt(item.quantityRequested, 10) === 0
                && (item.rowSaveStatus === RowSaveStatus.SAVING
                  || item.rowSaveStatus === RowSaveStatus.SAVED
                  || !item.rowSaveStatus)
                && _.includes(savedItemsIds, item.id)
              ) {
                return { ..._.omit(item, ['id', 'statusCode']), disabled: true, rowSaveStatus: RowSaveStatus.SAVED };
              }
              // In this case we check if we're editing item
              // We don't have to disable edited item, because this
              // line is disabled by default
              const savedIds = item?.id ? savedItemsIds : backendResponseIds;
              if (
                _.includes(savedIds, item.id)
                && item.rowSaveStatus !== RowSaveStatus.ERROR
              ) {
                const editedItem = lineItemsBackendData.find(
                  (savedItem) => savedItem.id === item.id,
                );
                return { ...editedItem, rowSaveStatus: RowSaveStatus.SAVED };
              }

              if (
                itemToChange
                && _.includes(backendResponseProductCodes, item.product?.productCode)
                && parseInt(item.quantityRequested, 10) > 0
                && item.rowSaveStatus === RowSaveStatus.SAVING
              ) {
                return { ...itemToChange, disabled: true, rowSaveStatus: RowSaveStatus.SAVED };
              }

              return item;
            });

            this.setState((prev) => ({
              values: { ...prev.values, lineItems: lineItemsAfterSave },
              currentLineItems: lineItemsBackendData,
            }));
            return;
          }

          this.setState((prev) => ({
            values: { ...prev.values, lineItems: lineItemsBackendData },
            currentLineItems: lineItemsBackendData,
          }));
        })
        .then(() => {
          if (this.props.isAutosaveEnabled) {
            // There is no need for creating draft
            // if all of my items are saved correctly
            // (it means that we have internet connection)
            this.props.removeStockMovementDraft(this.state.values.stockMovementId);
          }
        })
        .catch(() => {
          if (this.props.isAutosaveEnabled) {
            // When there is an error during saving we have to find products which
            // caused the error. These items are not saved, so we don't have line ID,
            // and we have to find these items by product ID and SaveStatus
            const notSavedItemsIds = payload.lineItems.map((item) => item.product.id);
            const lineItemsWithErrors = this.state.values.lineItems.map((item) => {
              if (
                item.product
                && item.rowSaveStatus === RowSaveStatus.SAVING
                && _.includes(notSavedItemsIds, item.product.id)
              ) {
                return { ...item, rowSaveStatus: RowSaveStatus.ERROR };
              }
              return item;
            });
            this.setState((prev) => ({
              values: {
                ...prev.values,
                lineItems: lineItemsWithErrors,
              },
            }));

            if (!this.props.isOnline) {
              // When there is an error, we are adding items to
              // state for draft
              this.props.addStockMovementDraft({
                lineItems: this.state.values.lineItems,
                id: this.state.values.stockMovementId,
                statusCode: this.state.values.statusCode,
              });
            }
          }
          return Promise.reject(new Error(this.props.translate('react.stockMovement.error.saveRequisitionItems.label', 'Could not save requisition items')));
        });

      if (this.props.isAutosaveEnabled) {
        this.requestsQueue.enqueueRequest(
          saveItemsRequest(payload),
        );
      } else {
        await saveItemsRequest(payload)();
      }
    }

    this.setState((previousState) => ({
      values: {
        ...previousState.values,
        lineItems: previousState.values.lineItems.map((item) => {
          if (parseInt(item.quantityRequested, 10) === 0 && !item?.id) {
            return { ...item, disabled: true, rowSaveStatus: RowSaveStatus.SAVED };
          }
          return item;
        }),
      },
    }));

    return Promise.resolve();
  }

  // if rowIndex is passed, it means that we are editing row
  // not adding new one
  saveProgress = ({
    values, rowIndex, fieldValue, editAll,
  }) => {
    if (!this.props.isAutosaveEnabled) {
      return;
    }

    if (actionInProgress) {
      return;
    }
    // I can't check !!rowIndex, because it can be 0,
    // so there is possibility to return false when the
    // rowIndex is present
    const isEdited = rowIndex !== undefined;
    const itemsWithStatuses = values.lineItems.map((item) => {
      if (editAll) {
        return { ...item, rowSaveStatus: RowSaveStatus.PENDING };
      }

      if (isEdited && rowIndex === values.lineItems.indexOf(item)) {
        return { ...fieldValue, rowSaveStatus: RowSaveStatus.PENDING };
      }

      if (
        item.product && ((item.quantityRequested !== 0 && !item.quantityRequested)
          || parseInt(item.quantityRequested, 10) < 0)
      ) {
        return { ...item, rowSaveStatus: RowSaveStatus.ERROR };
      }

      return item;
    });

    this.setState({ values: { ...values, lineItems: itemsWithStatuses } });

    // We don't want to save the item during editing or
    // when there is an error in line
    if (isEdited || editAll) {
      this.debouncedSave();
      return;
    }

    this.debouncedSave.cancel();

    this.saveRequisitionItemsInCurrentStep(itemsWithStatuses, false);
  };

  /**
   * Saves list of requisition items in current step (without step change).
   * @param {object} formValues
   * @public
   */
  save(formValues) {
    actionInProgress = true;
    const lineItems = _.filter(formValues.lineItems, (item) => !_.isEmpty(item));

    if (_.some(lineItems, (item) => !item.quantityRequested || item.quantityRequested === '0')) {
      this.confirmSave(() => {
        this.saveItems(lineItems);
      });
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
    actionInProgress = true;
    const errors = this.validate(formValues).lineItems;
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
        this.fetchLineItems();
        this.props.removeStockMovementDraft(this.state.values.stockMovementId);
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'), { timeout: 3000 });
      })
      .catch(() => this.props.hideSpinner())
      .finally(() => {
        actionInProgress = false;
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
    const removeItemsUrl = `/api/stockMovementItems/${itemId}/removeItem`;
    const payload = { stockMovementId: this.state.values.stockMovementId };

    return apiClient.delete(removeItemsUrl, { data: payload })
      .then(() => {
        if (!this.props.isOnline) {
          this.props.addStockMovementDraft({
            lineItems: this.state.values.lineItems,
            id: this.state.values.stockMovementId,
            statusCode: this.state.values.statusCode,
          });
        }
      })
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
    actionInProgress = true;
    const removeItemsUrl = `/api/stockMovements/${this.state.values.stockMovementId}/removeAllItems`;

    return apiClient.delete(removeItemsUrl)
      .then(() => {
        this.props.removeStockMovementDraft(this.state.values.stockMovementId);
        this.setState((prev) => ({
          totalCount: 1,
          currentLineItems: [],
          values: {
            ...prev.values,
            lineItems: new Array(1).fill({ sortOrder: 100, rowSaveStatus: RowSaveStatus.PENDING }),
          },
        }));
      })
      .catch(() => {
        this.fetchLineItems();
        return Promise.reject(new Error('react.stockMovement.error.deleteRequisitionItem.label'));
      })
      .finally(() => {
        actionInProgress = false;
      });
  }

  /**
   * Transition to next stock movement status:
   * - 'CHECKING' if origin type is supplier.
   * - 'VERIFYING' if origin type is other than supplier.
   * @public
   */
  transitionToNextStep({ values }) {
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'REQUESTED' };

    this.props.showSpinner();
    new Promise((resolve) => {
      if (this.state.values.statusCode === 'CREATED') {
        resolve(apiClient.post(url, payload));
      }
      return resolve();
    })
      .then(this.props.nextPage(values || this.state.values))
      .finally(this.props.hideSpinner());
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

  showPendingSaveNotification() {
    notification(NotificationType.INFO)({
      message: this.props.translate(
        'react.notification.autosave.pending.label',
        'Please wait while your line items are being saved.',
      ),
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
    const { showOnly } = this.props;

    return (
      <>
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
              { !showOnly
                ? (
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
                      disabled={invalid}
                  // onClick -> onMouseDown (see comment for DELETE_BUTTON_FIELD)
                      onMouseDown={() => this.save(values)}
                      className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
                    >
                      <span className="saving-button">
                        {_.some(
                          values.lineItems,
                          (item) => item.rowSaveStatus === RowSaveStatus.SAVING,
                        ) ? <Spinner /> : <i className="fa fa-save pr-2" />}
                        <Translate id="react.default.button.save.label" defaultMessage="Save" />
                      </span>
                    </button>
                    <button
                      type="button"
                      disabled={invalid}
                  // onClick -> onMouseDown (see comment for DELETE_BUTTON_FIELD)
                      onMouseDown={() => this.saveAndExit(values)}
                      className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
                    >
                      <span>
                        <i className="fa fa-sign-out pr-2" />
                        <Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" />
                      </span>
                    </button>
                  </span>
                )
                : (
                  <button
                    type="button"
                    disabled={invalid}
                    onClick={() => {
                      this.props.history(STOCK_MOVEMENT_URL.listOutbound());
                    }}
                    className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
                  >
                    <span>
                      <i className="fa fa-sign-out pr-2" />
                      <Translate id="react.default.button.exit.label" defaultMessage="Exit" />
                    </span>
                  </button>
                ) }
              <form onSubmit={handleSubmit}>
                <div className="table-form">
                  {_.map(this.getFields(), (fieldConfig, fieldName) =>
                    renderFormField(fieldConfig, fieldName, {
                      stocklist: values.stocklist,
                      recipients: this.props.recipients,
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
                      showOnly,
                      updateRow: this.updateRow,
                      values,
                      isFirstPageLoaded: this.state.isFirstPageLoaded,
                      saveProgress: this.saveProgress,
                      getStockMovementDraft: this.getStockMovementDraft,
                      isDraftAvailable: this.state.isDraftAvailable,
                      isAutosaveEnabled: this.props.isAutosaveEnabled,
                      setRecipientValue: (val) => mutators.setColumnValue('lineItems', 'recipient', val),
                      translate: this.props.translate,
                    }))}
                </div>
                <div className="submit-buttons">
                  <button
                    type="button"
                    disabled={
                    invalid
                    || showOnly
                    || _.some(values.lineItems, (item) => item.quantityRequested < 0)
                  }
                  // onClick -> onMouseDown (see comment for DELETE_BUTTON_FIELD)
                    onMouseDown={() => {
                      if (
                        this.props.isAutosaveEnabled
                      && _.some(values.lineItems, (lineItem) =>
                        (lineItem.rowSaveStatus === RowSaveStatus.PENDING
                        || lineItem.rowSaveStatus === RowSaveStatus.SAVING)
                        && lineItem.product)
                      ) {
                        this.showPendingSaveNotification();
                        return;
                      }
                      this.previousPage(values, invalid);
                    }}
                    className="btn btn-outline-primary btn-form btn-xs"
                  >
                    <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                  </button>
                  <button
                    type="submit"
                  // onClick -> onMouseDown (see comment for DELETE_BUTTON_FIELD)
                    onMouseDown={() => {
                      if (
                        this.props.isAutosaveEnabled
                      && _.some(values.lineItems, (lineItem) =>
                        lineItem.rowSaveStatus === RowSaveStatus.PENDING
                        || lineItem.rowSaveStatus === RowSaveStatus.SAVING)
                      ) {
                        this.showPendingSaveNotification();
                        return;
                      }
                      if (!invalid) {
                        this.nextPage(values);
                      }
                    }}
                    className="btn btn-outline-primary btn-form float-right btn-xs"
                    disabled={
                    values.lineItems.length === 0
                    || (values.lineItems.length === 1 && !('product' in values.lineItems[0]))
                    || invalid
                    || showOnly
                    || _.some(values.lineItems, (item) => item.quantityRequested < 0)
                    || _.every(values.lineItems,
                      (item) => parseInt(item.quantityRequested, 10) === 0)
                  }
                  >
                    <Translate id="react.default.button.next.label" defaultMessage="Next" />
                  </button>
                </div>
              </form>
            </div>
          )}
        />
      </>
    );
  }
}

const mapStateToProps = (state, ownProps) => ({
  recipients: state.users.data,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  minimumExpirationDate: state.session.minimumExpirationDate,
  hasPackingSupport: state.session.currentLocation.hasPackingSupport,
  isPaginated: state.session.isPaginated,
  pageSize: state.session.pageSize,
  savedStockMovement: state.stockMovementDraft[ownProps.initialValues.id],
  isOnline: state.connection.online,
  isAutosaveEnabled: state.session.isAutosaveEnabled,
  supportedActivities: state.session.supportedActivities,
  bars: state.infoBar.bars,
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
  fetchUsers,
  addStockMovementDraft,
  removeStockMovementDraft,
  createInfoBar,
  hideInfoBar,
  closeInfoBar,
  showInfoBar,
};

export default (connect(mapStateToProps, mapDispatchToProps)(AddItemsPage));

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
  /** Return true if show only */
  showOnly: PropTypes.bool,
  pageSize: PropTypes.number.isRequired,
  addStockMovementDraft: PropTypes.func.isRequired,
  removeStockMovementDraft: PropTypes.func.isRequired,
  savedStockMovement: PropTypes.shape({
    id: PropTypes.string,
    lineItems: PropTypes.arrayOf(PropTypes.shape({})),
    lastUpdated: PropTypes.string,
    statusCode: null,
  }),
  isOnline: PropTypes.bool,
  isAutosaveEnabled: PropTypes.bool,
  createInfoBar: PropTypes.func.isRequired,
  bars: PropTypes.arrayOf(PropTypes.shape({
    name: PropTypes.string.isRequired,
    show: PropTypes.bool.isRequired,
    closed: PropTypes.bool,
    title: PropTypes.shape({
      label: PropTypes.string.isRequired,
      defaultLabel: PropTypes.string.isRequired,
    }),
    versionLabel: PropTypes.shape({
      label: PropTypes.string.isRequired,
      defaultLabel: PropTypes.string.isRequired,
    }),
  })).isRequired,
  showInfoBar: PropTypes.func.isRequired,
  hideInfoBar: PropTypes.func.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};

AddItemsPage.defaultProps = {
  showOnly: false,
  savedStockMovement: {
    id: null,
    lineItems: [],
    lastUpdated: null,
    statusCode: null,
  },
  isOnline: true,
  isAutosaveEnabled: false,
};
