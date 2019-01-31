import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate, Translate } from 'react-localize-redux';
import fileDownload from 'js-file-download';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';
import AdjustInventoryModal from './modals/AdjustInventoryModal';
import EditPickModal from './modals/EditPickModal';
import { showSpinner, hideSpinner } from '../../actions';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import ButtonField from '../form-elements/ButtonField';

const FIELDS = {
  pickPageItems: {
    type: ArrayField,
    virtualized: true,
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
        label: 'stockMovement.code.label',
        flexWidth: '0.9',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      'product.name': {
        type: LabelField,
        label: 'stockMovement.productName.label',
        flexWidth: '4.7',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      lotNumber: {
        type: LabelField,
        flexWidth: '1.3',
        label: 'stockMovement.lot.label',
      },
      expirationDate: {
        type: LabelField,
        flexWidth: '0.9',
        label: 'stockMovement.expiry.label',
      },
      'binLocation.name': {
        type: LabelField,
        flexWidth: '1.2',
        label: 'stockMovement.binLocation.label',
      },
      quantityRequired: {
        type: LabelField,
        label: 'stockMovement.quantityRequired.label',
        flexWidth: '0.8',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityPicked: {
        type: LabelField,
        label: 'stockMovement.quantityPicked.label',
        flexWidth: '0.7',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      buttonEditPick: {
        label: 'stockMovement.editPick.label',
        type: EditPickModal,
        fieldKey: '',
        flexWidth: '0.6',
        attributes: {
          title: 'stockMovement.editPick.label',
        },
        getDynamicAttr: ({
          fieldValue, subfield, stockMovementId, onResponse,
        }) => ({
          fieldValue: flattenRequest(fieldValue),
          subfield,
          stockMovementId,
          btnOpenText: fieldValue.hasChangedPick ? '' : 'default.button.edit.label',
          btnOpenClassName: fieldValue.hasChangedPick ? ' btn fa fa-check btn-outline-success' : 'btn btn-outline-primary',
          onResponse,
        }),
      },
      buttonAdjustInventory: {
        label: 'stockMovement.adjustInventory.label',
        type: AdjustInventoryModal,
        fieldKey: '',
        flexWidth: '1.3',
        attributes: {
          title: 'stockMovement.adjustInventory.label',
        },
        getDynamicAttr: ({
          fieldValue, subfield, stockMovementId, onResponse, bins, locationId,
        }) => ({
          fieldValue: flattenRequest(fieldValue),
          subfield,
          stockMovementId,
          btnOpenText: fieldValue.hasAdjustedInventory ? '' : 'stockMovement.adjust.label',
          btnOpenClassName: fieldValue.hasAdjustedInventory ? ' btn fa fa-check btn-outline-success' : 'btn btn-outline-primary',
          onResponse,
          bins,
          locationId,
        }),
      },
      revert: {
        type: ButtonField,
        label: 'default.button.undo.label',
        flexWidth: '0.7',
        fieldKey: '',
        buttonLabel: 'default.button.undo.label',
        getDynamicAttr: ({ fieldValue, revertUserPick, subfield }) => ({
          onClick: flattenRequest(fieldValue)['requisitionItem.id'] ? () => revertUserPick(flattenRequest(fieldValue)['requisitionItem.id']) : () => null,
          hidden: subfield,
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
      values: this.props.initialValues,
    };

    this.revertUserPick = this.revertUserPick.bind(this);
    this.saveNewItems = this.saveNewItems.bind(this);
    this.sortByBins = this.sortByBins.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
    this.props.showSpinner();
  }

  componentDidMount() {
    this.fetchAllData();
  }

  /**
   * Fetches all required data.
   * @public
   */
  fetchAllData() {
    this.props.showSpinner();
    this.fetchLineItems()
      .then((resp) => {
        const { associations } = resp.data.data;
        const { pickPageItems } = resp.data.data.pickPage;

        const printPicks = _.find(
          associations.documents,
          doc => doc.documentType === 'PICKLIST' && doc.uri.includes('print'),
        );
        this.setState({
          printPicksUrl: printPicks ? printPicks.uri : '/',
          values: {
            ...this.state.values,
            pickPageItems: this.checkForInitialPicksChanges(parseResponse(pickPageItems)),
          },
          sorted: false,
        }, () => this.fetchBins());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Refetch the data, all not saved changes will be lost.
   * @public
   */
  refresh() {
    confirmAlert({
      title: this.props.translate('message.confirmRefresh.label '),
      message: this.props.translate('confirmRefresh.message'),
      buttons: [
        {
          label: this.props.translate('default.yes.label'),
          onClick: () => this.fetchAllData(),
        },
        {
          label: this.props.translate('default.no.label'),
        },
      ],
    });
  }

  /**
   * Checks if any changes has been made and adjusts initial pick.
   * @param {object} pickPageItems
   * @public
   */
  checkForInitialPicksChanges(pickPageItems) {
    _.forEach(pickPageItems, (pickPageItem) => {
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
        /* eslint-disable-next-line no-param-reassign */
        pickPageItem.picklistItems = _.sortBy(_.concat(pickPageItem.picklistItems, initialPicks), ['binLocation.name', 'initial']);
      }
    });
    return pickPageItems;
  }

  /**
   * Fetches 4th step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=4`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
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

    return apiClient.post(url, payload);
  }

  /**
   * Goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    this.props.showSpinner();
    this.transitionToNextStep()
      .then(() => this.props.onSubmit(formValues))
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Saves changes made in edit pick or adjust inventory modals and updates data.
   * @param {object} pickPageItems
   * @public
   */
  saveNewItems(pickPageItems) {
    this.setState({
      values: {
        ...this.state.values,
        pickPageItems: this.checkForInitialPicksChanges(parseResponse(pickPageItems)),
      },
      sorted: false,
    });
  }

  /**
   * Reverts to previous state of picks for requisition item
   * @param {string} itemId
   * @public
   */
  revertUserPick(itemId) {
    this.props.showSpinner();

    const itemsUrl = `/openboxes/api/stockMovementItems/${itemId}`;
    const pickPageItemData = _.find(
      flattenRequest(this.state.values.pickPageItems),
      item => item['requisitionItem.id'] === itemId,
    );

    const resetPicksPayload = {
      picklistItems: _.map(pickPageItemData.picklistItems, item => ({
        id: item.id,
        quantityPicked: '',
      })),
    };

    if (resetPicksPayload.picklistItems.length) {
      apiClient.post(itemsUrl, resetPicksPayload).then(() => {
        this.sendInitialPicks(itemsUrl, pickPageItemData);
      }).catch(() => { this.props.hideSpinner(); });
    } else {
      this.sendInitialPicks(itemsUrl, pickPageItemData);
    }
  }

  sendInitialPicks(itemsUrl, pickPageItemData) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=4`;
    const initialPicksPayload = {
      picklistItems: _.map(pickPageItemData.suggestedItems, item => ({
        ...item,
        'binLocation.id': item['binLocation.id'] || '',
        reasonCode: '',
      })),
    };

    apiClient.post(itemsUrl, initialPicksPayload).then(() => {
      apiClient.get(url)
        .then((resp) => {
          const { pickPageItems } = resp.data.data.pickPage;
          this.saveNewItems(pickPageItems);
          this.props.hideSpinner();
        })
        .catch(() => { this.props.hideSpinner(); });
    }).catch(() => { this.props.hideSpinner(); });
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
        this.fetchAllData();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  render() {
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values }) => (
          <div className="d-flex flex-column">
            <span>
              <label
                htmlFor="csvInput"
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-download pr-2" /><Translate id="default.button.importTemplate.label" /></span>
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
                <span><i className="fa fa-upload pr-2" /><Translate id="default.button.exportTemplate.label" /></span>
              </button>
              <a
                href={`${this.state.printPicksUrl}${this.state.sorted ? '?sorted=true' : ''}`}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
                target="_blank"
                rel="noopener noreferrer"
              >
                <span><i className="fa fa-print pr-2" /><Translate id="stockMovement.printPicklist.label" /></span>
              </a>
              <button
                type="button"
                onClick={() => this.refresh()}
                className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs ml-1"
              >
                <span><i className="fa fa-refresh pr-2" /><Translate id="default.button.refresh.label" /></span>
              </button>
              <button
                type="button"
                onClick={() => this.sortByBins()}
                className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
              >
                {this.state.sorted && <Translate id="stockMovement.originalOrder.label" />}
                {!this.state.sorted && <Translate id="stockMovement.sortByBins.label" />}
              </button>
            </span>
            <form onSubmit={handleSubmit} className="print-mt">
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                stockMovementId: values.stockMovementId,
                onResponse: this.saveNewItems,
                revertUserPick: this.revertUserPick,
                bins: this.state.bins,
                locationId: this.state.values.origin.id,
              }))}
              <div className="d-print-none">
                <button type="button" className="btn btn-outline-primary btn-form btn-xs" onClick={() => this.props.previousPage(values)}>
                  <Translate id="default.button.previous.label" />
                </button>
                <button type="submit" className="btn btn-outline-primary btn-form float-right btn-xs">
                  <Translate id="default.button.next.label" />
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
  translate: getTranslate(state.localize),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(PickPage);

PickPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  onSubmit: PropTypes.func.isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
