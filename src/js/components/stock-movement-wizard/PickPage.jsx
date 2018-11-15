import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';
import AdjustInventoryModal from './modals/AdjustInventoryModal';
import EditPickModal from './modals/EditPickModal';
import { showSpinner, hideSpinner } from '../../actions';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import apiClient from '../../utils/apiClient';
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
        label: 'Code',
        flexWidth: '0.9',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      'product.name': {
        type: LabelField,
        label: 'Product Name',
        flexWidth: '6',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      lotNumber: {
        type: LabelField,
        flexWidth: '1.3',
        label: 'Lot #',
      },
      expirationDate: {
        type: LabelField,
        flexWidth: '0.9',
        label: 'Expiry Date',
      },
      'binLocation.name': {
        type: LabelField,
        flexWidth: '1.2',
        label: 'Bin',
      },
      quantityRequired: {
        type: LabelField,
        label: 'Qty required',
        flexWidth: '0.8',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityPicked: {
        type: LabelField,
        label: 'Qty picked',
        flexWidth: '0.7',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      buttonEditPick: {
        label: 'Edit Pick',
        type: EditPickModal,
        fieldKey: '',
        flexWidth: '0.6',
        attributes: {
          title: 'Edit Pick',
        },
        getDynamicAttr: ({
          fieldValue, selectedValue, subfield, stockMovementId,
          checkForInitialPicksChanges, onResponse,
        }) => ({
          productCode: selectedValue,
          fieldValue,
          subfield,
          stockMovementId,
          checkForInitialPicksChanges,
          btnOpenText: fieldValue.hasChangedPick ? '' : 'Edit',
          btnOpenClassName: fieldValue.hasChangedPick ? ' btn fa fa-check btn-outline-success' : 'btn btn-outline-primary',
          onResponse,
        }),
      },
      buttonAdjustInventory: {
        label: 'Adjust Inventory',
        type: AdjustInventoryModal,
        fieldKey: '',
        flexWidth: '1',
        attributes: {
          title: 'Adjust Inventory',
        },
        getDynamicAttr: ({
          fieldValue, selectedValue, subfield, stockMovementId,
          checkForInitialPicksChanges, onResponse, bins, locationId,
        }) => ({
          product: selectedValue,
          fieldValue,
          subfield,
          stockMovementId,
          checkForInitialPicksChanges,
          btnOpenText: fieldValue.hasAdjustedInventory ? '' : 'Adjust',
          btnOpenClassName: fieldValue.hasAdjustedInventory ? ' btn fa fa-check btn-outline-success' : 'btn btn-outline-primary',
          btnOpenDisabled: true,
          onResponse,
          bins,
          locationId,
        }),
      },
      revert: {
        type: ButtonField,
        label: 'Undo',
        flexWidth: '0.7',
        fieldKey: '',
        buttonLabel: 'Undo',
        getDynamicAttr: ({ fieldValue, revertUserPick, subfield }) => ({
          onClick: fieldValue['requisitionItem.id'] ? () => revertUserPick(fieldValue['requisitionItem.id']) : () => null,
          hidden: subfield || fieldValue.pickStatusCode === 'NOT_PICKED',
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
      printPicksUrl: '',
      values: this.props.initialValues,
    };

    this.revertUserPick = this.revertUserPick.bind(this);
    this.saveNewItems = this.saveNewItems.bind(this);
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
          values: { ...this.state.values, pickPageItems: [] },
        }, () => this.setState({
          values: {
            ...this.state.values,
            pickPageItems: this.checkForInitialPicksChanges(pickPageItems),
          },
        }, () => this.fetchBins()));
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Refetch the data, all not saved changes will be lost.
   * @public
   */
  refresh() {
    confirmAlert({
      title: 'Confirm refresh',
      message: 'Are you sure you want to refresh? Your progress since last save will be lost.',
      buttons: [
        {
          label: 'Yes',
          onClick: () => this.fetchAllData(),
        },
        {
          label: 'No',
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
            item => (suggestion['inventoryItem.id']) === item['inventoryItem.id'] && (item['binLocation.id'] === suggestion['binLocation.id']),
          );
          if (_.isEmpty(pick) || (pick.quantityPicked !== suggestion.quantityPicked)) {
            initialPicks.push({
              ...suggestion,
              initial: true,
            });
          }
        });
        /* eslint-disable-next-line no-param-reassign */
        pickPageItem.picklistItems = _.sortBy(_.concat(pickPageItem.picklistItems, initialPicks), ['inventoryItem.id', 'initial']);
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
        pickPageItems: [],
      },
    }, () => this.setState({
      values: {
        ...this.state.values,
        pickPageItems: this.checkForInitialPicksChanges(pickPageItems),
      },
    }));
  }

  /**
   * Reverts to previous state of picks for requisition item
   * @param {string} itemId
   * @public
   */
  revertUserPick(itemId) {
    this.props.showSpinner();

    const itemsUrl = `/openboxes/api/stockMovementItems/${itemId}`;
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=4`;
    const pickPageItemData = _.find(
      this.state.values.pickPageItems,
      item => item['requisitionItem.id'] === itemId,
    );

    const resetPicksPayload = {
      picklistItems: _.map(pickPageItemData.picklistItems, item => ({
        id: item.id,
        quantityPicked: 0,
      })),
    };

    const initialPicksPayload = {
      picklistItems: _.map(pickPageItemData.suggestedItems, item => ({
        ...item,
        'binLocation.id': item['binLocation.id'] || '',
        reasonCode: '',
      })),
    };

    return apiClient.post(itemsUrl, resetPicksPayload).then(() => {
      apiClient.post(itemsUrl, initialPicksPayload).then(() => {
        apiClient.get(url)
          .then((resp) => {
            const { pickPageItems } = resp.data.data.pickPage;
            this.saveNewItems(pickPageItems);
            this.props.hideSpinner();
          })
          .catch(() => { this.props.hideSpinner(); });
      }).catch(() => { this.props.hideSpinner(); });
    }).catch(() => { this.props.hideSpinner(); });
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
              <a
                href={this.state.printPicksUrl}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
                target="_blank"
                rel="noopener noreferrer"
              >
                <span><i className="fa fa-print pr-2" />Print Picklist</span>
              </a>
              <button
                type="button"
                onClick={() => this.refresh()}
                className="float-right  mb-1 btn btn-outline-secondary align-self-end btn-xs"
              >
                <span><i className="fa fa-refresh pr-2" />Refresh</span>
              </button>
            </span>
            <form onSubmit={handleSubmit} className="print-mt">
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  checkForInitialPicksChanges: this.checkForInitialPicksChanges,
                  stockMovementId: values.stockMovementId,
                  onResponse: this.saveNewItems,
                  revertUserPick: this.revertUserPick,
                  bins: this.state.bins,
                  locationId: this.state.values.origin.id,
                }))}
              <div className="d-print-none">
                <button type="button" className="btn btn-outline-primary btn-form btn-xs" onClick={() => this.props.previousPage(values)}>
                    Previous
                </button>
                <button type="submit" className="btn btn-outline-primary btn-form float-right btn-xs">Next</button>
              </div>
            </form>
          </div>
          )}
      />
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(PickPage);

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
};
