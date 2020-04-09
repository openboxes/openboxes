import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import update from 'immutability-helper';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import { renderFormField } from '../../../utils/form-utils';
import LabelField from '../../form-elements/LabelField';
import SelectField from '../../form-elements/SelectField';
import SubstitutionsModal from '../modals/SubstitutionsModal';
import apiClient from '../../../utils/apiClient';
import TableRowWithSubfields from '../../form-elements/TableRowWithSubfields';
import { showSpinner, hideSpinner, fetchReasonCodes } from '../../../actions';
import ButtonField from '../../form-elements/ButtonField';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';

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
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    rowComponent: TableRowWithSubfields,
    getDynamicRowAttr: ({ rowValues, subfield }) => {
      let className = rowValues.statusCode === 'SUBSTITUTED' ? 'crossed-out ' : '';
      if (!subfield) { className += 'font-weight-bold'; }
      return { className };
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
      },
      productName: {
        type: LabelField,
        headerAlign: 'left',
        flexWidth: '3.5',
        label: 'react.stockMovement.productName.label',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      quantityRequested: {
        type: LabelField,
        label: 'react.stockMovement.quantityRequested.label',
        defaultMessage: 'Qty requested',
        flexWidth: '1.1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.quantityAvailable.label',
        defaultMessage: 'Qty available',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue }) => {
          let className = '';
          if (fieldValue && (!fieldValue.quantityAvailable ||
            fieldValue.quantityAvailable < fieldValue.quantityRequested)) {
            className = 'text-danger';
          }
          return {
            className,
          };
        },
        attributes: {
          formatValue: value => (value.quantityAvailable ? (value.quantityAvailable.toLocaleString('en-US')) : value.quantityAvailable),
        },
      },
      quantityConsumed: {
        type: LabelField,
        label: 'react.stockMovement.monthlyQuantity.label',
        defaultMessage: 'Monthly stocklist qty',
        flexWidth: '1.5',
        getDynamicAttr: ({ hasStockList, translate, subfield }) => ({
          formatValue: (value) => {
            if (value && value !== '0') {
              return value.toLocaleString('en-US');
            } else if (hasStockList && !subfield) {
              return translate('react.stockMovement.replenishmentPeriodNotFound.label', 'Replenishment period not found');
            } else if (subfield) {
              return '';
            }

            return '0';
          },
          showValueTooltip: true,
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
        }) => ({
          disabled: !fieldValue || subfield,
          options: reasonCodes,
          showValueTooltip: true,
          onBlur: () => updateRow(values, rowIndex),
        }),
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
          btnOpenDisabled: showOnly,
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
    if (!_.isEmpty(item.quantityRevised) && (item.quantityRevised > item.quantityAvailable)) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.higherQty.label' };
    }
    if (!_.isEmpty(item.quantityRevised) && (item.quantityRevised < 0)) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.negativeQty.label' };
    }
  });
  return errors;
}

function validate(values) {
  const errors = validateForSave(values);

  _.forEach(values.editPageItems, (item, key) => {
    if (_.isNil(item.quantityRevised) && (item.quantityRequested > item.quantityAvailable) && (item.statusCode !== 'SUBSTITUTED')) {
      errors.editPageItems[key] = { quantityRevised: 'react.stockMovement.errors.lowerQty.label' };
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
    };

    this.revertItem = this.revertItem.bind(this);
    this.fetchEditPageItems = this.fetchEditPageItems.bind(this);
    this.reviseRequisitionItems = this.reviseRequisitionItems.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateRow = this.updateRow.bind(this);
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
  }

  setEditPageItems(response) {
    this.props.showSpinner();
    const { data } = response.data;

    const editPageItems = _.map(
      data,
      val => ({
        ...val,
        disabled: true,
        quantityAvailable: val.quantityAvailable > 0 ? val.quantityAvailable : 0,
        product: {
          ...val.product,
          label: `${val.productCode} ${val.productName}`,
        },
        substitutionItems: _.map(val.substitutionItems, sub => ({
          ...sub,
          requisitionItemId: val.requisitionItemId,
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
    }, () => this.props.hideSpinner());
  }

  dataFetched = false;

  /**
   * Fetches all required data.
   * @param {boolean} forceFetch
   * @public
   */
  fetchAllData(forceFetch) {
    this.props.showSpinner();

    if (!this.props.reasonCodesFetched || forceFetch) {
      this.props.fetchReasonCodes();
    }

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
        this.setEditPageItems(response);
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
              quantityAvailable: item.quantityAvailable || 0,
              substitutionItems: _.map(item.substitutionItems, sub => ({
                ...sub,
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

  loadMoreRows({ startIndex, stopIndex }) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${stopIndex - startIndex > 0 ? stopIndex - startIndex : 1}&stepNumber=3`;
    apiClient.get(url)
      .then((response) => {
        this.setEditPageItems(response);
      });
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
        reasonCode: item.reasonCode,
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
      let errorMessage = `${this.props.translate('react.stockMovement.errors.errorInLine.label', 'Error occurred in line')}:</br>`;
      errorMessage += _.reduce(
        errors,
        (message, value, key) => (
          `${message}${value ? `${key + 1} - ${_.map(value, val => this.props.translate(`${val}`))}</br>` : ''}`
        ),
        '',
      );

      Alert.error(errorMessage);

      this.props.hideSpinner();
      return null;
    }

    return this.reviseRequisitionItems(formValues)
      .then((resp) => {
        const editPageItems = _.get(resp, 'data.data');
        if (editPageItems && editPageItems.length) {
          this.setState({
            revisedItems: [...this.state.revisedItems, ...editPageItems],
          });
        }
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
          onClick: () => {
            this.setState({
              revisedItems: [],
            });
            this.fetchAllData(true);
          },
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
          .catch(() => this.props.hideSpinner());
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
    const revisedItemIndex = _.findIndex(this.state.values.revisedItems, item =>
      item.requisitionItemId === editPageItem.requisitionItemId);

    this.setState({
      values: {
        ...values,
        editPageItems: update(values.editPageItems, {
          [editPageItemIndex]: {
            $set: {
              ...editPageItem,
              quantityAvailable: editPageItem.quantityAvailable || 0,
              substitutionItems: _.map(editPageItem.substitutionItems, sub => ({
                ...sub,
                requisitionItemId: editPageItem.requisitionItemId,
              })),
            },
          },
        }),
      },
      revisedItems: update(this.state.revisedItems, { $splice: [[revisedItemIndex, 1]] }),
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

    return apiClient.post(revertItemsUrl)
      .then((response) => {
        const editPageItem = response.data.data;
        this.updateEditPageItem(values, editPageItem);
        this.props.hideSpinner();
      })
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
    const { showOnly } = this.props;
    return (
      <Form
        onSubmit={() => {}}
        validate={validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            { !showOnly ?
              <span>
                <button
                  type="button"
                  onClick={() => this.refresh()}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
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
                  className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
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
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
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
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                stockMovementId: values.stockMovementId,
                hasStockList: !!_.get(values.stocklist, 'id'),
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
                values,
                showOnly,
              }))}
              <div>
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
  reasonCodesFetched: state.reasonCodes.fetched,
  reasonCodes: state.reasonCodes.data,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  isPaginated: state.session.isPaginated,
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
  /** Indicator if reason codes' data is fetched */
  reasonCodesFetched: PropTypes.bool.isRequired,
  /** Array of available reason codes */
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  translate: PropTypes.func.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
  /** Return true if show only */
  showOnly: PropTypes.bool.isRequired,
};
