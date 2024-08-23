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

import { fetchReasonCodes, hideSpinner, showSpinner } from 'actions';
import picklistApi from 'api/services/PicklistApi';
import stockMovementItemApi from 'api/services/StockMovementItemApi';
import {
  STOCK_MOVEMENT_BY_ID, STOCK_MOVEMENT_CREATE_PICKLIST, STOCK_MOVEMENT_ITEM_BY_ID,
  STOCK_MOVEMENT_ITEMS,
} from 'api/urls';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import FilterInput from 'components/form-elements/FilterInput';
import LabelField from 'components/form-elements/LabelField';
import TableRowWithSubfields from 'components/form-elements/TableRowWithSubfields';
import EditPickModal from 'components/stock-movement-wizard/modals/EditPickModal';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import DateFormat from 'consts/dateFormat';
import { OutboundWorkflowState } from 'consts/WorkflowState';
import AlertMessage from 'utils/AlertMessage';
import {
  apiClientCustomResponseHandler as apiClient,
  handleSuccess,
  handleValidationErrors,
  parseResponse,
} from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import { formatProductDisplayName, matchesProductCodeOrName } from 'utils/form-values-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';
import { formatDate } from 'utils/translation-utils';

import 'react-confirm-alert/src/react-confirm-alert.css';

const FIELDS = {
  pickPageItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'picklistItems',
    getDynamicRowAttr: ({
      rowValues, subfield, showOnlyErroredItems, itemFilter,
    }) => {
      let className = rowValues.initial ? 'crossed-out ' : '';
      if (!subfield) { className += 'font-weight-bold'; }
      const filterOutItems = itemFilter &&
        !matchesProductCodeOrName({
          product: rowValues?.product,
          filterValue: itemFilter,
        });
      const hideRow = (
        (showOnlyErroredItems && !rowValues.hasError) || filterOutItems
      ) && !subfield;
      return { className, hideRow };
    },
    fields: {
      productCode: {
        type: LabelField,
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
        flexWidth: '0.9',
        headerAlign: 'left',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      product: {
        type: LabelField,
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
        flexWidth: '3.8',
        headerAlign: 'left',
        getDynamicAttr: ({ fieldValue }) => ({
          showValueTooltip: !!(fieldValue?.displayName || fieldValue?.displayNames?.default),
          tooltipValue: fieldValue?.name,
          color: fieldValue?.color,
        }),
        attributes: {
          className: 'text-left ml-1',
          formatValue: formatProductDisplayName,
        },
      },
      lotNumber: {
        type: LabelField,
        flexWidth: '1.1',
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
        attributes: {
          showValueTooltip: true,
        },
      },
      expirationDate: {
        type: LabelField,
        flexWidth: '0.9',
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        getDynamicAttr: ({ formatLocalizedDate }) => ({
          formatValue: (value) => formatLocalizedDate(value, DateFormat.COMMON),
        }),
      },
      binLocation: {
        type: LabelField,
        flexWidth: '1.1',
        label: 'react.stockMovement.binLocation.label',
        defaultMessage: 'Bin location',
        getDynamicAttr: ({ hasBinLocationSupport }) => ({
          hide: !hasBinLocationSupport,
        }),
        attributes: {
          showValueTooltip: true,
          formatValue: fieldValue => fieldValue && (
            <div className="d-flex">
              {fieldValue.zoneName ? <div className="text-truncate" style={{ minWidth: 30, flexShrink: 20 }}>{fieldValue.zoneName}</div> : ''}
              <div className="text-truncate">{fieldValue.zoneName ? `: ${fieldValue.name}` : fieldValue.name}</div>
            </div>),
        },
      },
      quantityRequired: {
        type: LabelField,
        label: 'react.stockMovement.quantityRequired.label',
        defaultMessage: 'Qty required',
        flexWidth: '0.8',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityPicked: {
        type: LabelField,
        label: 'react.stockMovement.quantityPicked.label',
        defaultMessage: 'Qty picked',
        flexWidth: '0.7',
        attributes: {
          formatValue: (value) => (
            <div className={!value ? 'text-danger' : null}>
              {value.toLocaleString('en-US')}
            </div>
          ),
        },
      },
      buttonEditPick: {
        label: 'react.stockMovement.pick.label',
        defaultMessage: 'Pick',
        type: EditPickModal,
        fieldKey: '',
        flexWidth: '0.7',
        attributes: {
          title: 'react.stockMovement.pick.label',
          defaultTitleMessage: 'Pick',
        },
        getDynamicAttr: ({
          fieldValue, subfield, updatePickPageItem,
          reasonCodes, hasBinLocationSupport, showOnly,
        }) => ({
          itemId: _.get(fieldValue, 'requisitionItem.id'),
          btnOpenDisabled: showOnly,
          subfield,
          btnOpenText: fieldValue && fieldValue.hasChangedPick ? '' : 'react.stockMovement.pick.label',
          btnOpenDefaultText: fieldValue && fieldValue.hasChangedPick ? '' : 'Pick',
          btnOpenClassName: fieldValue && fieldValue.hasChangedPick ? ' btn fa fa-check btn-outline-success' : 'btn btn-outline-primary',
          onResponse: updatePickPageItem,
          reasonCodes,
          hasBinLocationSupport,
        }),
      },
      buttonAdjustInventory: {
        label: 'react.stockMovement.adjustStock.label',
        defaultMessage: 'Adjust stock',
        buttonLabel: 'react.stockMovement.adjustStock.label',
        buttonDefaultMessage: 'Adjust stock',
        type: ButtonField,
        fieldKey: '',
        flexWidth: '1',
        attributes: {
          className: 'btn btn-outline-primary',
        },
        getDynamicAttr: ({ subfield, translate, showOnly }) => ({
          hidden: subfield,
          disabled: showOnly,
          onClick: () => Alert.error(translate('react.stockMovement.alert.disabledAdjustment.label', 'This feature is not available yet. Please adjust stock on the electronic stock card page.')),
        }),
      },
      revert: {
        type: ButtonField,
        label: 'react.default.button.undoEdit.label',
        defaultMessage: 'Undo edit',
        flexWidth: '1',
        fieldKey: '',
        buttonLabel: 'react.default.button.undoEdit.label',
        buttonDefaultMessage: 'Undo edit',
        getDynamicAttr: ({
          fieldValue, revertUserPick, subfield, showOnly,
        }) => ({
          onClick: _.get(fieldValue, 'requisitionItem.id') ? () => revertUserPick(fieldValue?.requisitionItem?.id) : () => null,
          hidden: subfield,
          disabled: showOnly,
        }),
        attributes: {
          className: 'btn btn-outline-danger',
        },
      },
    },
  },
};

/* eslint class-methods-use-this: ["error",{ "exceptMethods": ["checkForInitialPicksChanges"] }] */
/**
 * The forth step of stock movement(for movements from a depot) where user
 * can edit pick or adjust inventory.
 */
class PickPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      sorted: false,
      printPicksUrl: '',
      values: { ...this.props.initialValues, pickPageItems: [] },
      totalCount: 0,
      isFirstPageLoaded: false,
      showAlert: false,
      alertMessage: '',
      itemFilter: '',
      showOnlyErroredItems: false,
      isExportDropdownVisible: false,
      isPicklistCleared: false,
    };

    this.revertUserPick = this.revertUserPick.bind(this);
    this.updatePickPageItem = this.updatePickPageItem.bind(this);
    this.fetchAdjustedItems = this.fetchAdjustedItems.bind(this);
    this.sortByBins = this.sortByBins.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.recreatePicklist = this.recreatePicklist.bind(this);
    this.setState = this.setState.bind(this);

    apiClient.interceptors.response.use(handleSuccess, handleValidationErrors(this.setState));
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

  setPickPageItems(response, startIndex) {
    const { data } = response.data;
    this.setState({
      values: {
        ...this.state.values,
        pickPageItems: this.props.isPaginated ? _.uniqBy(_.concat(
          this.state.values.pickPageItems,
          _.map(
            parseResponse(data),
            item => this.checkForInitialPicksChanges(item),
          ),
        ), 'requisitionItem.id') : _.map(
          parseResponse(data),
          item => this.checkForInitialPicksChanges(item),
        ),
      },
      sorted: false,
    }, () => {
      // eslint-disable-next-line max-len
      if (!_.isNull(startIndex) && this.state.values.pickPageItems.length !== this.state.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      }
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

    this.fetchPickPageData();
    if (!this.props.isPaginated) {
      this.fetchPickPageItems();
    } else if (forceFetch) {
      this.setState({
        values: {
          ...this.state.values,
          pickPageItems: [],
        },
      }, () => {
        this.loadMoreRows({ startIndex: 0 });
      });
    }
  }

  /**
   * Checks if any changes has been made and adjusts initial pick.
   * @param {object} pickPageItem
   * @public
   */
  checkForInitialPicksChanges(pickPageItem) {
    if (pickPageItem.picklistItems.length && pickPageItem.autoAllocated) {
      const initialPicks = [];
      _.forEach(pickPageItem.suggestedItems, (suggestion) => {
        // search if suggested picks are inside picklist
        // if no -> add suggested pick as initial pick (to be crossed out)
        // if yes -> compare quantityPicked of item in picklist with sugestion
        const pick = _.find(
          pickPageItem.picklistItems,
          item => _.get(suggestion, 'inventoryItem.id') === _.get(item, 'inventoryItem.id') && _.get(item, 'binLocation.id') === _.get(suggestion, 'binLocation.id'),
        );
        if (_.isEmpty(pick) || (pick.quantityPicked !== suggestion.quantityPicked)) {
          initialPicks.push({
            ...suggestion,
            initial: true,
          });
        }
      });

      return { ...pickPageItem, picklistItems: _.concat(initialPicks, _.sortBy(pickPageItem.picklistItems, ['binLocation.name', 'initial'])) };
    }

    return pickPageItem;
  }

  /**
   * Fetches 4th step data from current stock movement.
   * @public
   */
  fetchPickPageData() {
    return apiClient.get(STOCK_MOVEMENT_BY_ID(this.state.values.stockMovementId), {
      params: { stepNumber: OutboundWorkflowState.PICK_ITEMS },
    })
      .then((resp) => {
        const { totalCount } = resp.data;
        const { associations, picklist } = resp.data.data;
        const printPicks = _.find(
          associations.documents,
          doc => doc.documentType === 'PICKLIST' && doc.uri.includes('print'),
        );
        this.setState({
          totalCount,
          printPicksUrl: printPicks ? printPicks.uri : '/',
          sorted: false,
          values: {
            ...this.state.values,
            picklist,
          },
        }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchPickPageItems() {
    apiClient.get(STOCK_MOVEMENT_ITEMS(this.state.values.stockMovementId), {
      params: { stepNumber: OutboundWorkflowState.PICK_ITEMS },
    }).then((response) => {
      this.setPickPageItems(response, null);
    });
  }

  fetchItemsAfterImport() {
    apiClient.get(STOCK_MOVEMENT_ITEMS(this.state.values.stockMovementId), {
      params: { stepNumber: OutboundWorkflowState.PICK_ITEMS, refreshPicklistItems: false },
    }).then((response) => {
      const { data } = response.data;
      this.setState({
        values: {
          ...this.state.values,
          pickPageItems: _.map(
            parseResponse(data),
            item => this.checkForInitialPicksChanges(item),
          ),
        },
        sorted: false,
      });
    });
  }

  loadMoreRows({ startIndex }) {
    if (this.state.totalCount) {
      this.setState({
        isFirstPageLoaded: true,
      });
      apiClient.get(STOCK_MOVEMENT_ITEMS(this.state.values.stockMovementId), {
        params: {
          offset: startIndex,
          max: this.props.pageSize,
          stepNumber: OutboundWorkflowState.PICK_ITEMS,
        },
      }).then((response) => {
        this.setPickPageItems(response, startIndex);
      });
    }
  }

  isRowLoaded({ index }) {
    return !!this.state.values.pickPageItems[index];
  }

  fetchAdjustedItems(adjustedProductCode) {
    apiClient.post(`/api/stockMovements/${this.state.values.stockMovementId}/updateAdjustedItems?adjustedProduct=${adjustedProductCode}`)
      .then((resp) => {
        const { pickPageItems } = resp.data.data.pickPage;

        this.setState({
          values: {
            ...this.state.values,
            pickPageItems: _.map(parseResponse(pickPageItems), item =>
              this.checkForInitialPicksChanges(item)),
          },
          sorted: false,
        }, () => this.props.hideSpinner());
      })
      .catch(() => { this.props.hideSpinner(); });
  }

  /**
   * Transition to next stock movement status (PICKED).
   * @public
   */
  transitionToNextStep() {
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'PICKED' };

    if (this.state.values.statusCode !== 'PICKED' && this.state.values.statusCode !== 'PACKED') {
      return apiClient.post(url, payload);
    }
    return Promise.resolve();
  }

  validatePicklist() {
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/validatePicklist`;
    return apiClient.get(url);
  }

  validateReasonCodes(lineItems) {
    const { pickPageItems } = lineItems;
    const invalidItem = _.find(
      pickPageItems,
      pickPageItem => pickPageItem.quantityRequired > pickPageItem.quantityPicked && _.find(
        pickPageItem.picklistItems,
        item => !item.reasonCode && !item.initial,
      ),
    );

    if (invalidItem) {
      this.setState({
        showAlert: true,
        alertMessage: `Product ${invalidItem.productCode} requires a reason code for the pick value. 
        Please add a reason code through the Edit Pick.`,
      });
      return false;
    }

    return true;
  }

  /**
   * Goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    if (!this.validateEmptyPicks(formValues)) {
      this.showEmptyPicksErrorMessage(formValues);
      return;
    }

    if (this.validateReasonCodes(formValues)) {
      this.props.showSpinner();
      this.validatePicklist()
        .then(() =>
          this.transitionToNextStep()
            .then(() => this.props.nextPage(formValues))
            .catch(() => this.props.hideSpinner()))
        .catch(() => this.props.hideSpinner());
    }
  }

  /**
   * Saves changes made in edit pick and updates data.
   * @param {object} pickPageItem
   * @public
   */
  updatePickPageItem(pickPageItem) {
    const pickPageItemIndex =
      _.findIndex(this.state.values.pickPageItems, item => _.get(item, 'requisitionItem.id') === _.get(pickPageItem, 'requisitionItem.id'));

    this.setState({
      values: {
        ...this.state.values,
        pickPageItems: update(this.state.values.pickPageItems, {
          [pickPageItemIndex]: {
            $set: this.checkForInitialPicksChanges(parseResponse(pickPageItem)),
          },
        }),
      },
    }, () => {
      const { values, showOnlyErroredItems } = this.state;
      const indexesOfEmptyPicks = this.getIndexesOfRowsWithEmptyPicks(values.pickPageItems);
      this.setState({
        showOnlyErroredItems: indexesOfEmptyPicks.length ? showOnlyErroredItems : false,
      });
    });
  }

  async revertToClearedPick(itemId) {
    try {
      await stockMovementItemApi.revertPick(itemId);
      await this.fetchRevertedItem(itemId);
    } finally {
      this.props.hideSpinner();
    }
  }

  async revertToAutoPick(itemId) {
    try {
      await apiClient.post(STOCK_MOVEMENT_CREATE_PICKLIST(itemId));
      await this.fetchRevertedItem(itemId);
    } finally {
      this.props.hideSpinner();
    }
  }

  async fetchRevertedItem(itemId) {
    const { data } = await apiClient.get(STOCK_MOVEMENT_ITEM_BY_ID(itemId), {
      params: { stepNumber: OutboundWorkflowState.PICK_ITEMS, refreshPicklistItems: false },
    });
    this.updatePickPageItem(data.data);
  }

  /**
   * Reverts to previous state of picks for requisition item
   * @param {string} itemId
   * @public
   */
  revertUserPick(itemId) {
    this.props.showSpinner();
    const { isPicklistCleared } = this.state;

    if (isPicklistCleared) {
      this.revertToClearedPick(itemId);
      return;
    }

    this.revertToAutoPick(itemId);
  }

  // Returns indexes of rows with quantity picked 0, and without subitems.
  getIndexesOfRowsWithEmptyPicks(pickPageItems) {
    return pickPageItems.reduce((acc, item, index) => {
      // When the quantity picked is equal to 0 we have to check its subitems,
      // because the item can be edited to 0, not cleared.
      if (!item.quantityPicked && !item.picklistItems.length) {
        return [...acc, index];
      }

      return acc;
    }, []);
  }

  // Displaying an error message when an item with a quantity picked equal to 0 exists
  showEmptyPicksErrorMessage(formValues) {
    const emptyPicks = this.getIndexesOfRowsWithEmptyPicks(formValues.pickPageItems);
    const emptyPickLinesNumber = emptyPicks.map((index) => index + 1).join(', ');
    const alertMessage = this.props.translate(
      'react.stockMovement.missingPickedLot.label',
      `The picked lot is missing at rows: ${emptyPickLinesNumber}`,
      {
        rows: emptyPickLinesNumber,
      },
    );

    this.setState({
      alertMessage,
      showAlert: emptyPicks.length,
    });
  }

  // Managing hasError property depending on the value of quantity
  // picked and on the existing subitems
  validateEmptyPicks(formValues) {
    const emptyPicks = this.getIndexesOfRowsWithEmptyPicks(formValues.pickPageItems);

    const pickPageItems = formValues.pickPageItems.map((item, index) => {
      if (emptyPicks.includes(index)) {
        return { ...item, hasError: true };
      }

      return { ...item, hasError: false };
    });

    this.setState({
      values: {
        ...this.state.values,
        pickPageItems,
      },
    });

    return !emptyPicks.length;
  }

  sortByBins() {
    const { sorted } = this.state;
    let sortedValues;

    if (!sorted) {
      sortedValues = _.orderBy(this.state.values.pickPageItems, ['picklistItems[0].binLocation.name'], ['asc']);
    } else {
      sortedValues = _.orderBy(this.state.values.pickPageItems, ['sortOrder'], ['asc']);
    }

    this.setState({
      values: {
        ...this.state.values,
        pickPageItems: [],
      },
    }, () => this.setState({
      values: {
        ...this.state.values,
        pickPageItems: sortedValues,
      },
      sorted: !this.state.sorted,
    }));
  }

  exportTemplate(formValues) {
    this.props.showSpinner();
    const { movementNumber, stockMovementId } = formValues;

    const fileName = `PickListItems${movementNumber ? `-${movementNumber}` : ''}-template`;
    picklistApi.exportPicklistTemplate(stockMovementId, { fileName })
      .finally(() => this.props.hideSpinner());
  }

  exportPick(formValues) {
    this.props.showSpinner();
    const { movementNumber, stockMovementId } = formValues;

    const fileName = `PickListItems${movementNumber ? `-${movementNumber}` : ''}`;
    picklistApi.exportPicklistItems(stockMovementId, { fileName })
      .finally(() => this.props.hideSpinner());
  }

  importTemplate(event) {
    this.props.showSpinner();
    if (this.state.showAlert) {
      this.setState({ alertMessage: null, showAlert: false });
    }
    const file = event.target.files[0];
    const { stockMovementId } = this.state.values;

    return picklistApi.importPicklist(stockMovementId, file)
      .then((resp) => {
        const { errors } = resp.data;
        if (errors) {
          this.setState({
            showAlert: true,
            alertMessage: errors,
          });
        }

        this.props.hideSpinner();
        this.fetchItemsAfterImport();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  recreatePicklist() {
    const url = `/api/stockMovements/createPickList/${this.state.values.stockMovementId}`;
    this.props.showSpinner();

    if (this.state.showAlert) {
      this.setState({ alertMessage: null, showAlert: false });
    }
    apiClient.get(url)
      .then(() => this.fetchAllData(true))
      .finally(() => {
        this.setState({ isPicklistCleared: false });
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
        'react.stockMovement.confirmPickRefresh.message',
        'This button will redo the autopick on all items. Are you sure you want to continue?',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => {
            this.recreatePicklist();
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  confirmClearPicklist() {
    confirmAlert({
      title: this.props.translate('react.stockMovement.confirmAlert.clearPick.title.label', 'Confirm clear pick'),
      message: this.props.translate(
        'react.stockMovement.confirmAlert.clearPick.label',
        'Clear pick will make all the pick lines on this page empty. You will have to enter the pick information manually. You should use this option when you have already picked or sent the items physically. Are you sure you want to proceed?',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => {
            this.clearPicklist();
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  confirmPreviousPage(values, emptyPicksCount) {
    confirmAlert({
      title: this.props.translate('react.default.areYouSure.label', 'Are you sure?'),
      message: this.props.translate(
        'react.stockMovement.confirmAlert.pickPage.previousPage.label',
        `You have ${emptyPicksCount} line/lines with an empty pick. If you go back, autopick will be generated for all empty lines. All other edits will remain saved. Are you sure you want to proceed?`,
        {
          emptyPicksCount,
        },
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => {
            this.props.previousPage(values);
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  async clearPicklist() {
    const { picklist } = this.state.values;
    if (this.state.showAlert) {
      this.setState({ alertMessage: null, showAlert: false });
    }
    this.props.showSpinner();
    try {
      await picklistApi.clearPicklist(picklist?.id);
      this.setState({ isPicklistCleared: true });
      const picklistItems = this.state.values.pickPageItems;
      this.setState((prevState) => ({
        values: ({
          ...prevState.values,
          pickPageItems: picklistItems
            .map((item) => ({ ...item, picklistItems: [], quantityPicked: 0 })),
        }),
      }));
    } finally {
      this.props.hideSpinner();
    }
  }

  /**
   * Method to check if any item is missing the pick
   */
  isMissingPick() {
    const { pickPageItems } = this.state.values;
    // Check if any item has an empty pick (picklistItems array empty)
    return pickPageItems.some((item) => !item.picklistItems?.length);
  }

  onSaveAndExit(values) {
    if (!this.validateEmptyPicks(values)) {
      this.showEmptyPicksErrorMessage(values);
      return;
    }

    window.location = STOCK_MOVEMENT_URL.show(values.stockMovementId);
  }

  handleExportDropdown() {
    this.setState((prevState) => ({ isExportDropdownVisible: !prevState.isExportDropdownVisible }));
  }

  render() {
    const {
      showOnlyErroredItems,
      itemFilter,
      showAlert,
      alertMessage,
      isExportDropdownVisible,
    } = this.state;
    const { showOnly } = this.props;
    const emptyPicksCount =
      this.getIndexesOfRowsWithEmptyPicks(this.state.values?.pickPageItems).length;

    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values }) => (
          <div className="d-flex flex-column">
            <AlertMessage show={showAlert} message={alertMessage} danger />
            { !showOnly
              ? (
                <span className="buttons-container">
                  <FilterInput
                    itemFilter={itemFilter}
                    onChange={(e) => this.setState({itemFilter: e.target.value})}
                    onClear={() => this.setState({itemFilter: ''})}
                  />
                  {this.isMissingPick() && (
                    <button
                      type="button"
                      onClick={() => {
                        if (emptyPicksCount) {
                          this.validateEmptyPicks(values);
                          this.setState({showOnlyErroredItems: !showOnlyErroredItems});
                        }
                      }}
                      className={`float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-3 ${showOnlyErroredItems ? 'active' : ''}`}
                    >
                    <span>
                      {emptyPicksCount}
                      {' '}
                      <Translate
                        id="react.stockMovement.erroredItemsCount.label"
                        defaultMessage="item(s) require your attention"
                      />
                    </span>
                    </button>
                  )}
                  <label
                    htmlFor="csvInput"
                    className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs mr-1"
                  >
                    <span>
                      <i className="fa fa-download pr-2"/>
                      <Translate
                        id="react.default.button.importTemplate.label"
                        defaultMessage="Import template"
                      />
                    </span>
                    <input
                      id="csvInput"
                      type="file"
                      style={{display: 'none'}}
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
                      onClick={() => this.handleExportDropdown()}
                      className="dropdown-button float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                    >
                      <span>
                        <i className="fa fa-sign-out pr-2"/>
                        Export
                      </span>
                    </button>
                    <div className={`dropdown-content print-buttons-container col-md-3 flex-grow-1
                      ${isExportDropdownVisible ? 'visible' : ''}`}
                    >
                      <a
                        href="#"
                        className="py-1 mb-1 btn btn-outline-secondary"
                        rel="noopener noreferrer"
                        onClick={() => this.exportPick(values)}
                      >
                        <span>
                          <i className="fa fa-upload pr-2"/>
                          <Translate
                            id="react.stockMovement.pickListItem.export.label"
                            defaultMessage="Export Pick"
                          />
                        </span>
                      </a>
                      <a
                        href="#"
                        className="py-1 mb-1 btn btn-outline-secondary"
                        rel="noopener noreferrer"
                        onClick={() => this.exportTemplate(values)}
                      >
                        <span>
                          <i className="fa fa-upload pr-2"/>
                          <Translate
                            id="react.default.button.exportTemplate.label"
                            defaultMessage="Export template"
                          />
                        </span>
                      </a>
                    </div>
                  </div>
                  <a
                    href={`${this.state.printPicksUrl}${this.state.sorted ? '?sorted=true' : ''}`}
                    className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <span>
                      <i className="fa fa-print pr-2"/>
                      <Translate
                        id="react.stockMovement.printPicklist.label"
                        defaultMessage="Print picklist"
                      />
                    </span>
                  </a>
                  <button
                    type="button"
                    onClick={() => this.confirmClearPicklist()}
                    className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
                  >
                    <span>
                      <i className="fa fa-refresh pr-2"/>
                      <Translate
                        id="react.stockMovement.clearPick.label"
                        defaultMessage="Clear pick"
                      />
                    </span>
                  </button>
                  <button
                    type="button"
                    onClick={() => this.refresh()}
                    className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
                  >
                    <span>
                      <i className="fa fa-refresh pr-2" />
                      <Translate
                        id="react.stockMovement.button.redoAutopick.label"
                        defaultMessage="Redo Autopick"
                      />
                    </span>
                  </button>
                  <button
                    type="button"
                    onClick={() => this.onSaveAndExit(values)}
                    className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
                    disabled={emptyPicksCount}
                  >
                    <span>
                      <i className="fa fa-sign-out pr-2"/>
                      <Translate
                        id="react.default.button.saveAndExit.label"
                        defaultMessage="Save and exit"
                      />
                    </span>
                  </button>
                  <button
                    type="button"
                    onClick={() => this.sortByBins()}
                    className="float-right ml-1 mb-1 btn btn-outline-secondary align-self-end btn-xs"
                  >
                    {this.state.sorted && (
                      <span>
                      <i className="fa fa-sort pr-2"/>
                      <Translate
                        id="react.stockMovement.originalOrder.label"
                        defaultMessage="Original order"
                      />
                    </span>
                    )}
                    {!this.state.sorted && (
                      <span>
                      <i className="fa fa-sort pr-2"/>
                      <Translate
                        id="react.stockMovement.sortByBins.label"
                        defaultMessage="Sort by bins"
                      />
                    </span>
                    )}
                  </button>
                </span>
              )
              : (
                <button
                  type="button"
                  onClick={() => this.props.history.push(STOCK_MOVEMENT_URL.listRequest())}
                  className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
                >
                  <span>
                    <i className="fa fa-sign-out pr-2"/>
                    <Translate
                      id="react.default.button.exit.label"
                      defaultMessage="Exit"
                    />
                  </span>
                </button>
              )}
            <form onSubmit={handleSubmit} className="print-mt">
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  stockMovementId: values.stockMovementId,
                  updatePickPageItem: this.updatePickPageItem,
                  fetchAdjustedItems: this.fetchAdjustedItems,
                  revertUserPick: this.revertUserPick,
                  locationId: this.state.values.origin.id,
                  reasonCodes: this.props.reasonCodes,
                  translate: this.props.translate,
                  hasBinLocationSupport: this.props.hasBinLocationSupport,
                  totalCount: this.state.totalCount,
                  loadMoreRows: this.loadMoreRows,
                  isRowLoaded: this.isRowLoaded,
                  isPaginated: this.props.isPaginated,
                  showOnlyErroredItems,
                  showOnly,
                  isFirstPageLoaded: this.state.isFirstPageLoaded,
                  itemFilter,
                  formatLocalizedDate: this.props.formatLocalizedDate,
                }))}
              </div>
              <div className="d-print-none submit-buttons">
                <button
                  type="button"
                  disabled={showOnly}
                  className="btn btn-outline-primary btn-form btn-xs"
                  onClick={() => (emptyPicksCount
                    ? this.confirmPreviousPage(values, emptyPicksCount)
                    : this.props.previousPage(values))}
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button type="submit" disabled={showOnly} className="btn btn-outline-primary btn-form float-right btn-xs">
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
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  reasonCodes: state.reasonCodes.data,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  isPaginated: state.session.isPaginated,
  pageSize: state.session.pageSize,
  currentLocale: state.session.activeLanguage,
  formatLocalizedDate: formatDate(state.localize),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner, fetchReasonCodes })(PickPage);

PickPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  nextPage: PropTypes.func.isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function fetching reason codes */
  fetchReasonCodes: PropTypes.func.isRequired,
  /** Array of available reason codes */
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  /** Return true if show only */
  showOnly: PropTypes.bool.isRequired,
  pageSize: PropTypes.number.isRequired,
  currentLocale: PropTypes.string.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  formatLocalizedDate: PropTypes.func.isRequired,
};
