import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import fileDownload from 'js-file-download';
import update from 'immutability-helper';
import Alert from 'react-s-alert';
import { confirmAlert } from 'react-confirm-alert';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../../form-elements/ArrayField';
import LabelField from '../../form-elements/LabelField';
import { renderFormField } from '../../../utils/form-utils';

import EditPickModal from '../modals/EditPickModal';
import { showSpinner, hideSpinner, fetchReasonCodes } from '../../../actions';
import TableRowWithSubfields from '../../form-elements/TableRowWithSubfields';
import apiClient, { parseResponse, flattenRequest } from '../../../utils/apiClient';
import ButtonField from '../../form-elements/ButtonField';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';

const FIELDS = {
  pickPageItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'picklistItems',
    getDynamicRowAttr: ({ rowValues, subfield }) => {
      let className = rowValues.initial ? 'crossed-out ' : '';
      if (!subfield) { className += 'font-weight-bold'; }
      return { className };
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
      'product.name': {
        type: LabelField,
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
        flexWidth: '3.8',
        headerAlign: 'left',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      lotNumber: {
        type: LabelField,
        flexWidth: '1.1',
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
      },
      expirationDate: {
        type: LabelField,
        flexWidth: '0.9',
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
      },
      'binLocation.name': {
        type: LabelField,
        flexWidth: '1.1',
        label: 'react.stockMovement.binLocation.label',
        defaultMessage: 'Bin location',
        getDynamicAttr: ({ hasBinLocationSupport }) => ({
          hide: !hasBinLocationSupport,
        }),
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
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      buttonEditPick: {
        label: 'react.stockMovement.editPick.label',
        defaultMessage: 'Edit pick',
        type: EditPickModal,
        fieldKey: '',
        flexWidth: '0.7',
        attributes: {
          title: 'react.stockMovement.editPick.label',
        },
        getDynamicAttr: ({
          fieldValue, subfield, stockMovementId, updatePickPageItem,
          reasonCodes, hasBinLocationSupport, showOnly,
        }) => ({
          fieldValue: flattenRequest(fieldValue),
          btnOpenDisabled: showOnly,
          subfield,
          stockMovementId,
          btnOpenText: fieldValue && fieldValue.hasChangedPick ? '' : 'react.default.button.edit.label',
          btnOpenDefaultText: fieldValue && fieldValue.hasChangedPick ? '' : 'Edit',
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
          onClick: flattenRequest(fieldValue)['requisitionItem.id'] ? () => revertUserPick(flattenRequest(fieldValue)['requisitionItem.id']) : () => null,
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
      bins: [],
      sorted: false,
      printPicksUrl: '',
      values: { ...this.props.initialValues, pickPageItems: [] },
      totalCount: 0,
    };

    this.revertUserPick = this.revertUserPick.bind(this);
    this.updatePickPageItem = this.updatePickPageItem.bind(this);
    this.fetchAdjustedItems = this.fetchAdjustedItems.bind(this);
    this.sortByBins = this.sortByBins.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
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

  setPickPageItems(response) {
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

    if (!this.props.reasonCodesFetched || forceFetch) {
      this.props.fetchReasonCodes();
    }

    this.fetchPickPageData();
    if (!this.props.isPaginated) {
      this.fetchPickPageItems();
    }
  }

  /**
   * Checks if any changes has been made and adjusts initial pick.
   * @param {object} pickPageItem
   * @public
   */
  checkForInitialPicksChanges(pickPageItem) {
    if (pickPageItem.picklistItems.length) {
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
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=4`;

    return apiClient.get(url)
      .then((resp) => {
        const { totalCount } = resp.data;
        const { associations } = resp.data.data;
        const printPicks = _.find(
          associations.documents,
          doc => doc.documentType === 'PICKLIST' && doc.uri.includes('print'),
        );
        this.setState({
          totalCount,
          printPicksUrl: printPicks ? printPicks.uri : '/',
          sorted: false,
        }, () => this.fetchBins());
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchPickPageItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=4`;
    apiClient.get(url)
      .then((response) => {
        this.setPickPageItems(response);
      });
  }

  fetchItemsAfterImport() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=4`;
    apiClient.get(url)
      .then((response) => {
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

  loadMoreRows({ startIndex, stopIndex }) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${stopIndex - startIndex > 0 ? stopIndex - startIndex : 1}&stepNumber=4`;
    apiClient.get(url)
      .then((response) => {
        this.setPickPageItems(response);
      });
  }

  isRowLoaded({ index }) {
    return !!this.state.values.pickPageItems[index];
  }

  fetchAdjustedItems(adjustedProductCode) {
    apiClient.post(`/openboxes/api/stockMovements/${this.state.values.stockMovementId}/updateAdjustedItems?adjustedProduct=${adjustedProductCode}`)
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
   * Fetches available bin locations from API.
   * @public
   */
  fetchBins() {
    const url = `/openboxes/api/internalLocations?location.id=${this.state.values.origin.id}`;

    return apiClient.get(url)
      .then((response) => {
        const bins = _.map(response.data.data, bin => (
          { value: bin.id, label: bin.name, name: bin.name }
        ));
        this.setState({ bins }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Transition to next stock movement status (PICKED).
   * @public
   */
  transitionToNextStep() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'PICKED' };

    if (this.state.values.statusCode !== 'PICKED' && this.state.values.statusCode !== 'PACKED') {
      return apiClient.post(url, payload);
    }
    return Promise.resolve();
  }

  /**
   * Goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    this.props.showSpinner();
    this.transitionToNextStep()
      .then(() => this.props.nextPage(formValues))
      .catch(() => this.props.hideSpinner());
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
    });
  }

  /**
   * Reverts to previous state of picks for requisition item
   * @param {string} itemId
   * @public
   */
  revertUserPick(itemId) {
    this.props.showSpinner();

    const itemsUrl = `/openboxes/api/stockMovementItems/${itemId}/createPicklist`;

    apiClient.post(itemsUrl)
      .then((resp) => {
        const pickPageItem = resp.data.data;

        this.updatePickPageItem(pickPageItem);
        this.props.hideSpinner();
      })
      .catch(() => { this.props.hideSpinner(); });
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
    const url = `/openboxes/api/stockMovements/exportPickListItems/${stockMovementId}`;

    apiClient.get(url, { responseType: 'blob' })
      .then((response) => {
        fileDownload(response.data, `PickListItems${movementNumber ? `-${movementNumber}` : ''}.csv`, 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

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

    const url = `/openboxes/api/stockMovements/importPickListItems/${stockMovementId}`;

    return apiClient.post(url, formData, config)
      .then(() => {
        this.props.hideSpinner();
        this.fetchItemsAfterImport();
      })
      .catch(() => {
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
        'This button will redo the autopick on all items that have not been previously edited. Are you sure you want to continue?',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => {
            this.fetchAllData(true);
          },
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  render() {
    const { showOnly } = this.props;
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values }) => (
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
                <a
                  href={`${this.state.printPicksUrl}${this.state.sorted ? '?sorted=true' : ''}`}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <span><i className="fa fa-print pr-2" /><Translate id="react.stockMovement.printPicklist.label" defaultMessage="Print picklist" /></span>
                </a>
                <button
                  type="button"
                  onClick={() => this.refresh()}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
                >
                  <span><i className="fa fa-refresh pr-2" /><Translate id="react.default.button.refresh.label" defaultMessage="Reload" /></span>
                </button>
                <button
                  type="button"
                  onClick={() => { window.location = `/openboxes/stockMovement/show/${values.stockMovementId}`; }}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
                >
                  <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
                </button>
                <button
                  type="button"
                  onClick={() => this.sortByBins()}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                >
                  {this.state.sorted && <span><i className="fa fa-sort pr-2" /><Translate id="react.stockMovement.originalOrder.label" defaultMessage="Original order" /></span>}
                  {!this.state.sorted && <span><i className="fa fa-sort pr-2" /><Translate id="react.stockMovement.sortByBins.label" defaultMessage="Sort by bins" /></span>}
                </button>
              </span>
                :
              <button
                type="button"
                onClick={() => { window.location = '/openboxes/stockMovement/list?direction=OUTBOUND'; }}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
              >
                <span><i className="fa fa-sign-out pr-2" /> <Translate id="react.default.button.exit.label" defaultMessage="Exit" /> </span>
              </button> }
            <form onSubmit={handleSubmit} className="print-mt">
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                stockMovementId: values.stockMovementId,
                updatePickPageItem: this.updatePickPageItem,
                fetchAdjustedItems: this.fetchAdjustedItems,
                revertUserPick: this.revertUserPick,
                bins: this.state.bins,
                locationId: this.state.values.origin.id,
                reasonCodes: this.props.reasonCodes,
                translate: this.props.translate,
                hasBinLocationSupport: this.props.hasBinLocationSupport,
                totalCount: this.state.totalCount,
                loadMoreRows: this.loadMoreRows,
                isRowLoaded: this.isRowLoaded,
                isPaginated: this.props.isPaginated,
                showOnly,
              }))}
              <div className="d-print-none">
                <button type="button" disabled={showOnly} className="btn btn-outline-primary btn-form btn-xs" onClick={() => this.props.previousPage(values)}>
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

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  reasonCodesFetched: state.reasonCodes.fetched,
  reasonCodes: state.reasonCodes.data,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  isPaginated: state.session.isPaginated,
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
  /** Indicator if reason codes' data is fetched */
  reasonCodesFetched: PropTypes.bool.isRequired,
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
};
