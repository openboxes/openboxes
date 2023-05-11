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
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import FilterInput from 'components/form-elements/FilterInput';
import LabelField from 'components/form-elements/LabelField';
import SelectField from 'components/form-elements/SelectField';
import TableRowWithSubfields from 'components/form-elements/TableRowWithSubfields';
import TextField from 'components/form-elements/TextField';
import DetailsModal from 'components/stock-movement-wizard/modals/DetailsModal';
import SubstitutionsModal from 'components/stock-movement-wizard/modals/SubstitutionsModal';
import apiClient from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import { formatProductDisplayName, matchesProductCodeOrName, showOutboundEditValidationErrors } from 'utils/form-values-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


const BTN_CLASS_MAPPER = {
  YES: 'btn btn-outline-success',
  NO: 'btn btn-outline-secondary',
  EARLIER: 'btn btn-outline-warning',
  HIDDEN: 'btn invisible',
};

const FIELDS = {
  editPageItems: {
    type: ArrayField,
    arrowsNavigation: true,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    rowComponent: TableRowWithSubfields,
    getDynamicRowAttr: ({
      rowValues, subfield, showOnlyErroredItems, itemFilter,
    }) => {
      let className = rowValues.statusCode === 'SUBSTITUTED' ? 'crossed-out ' : '';
      if (rowValues.quantityAvailable < rowValues.quantityRequested) {
        className += 'font-weight-bold';
      }
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
    subfieldKey: 'substitutionItems',
    fields: {
      productCode: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '0.6',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
        attributes: {
          showValueTooltip: true,
        },
      },
      product: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '3.5',
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
        attributes: {
          formatValue: formatProductDisplayName,
        },
        getDynamicAttr: ({ subfield, fieldValue }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
          showValueTooltip: !!fieldValue?.displayNames?.default,
          tooltipValue: fieldValue?.name,
        }),
      },
      quantityRequested: {
        type: LabelField,
        label: 'react.stockMovement.requested.label',
        defaultMessage: 'Requested',
        flexWidth: '1.1',
        headerAlign: 'right',
        attributes: {
          className: 'text-right',
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityOnHand: {
        type: LabelField,
        label: 'react.stockMovement.onHand.label',
        defaultMessage: 'On Hand',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue }) => {
          let className = 'text-right';
          if (fieldValue && (!fieldValue.quantityOnHand ||
            fieldValue.quantityOnHand < fieldValue.quantityRequested)) {
            className = `${className} text-danger`;
          }
          return {
            className,
          };
        },
        headerAlign: 'right',
        attributes: {
          formatValue: value => (value.quantityOnHand ? (value.quantityOnHand.toLocaleString('en-US')) : value.quantityOnHand),
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.available.label',
        defaultMessage: 'Available',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue }) => {
          let className = 'text-right';
          if (fieldValue && (!fieldValue.quantityAvailable ||
            fieldValue.quantityAvailable < fieldValue.quantityRequested)) {
            className = `${className} text-danger`;
          }
          return {
            className,
          };
        },
        headerAlign: 'right',
        attributes: {
          formatValue: value => (value.quantityAvailable ? (value.quantityAvailable.toLocaleString('en-US')) : value.quantityAvailable),
        },
      },
      quantityDemandFulfilling: {
        type: LabelField,
        label: 'react.stockMovement.demandPerMonth.label',
        defaultMessage: 'Demand per Month',
        flexWidth: '1.5',
        getDynamicAttr: () => ({
          formatValue: (value) => {
            if (value && value !== '0') {
              return value.toLocaleString('en-US');
            }

            return '0';
          },
          showValueTooltip: true,
        }),
        headerAlign: 'right',
        attributes: {
          className: 'text-right',
        },
      },
      detailsButton: {
        label: 'react.stockMovement.details.label',
        defaultMessage: 'Details',
        type: DetailsModal,
        flexWidth: '1',
        fieldKey: '',
        attributes: {
          title: 'react.stockMovement.pendingRequisitionDetails.label',
          defaultTitleMessage: 'Pending Requisition Details',
        },
        getDynamicAttr: ({ fieldValue, stockMovementId, values }) => ({
          productId: fieldValue && fieldValue.product && fieldValue.product.id,
          productCode: fieldValue && fieldValue.product && fieldValue.product.productCode,
          productName: fieldValue && fieldValue.product && fieldValue.product.name,
          displayName: fieldValue?.product?.displayNames?.default,
          originId: values && values.origin && values.origin.id,
          stockMovementId,
          quantityRequested: fieldValue && fieldValue.quantityRequested,
          quantityOnHand: fieldValue && fieldValue.quantityOnHand,
          quantityAvailable: fieldValue && fieldValue.quantityAvailable,
          btnOpenText: 'react.stockMovement.details.label',
          btnOpenDefaultText: 'Details',
          btnCancelText: 'Close',
          btnSaveStyle: { display: 'none' },
          btnContainerClassName: 'float-right',
          btnOpenAsIcon: true,
          btnOpenStyle: { border: 'none', cursor: 'pointer' },
          btnOpenIcon: 'fa-search',
        }),
      },
      substituteButton: {
        label: 'react.stockMovement.substitution.label',
        defaultMessage: 'Substitution',
        type: SubstitutionsModal,
        fieldKey: '',
        flexWidth: '1',
        attributes: {
          title: 'react.stockMovement.substitutes.label',
          defaultTitleMessage: 'Substitutes',
        },
        getDynamicAttr: ({
          fieldValue, rowIndex, stockMovementId, onResponse,
          reviseRequisitionItems, values, reasonCodes, showOnly,
        }) => ({
          onOpen: () => reviseRequisitionItems(values),
          productCode: fieldValue && fieldValue.productCode,
          btnOpenText: `react.stockMovement.${fieldValue && fieldValue.substitutionStatus}.label`,
          btnOpenDefaultText: `${fieldValue && fieldValue.substitutionStatus}`,
          btnOpenDisabled: (fieldValue && fieldValue.statusCode === 'SUBSTITUTED') || showOnly,
          btnOpenClassName: BTN_CLASS_MAPPER[(fieldValue && fieldValue.substitutionStatus) || 'HIDDEN'],
          rowIndex,
          lineItem: fieldValue,
          stockMovementId,
          onResponse,
          reasonCodes,
        }),
      },
      quantityRevised: {
        label: 'react.stockMovement.quantityRevised.label',
        defaultMessage: 'Qty revised',
        type: TextField,
        fieldKey: 'statusCode',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          fieldValue, subfield, showOnly, updateRow, values, rowIndex,
        }) => ({
          disabled: (fieldValue && fieldValue === 'SUBSTITUTED') || subfield || showOnly,
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      reasonCode: {
        type: SelectField,
        label: 'react.stockMovement.reasonCode.label',
        defaultMessage: 'Reason code',
        flexWidth: '1.4',
        fieldKey: 'quantityRevised',
        getDynamicAttr: ({
          fieldValue, subfield, reasonCodes, updateRow, values, rowIndex,
        }) => {
          const isSubstituted = fieldValue && fieldValue.statusCode === 'SUBSTITUTED';
          return {
            disabled: fieldValue === null || fieldValue === undefined || subfield || isSubstituted,
            options: reasonCodes,
            showValueTooltip: true,
            onBlur: () => updateRow(values, rowIndex),
          };
        },
      },
      revert: {
        type: ButtonField,
        label: 'react.default.button.undo.label',
        defaultMessage: 'Undo',
        flexWidth: '1',
        fieldKey: '',
        buttonLabel: 'react.default.button.undo.label',
        buttonDefaultMessage: 'Undo',
        getDynamicAttr: ({
          fieldValue, revertItem, values, showOnly,
        }) => ({
          onClick: fieldValue && fieldValue.requisitionItemId ?
            () => revertItem(values, fieldValue.requisitionItemId) : () => null,
          hidden: fieldValue && fieldValue.statusCode ? !_.includes(['CHANGED', 'CANCELED'], fieldValue.statusCode) : false,
          disabled: showOnly,
        }),
        attributes: {
          className: 'btn btn-outline-danger',
        },
      },
    },
  },
};

function validateForSave(values) {
  const errors = {};
  errors.editPageItems = [];

  _.forEach(values.editPageItems, (item, key) => {
    if (!_.isEmpty(item.quantityRevised) && _.isEmpty(item.reasonCode)) {
      errors.editPageItems[key] = { reasonCode: 'react.stockMovement.errors.reasonCodeRequired.label' };
    } else if (_.isNil(item.quantityRevised) && !_.isEmpty(item.reasonCode) && item.statusCode !== 'SUBSTITUTED') {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.revisedQuantityRequired.label' };
    }
    if (parseInt(item.quantityRevised, 10) === item.quantityRequested) {
      errors.editPageItems[key] = {
        quantityRevised: 'react.stockMovement.errors.sameRevisedQty.label',
      };
    }
    if (!_.isEmpty(item.quantityRevised) && item.quantityAvailable >= 0 &&
      (item.quantityRevised > item.quantityAvailable)) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.higherQty.label' };
    }
    if (!_.isEmpty(item.quantityRevised) && (item.quantityRevised < 0)) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.negativeQty.label' };
    }
  });
  return errors;
}

/**
 * The third step of stock movement(for movements from a depot) where user can see the
 * stock available and adjust quantities or make substitutions based on that information.
 */
class EditItemsPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      statusCode: '',
      revisedItems: [],
      values: { ...this.props.initialValues, editPageItems: [] },
      hasItemsLoaded: false,
      totalCount: 0,
      isFirstPageLoaded: false,
      showOnlyErroredItems: false,
      itemFilter: '',
    };

    this.revertItem = this.revertItem.bind(this);
    this.fetchEditPageItems = this.fetchEditPageItems.bind(this);
    this.reviseRequisitionItems = this.reviseRequisitionItems.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateRow = this.updateRow.bind(this);
    this.markErroredLines = this.markErroredLines.bind(this);
    this.validate = this.validate.bind(this);
    this.props.showSpinner();
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

  setEditPageItems(response, startIndex) {
    this.props.showSpinner();
    const { data } = response.data;
    const editPageItems = _.map(
      data,
      val => ({
        ...val,
        disabled: true,
        quantityOnHand: val.quantityOnHand > 0 ? val.quantityOnHand : 0,
        quantityAvailable:
            val.quantityAvailable > 0 ? val.quantityAvailable : 0,
        product: {
          ...val.product,
          label: `${val.productCode} ${val.productName}`,
        },
        // eslint-disable-next-line max-len
        reasonCode: _.find(this.props.reasonCodes, ({ value }) => _.includes(val.reasonCode, value)),
        substitutionItems: _.map(val.substitutionItems, sub => ({
          ...sub,
          // eslint-disable-next-line max-len
          reasonCode: _.find(this.props.reasonCodes, ({ value }) => _.includes(val.reasonCode, value)),
          requisitionItemId: val.requisitionItemId,
          product: {
            ...sub.product,
            label: `${sub.productCode} ${sub.productName}`,
          },
        })),
      }),
    );

    this.setState({
      revisedItems: _.filter(editPageItems, item => item.statusCode === 'CHANGED'),
      values: {
        ...this.state.values,
        editPageItems: _.uniqBy(_.concat(this.state.values.editPageItems, editPageItems), 'requisitionItemId'),
      },
      hasItemsLoaded: this.state.hasItemsLoaded
      || this.state.totalCount === _.uniqBy(_.concat(this.state.values.editPageItems, editPageItems), 'requisitionItemId').length,
    }, () => {
      // eslint-disable-next-line max-len
      if (!_.isNull(startIndex) && this.state.values.editPageItems.length !== this.state.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      }
      this.props.hideSpinner();
    });
  }

  validate(values) {
    const errors = validateForSave(values);

    _.forEach(values.editPageItems, (item, key) => {
      if (_.isNil(item.quantityRevised) && (item.quantityRequested > item.quantityAvailable) && (item.statusCode !== 'SUBSTITUTED')) {
        errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.lowerQty.label' };
      }
    });

    this.markErroredLines(values, errors);

    return errors;
  }

  markErroredLines(values, errors) {
    let updatedValues = values;

    _.forEach(this.state.values.editPageItems, (item, itemIdx) => {
      updatedValues = update(updatedValues, {
        editPageItems: {
          [itemIdx]: {
            hasError: {
              $set: !!_.find(errors.editPageItems, (error, errorIdx) => itemIdx === errorIdx),
            },
          },
        },
      });
    });

    this.setState({
      values: updatedValues,
      showOnlyErroredItems: !errors.editPageItems.length ? false : this.state.showOnlyErroredItems,
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
    this.props.fetchReasonCodes();

    this.fetchEditPageData().then((resp) => {
      const { statusCode } = resp.data.data;
      const { totalCount } = resp.data;

      this.setState({
        statusCode,
        totalCount,
      }, () => {
        if (!this.props.isPaginated || forceFetch) {
          this.fetchItems();
        }
      });
    }).catch(() => {
      this.props.hideSpinner();
    });
  }

  fetchItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=3`;
    apiClient.get(url)
      .then((response) => {
        this.setEditPageItems(response, null);
        this.setState({
          hasItemsLoaded: true,
        });
      });
  }

  /**
   * Saves changes made in subsitution modal and updates data.
   * @public
   */
  fetchEditPageItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=3`;
    apiClient.get(url)
      .then((response) => {
        const { data } = response.data;
        this.setState({
          hasItemsLoaded: true,
          values: {
            ...this.state.values,
            editPageItems: _.map(data, item => ({
              ...item,
              quantityOnHand: item.quantityOnHand || 0,
              // eslint-disable-next-line max-len
              reasonCode: _.find(this.props.reasonCodes, ({ value }) => _.includes(item.reasonCode, value)),
              substitutionItems: _.map(item.substitutionItems, sub => ({
                ...sub,
                // eslint-disable-next-line max-len
                reasonCode: _.find(this.props.reasonCodes, ({ value }) => _.includes(item.reasonCode, value)),
                requisitionItemId: item.requisitionItemId,
              })),
            })),
          },
        }, () => {
          this.fetchAllData(false);
          this.props.hideSpinner();
        });
      }).catch(() => {
        this.props.hideSpinner();
      });
  }

  loadMoreRows({ startIndex }) {
    if (this.state.totalCount) {
      this.setState({
        isFirstPageLoaded: true,
      });
      const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${this.props.pageSize}&stepNumber=3`;
      apiClient.get(url)
        .then((response) => {
          this.setEditPageItems(response, startIndex);
        });
    }
  }

  isRowLoaded({ index }) {
    return !!this.state.values.editPageItems[index];
  }

  /**
   * Sends data of revised items with post method.
   * @param {object} values
   * @public
   */
  reviseRequisitionItems(values) {
    const itemsToRevise = _.filter(
      values.editPageItems,
      (item) => {
        if (item.quantityRevised && item.reasonCode) {
          const oldRevision = _.find(
            this.state.revisedItems,
            revision => revision.requisitionItemId === item.requisitionItemId,
          );
          return _.isEmpty(oldRevision) ? true :
            ((_.toInteger(oldRevision.quantityRevised) !== _.toInteger(item.quantityRevised)) ||
              (oldRevision.reasonCode !== item.reasonCode));
        }
        return false;
      },
    );

    let updatedValues = values;

    _.forEach(itemsToRevise, (item) => {
      const editPageItemIndex = _.findIndex(this.state.values.editPageItems, editPageItem =>
        item.requisitionItemId === editPageItem.requisitionItemId);

      updatedValues = update(updatedValues, {
        editPageItems: {
          [editPageItemIndex]: {
            statusCode: {
              $set: 'CHANGED',
            },
          },
        },
      });
    });

    this.setState({
      values: updatedValues,
    });

    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/reviseItems`;
    const payload = {
      lineItems: _.map(itemsToRevise, item => ({
        id: item.requisitionItemId,
        quantityRevised: item.quantityRevised,
        reasonCode: item.reasonCode.value,
      })),
    };

    if (payload.lineItems.length) {
      return apiClient.post(url, payload);
    }

    return Promise.resolve();
  }

  updateRow(values, index) {
    const item = values.editPageItems[index];
    let val = values;
    val = update(values, {
      editPageItems: { [index]: { $set: item } },
    });
    this.setState({
      values: val,
    });
  }

  /**
   * Saves list of requisition items in current step (without step change).
   * @param {object} formValues
   * @public
   */
  save(formValues) {
    this.props.showSpinner();

    const errors = validateForSave(formValues).editPageItems;

    if (errors.length) {
      showOutboundEditValidationErrors({ translate: this.props.translate, errors });

      this.props.hideSpinner();
      return null;
    }

    return this.reviseRequisitionItems(formValues)
      .then((resp) => {
        // If reponse 200, then save revise items taken from the payload
        const payload = JSON.parse(resp.config.data);
        if (payload.lineItems && payload.lineItems.length) {
          const savedItemIds = payload.lineItems.map(item => item.id);
          // Map to have the required field
          // (requisitionItemId, quantityRevised and reasonCode as obj)
          const savedItems = payload.lineItems.map(item => ({
            ...item,
            requisitionItemId: item.id,
            reasonCode: _.find(
              this.props.reasonCodes,
              ({ value }) => _.includes(item.reasonCode, value),
            ),
          }));
          // Get old revise items, that were not changed in this request
          const oldItems = _.filter(
            this.state.values.editPageItems,
            item => savedItemIds.indexOf(item.requisitionItemId) < 0,
          );
          this.setState({
            revisedItems: [...oldItems, ...savedItems],
          });
        }

        this.props.hideSpinner();
        Alert.success(this.props.translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'), { timeout: 3000 });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Reload the data, all not saved changes will be lost.
   * @public
   */
  reload() {
    this.setState({
      revisedItems: [],
      values: { ...this.props.initialValues, editPageItems: [] },
      hasItemsLoaded: false,
      totalCount: 0,
      isFirstPageLoaded: false,
    });
    this.fetchAllData(true);
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
          onClick: () => this.reload(),
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Transition to next stock movement status (PICKING)
   * after sending createPicklist: 'true' to backend autopick functionality is invoked.
   * @public
   */
  transitionToNextStep() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = {
      status: 'PICKING',
      createPicklist: this.state.statusCode === 'REQUESTED' ? 'true' : 'false',
    };

    return apiClient.post(url, payload);
  }

  /**
   * Fetches 3rd step data from current stock movement.
   * @public
   */
  fetchEditPageData() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    this.props.showSpinner();
    this.reviseRequisitionItems(formValues)
      .then(() => {
        this.transitionToNextStep()
          .then(() => this.props.nextPage(formValues))
          .catch(() => this.reload());
      }).catch(() => this.props.hideSpinner());
  }

  /**
   * Saves changes made when item reverted.
   * @param {object} editPageItem
   * @public
   */
  updateEditPageItem(values, editPageItem) {
    const editPageItemIndex = _.findIndex(this.state.values.editPageItems, item =>
      item.requisitionItemId === editPageItem.requisitionItemId);
    const revisedItemIndex = _.findIndex(this.state.revisedItems, item =>
      item.requisitionItemId === editPageItem.requisitionItemId);

    this.setState({
      values: {
        ...values,
        editPageItems: update(values.editPageItems, {
          [editPageItemIndex]: {
            $set: {
              ...editPageItem,
              quantityOnHand: editPageItem.quantityOnHand || 0,
              quantityAvailable: editPageItem.quantityAvailable || 0,
              substitutionItems: _.map(editPageItem.substitutionItems, sub => ({
                ...sub,
                requisitionItemId: editPageItem.requisitionItemId,
              })),
            },
          },
        }),
      },
      revisedItems: update(this.state.revisedItems, revisedItemIndex > -1 ? {
        $splice: [[revisedItemIndex, 1]],
      } : {}),
    });
  }

  /**
   * Saves changes made by user in this step and redirects to the shipment view page
   * @param {object} formValues
   * @public
   */
  saveAndExit(formValues) {
    const errors = validateForSave(formValues).editPageItems;

    if (errors.length) {
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
      this.props.hideSpinner();
    } else {
      this.reviseRequisitionItems(formValues)
        .then(() => {
          window.location = `/openboxes/stockMovement/show/${formValues.stockMovementId}`;
        });
    }
  }

  /**
   * Reverts to previous state of requisition item (reverts substitutions and quantity revisions)
   * @param {string} itemId
   * @public
   */
  revertItem(values, itemId) {
    this.props.showSpinner();
    const revertItemsUrl = `/openboxes/api/stockMovementItems/${itemId}/revertItem`;
    const itemsUrl = `/openboxes/api/stockMovementItems/${itemId}?stepNumber=3`;

    return apiClient.post(revertItemsUrl)
      .then(() => apiClient.get(itemsUrl)
        .then((response) => {
          const editPageItem = response.data.data;
          this.updateEditPageItem(values, editPageItem);
          this.props.hideSpinner();
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.stockMovement.error.revertRequisitionItem.label', 'Could not revert requisition items')));
        }))
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.stockMovement.error.revertRequisitionItem.label', 'Could not revert requisition items')));
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
      this.reviseRequisitionItems(values)
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
    const { showOnlyErroredItems, itemFilter } = this.state;
    const { showOnly } = this.props;
    const erroredItemsCount = this.state.values && this.state.values.editPageItems.length > 0 ? _.filter(this.state.values.editPageItems, item => item.hasError).length : '0';
    return (
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            { !showOnly ?
              <span className="buttons-container">
                <FilterInput
                  itemFilter={itemFilter}
                  onChange={e => this.setState({ itemFilter: e.target.value })}
                  onClear={() => this.setState({ itemFilter: '' })}
                />
                <button
                  type="button"
                  onClick={() => this.setState({ showOnlyErroredItems: !showOnlyErroredItems })}
                  className={`float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-3 ${showOnlyErroredItems ? 'active' : ''}`}
                >
                  <span>{erroredItemsCount} <Translate id="react.stockMovement.erroredItemsCount.label" defaultMessage="item(s) require your attention" /></span>
                </button>
                <button
                  type="button"
                  onClick={() => this.refresh()}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-3"
                >
                  <span><i className="fa fa-refresh pr-2" /><Translate
                    id="react.default.button.refresh.label"
                    defaultMessage="Reload"
                  />
                  </span>
                </button>
                <button
                  type="button"
                  onClick={() => this.save(values)}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end ml-3 btn-xs ml-3"
                >
                  <span><i className="fa fa-save pr-2" /><Translate
                    id="react.default.button.save.label"
                    defaultMessage="Save"
                  />
                  </span>
                </button>
                <button
                  type="button"
                  onClick={() => this.saveAndExit(values)}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end ml-3 btn-xs ml-3"
                >
                  <span><i className="fa fa-sign-out pr-2" /><Translate
                    id="react.default.button.saveAndExit.label"
                    defaultMessage="Save and exit"
                  />
                  </span>
                </button>
              </span>
                :
              <button
                type="button"
                onClick={() => {
                  window.location = '/openboxes/stockMovement/list?direction=OUTBOUND';
                }}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
              >
                <span><i className="fa fa-sign-out pr-2" /> <Translate id="react.default.button.exit.label" defaultMessage="Exit" /> </span>
              </button> }
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  stockMovementId: values.stockMovementId,
                  translate: this.props.translate,
                  reasonCodes: this.props.reasonCodes,
                  onResponse: this.fetchEditPageItems,
                  revertItem: this.revertItem,
                  reviseRequisitionItems: this.reviseRequisitionItems,
                  totalCount: this.state.totalCount,
                  loadMoreRows: this.loadMoreRows,
                  isRowLoaded: this.isRowLoaded,
                  isPaginated: this.props.isPaginated,
                  updateRow: this.updateRow,
                  isFirstPageLoaded: this.state.isFirstPageLoaded,
                  values,
                  showOnly,
                  showOnlyErroredItems,
                  itemFilter,
                }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="submit"
                  onClick={() => this.previousPage(values, invalid)}
                  disabled={showOnly}
                  className="btn btn-outline-primary btn-form btn-xs"
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  disabled={!this.state.hasItemsLoaded || showOnly}
                  onClick={() => {
                    if (!invalid) {
                      this.nextPage(values);
                    }
                  }}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
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

const mapStateToProps = state => ({
  reasonCodes: state.reasonCodes.data,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  isPaginated: state.session.isPaginated,
  pageSize: state.session.pageSize,
  currentLocale: state.session.activeLanguage,
});

export default connect(mapStateToProps, {
  fetchReasonCodes, showSpinner, hideSpinner,
})(EditItemsPage);

EditItemsPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
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
  /** Function fetching reason codes */
  fetchReasonCodes: PropTypes.func.isRequired,
  /** Array of available reason codes */
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  /** Return true if show only */
  showOnly: PropTypes.bool.isRequired,
  pageSize: PropTypes.number.isRequired,
  currentLocale: PropTypes.string.isRequired,
};
