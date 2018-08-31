import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';

import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';
import AdjustInventoryModal from './modals/AdjustInventoryModal';
import EditPickModal from './modals/EditPickModal';
import { showSpinner, hideSpinner } from '../../actions';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import apiClient from '../../utils/apiClient';

const FIELDS = {
  pickPageItems: {
    type: ArrayField,
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
        flexWidth: '0.7',
        label: 'Lot #',
      },
      expirationDate: {
        type: LabelField,
        flexWidth: '0.9',
        label: 'Expiry Date',
      },
      'binLocation.name': {
        type: LabelField,
        flexWidth: '0.7',
        label: 'Bin',
      },
      quantityRequired: {
        type: LabelField,
        label: 'Qty required',
        flexWidth: '0.9',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityPicked: {
        type: LabelField,
        label: 'Qty picked',
        flexWidth: '0.9',
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
          checkForInitialPicksChanges, onResponse,
        }) => ({
          product: selectedValue,
          fieldValue,
          subfield,
          stockMovementId,
          checkForInitialPicksChanges,
          btnOpenText: fieldValue.hasAdjustedInventory ? '' : 'Adjust',
          btnOpenClassName: fieldValue.hasAdjustedInventory ? ' btn fa fa-check btn-outline-success' : 'btn btn-outline-primary',
          onResponse,
        }),
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
      statusCode: '',
      printPicksUrl: '',
      values: this.props.initialValues,
    };

    this.saveNewItems = this.saveNewItems.bind(this);
    this.props.showSpinner();
  }

  componentDidMount() {
    this.fetchLineItems()
      .then((resp) => {
        const { associations, statusCode } = resp.data.data;
        const { pickPageItems } = resp.data.data.pickPage;

        const printPicks = _.find(associations.documents, doc => doc.name === 'Print Picklist');
        this.setState({
          printPicksUrl: printPicks.uri,
          statusCode,
          values: { ...this.state.values, pickPageItems: [] },
        }, () => this.setState({
          values: {
            ...this.state.values,
            pickPageItems: this.checkForInitialPicksChanges(pickPageItems),
          },
        }));

        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
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
            item => suggestion['inventoryItem.id'] === item['inventoryItem.id'],
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
   * Transition to next stock movement status (PICKED).
   * @public
   */
  transitionToStep5() {
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
    if (this.state.statusCode === 'PICKING') {
      this.transitionToStep5()
        .then(() => this.props.onSubmit(formValues))
        .catch(() => this.props.hideSpinner());
    } else {
      this.props.onSubmit(formValues);
    }
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
        pickPageItems,
      },
    }));
  }

  render() {
    return (
      <div className="d-flex flex-column">
        <a
          href={this.state.printPicksUrl}
          className="float-right py-1 mb-1 btn btn-outline-secondary align-self-end"
          target="_blank"
          rel="noopener noreferrer"
        >
          <span><i className="fa fa-print pr-2" />Print Picklist</span>
        </a>
        <Form
          onSubmit={values => this.nextPage(values)}
          mutators={{ ...arrayMutators }}
          initialValues={this.state.values}
          render={({ handleSubmit, values }) => (
            <form onSubmit={handleSubmit} className="print-mt">
              {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                checkForInitialPicksChanges: this.checkForInitialPicksChanges,
                stockMovementId: values.stockMovementId,
                onResponse: this.saveNewItems,
              }))}
              <div className="d-print-none">
                <button type="button" className="btn btn-outline-primary btn-form" onClick={() => this.props.previousPage(values)}>
                  Previous
                </button>
                <button type="submit" className="btn btn-outline-primary btn-form float-right">Next</button>
              </div>
            </form>
          )}
        />
      </div>
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
