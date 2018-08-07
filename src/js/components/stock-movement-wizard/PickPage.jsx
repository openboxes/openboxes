import React, { Component } from 'react';
import { reduxForm, formValueSelector, change } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import _ from 'lodash';

import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';
import ValueSelectorField from '../form-elements/ValueSelectorField';
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
        flexWidth: '130px',
        getDynamicAttr: ({ subfield }) => ({
          className: subfield ? 'text-center' : 'text-left ml-1',
        }),
      },
      'product.name': {
        type: LabelField,
        label: 'Product Name',
        flexWidth: '150px',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      lotNumber: {
        type: LabelField,
        flexWidth: '60px',
        label: 'Lot #',
      },
      expirationDate: {
        type: LabelField,
        flexWidth: '90px',
        label: 'Expiry Date',
      },
      'binLocation.name': {
        type: LabelField,
        flexWidth: '60px',
        label: 'Bin',
      },
      quantityRequired: {
        type: LabelField,
        label: 'Qty required',
        flexWidth: '120px',
      },
      quantityPicked: {
        type: LabelField,
        label: 'Qty picked',
        flexWidth: '120px',
      },
      recipient: {
        type: ValueSelectorField,
        flexWidth: '120px',
        label: 'Includes recipient',
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `pickPage[${rowIndex}].recipient`,
        }),
        component: LabelField,
        componentConfig: {
          getDynamicAttr: ({ selectedValue }) => ({
            className: selectedValue ? 'fa fa-user' : '',
          }),
        },
      },
      buttonEditPick: {
        label: 'Edit Pick',
        type: EditPickModal,
        fieldKey: '',
        flexWidth: '110px',
        attributes: {
          title: 'Edit Pick',
        },
        getDynamicAttr: ({
          fieldValue, selectedValue, subfield, stockMovementId, checkForInitialPicksChanges,
        }) => ({
          productCode: selectedValue,
          fieldValue,
          subfield,
          stockMovementId,
          checkForInitialPicksChanges,
          btnOpenText: fieldValue.hasChangedPick ? '' : 'Edit',
          btnOpenClassName: fieldValue.hasChangedPick ? ' btn fa fa-check btn-outline-success' : 'btn btn-outline-primary',
        }),
      },
      buttonAdjustInventory: {
        label: 'Adjust Inventory',
        type: AdjustInventoryModal,
        fieldKey: '',
        flexWidth: '130px',
        attributes: {
          title: 'Adjust Inventory',
        },
        getDynamicAttr: ({
          fieldValue, selectedValue, subfield, stockMovementId, checkForInitialPicksChanges,
        }) => ({
          product: selectedValue,
          fieldValue,
          subfield,
          stockMovementId,
          checkForInitialPicksChanges,
          btnOpenText: fieldValue.hasAdjustedInventory ? '' : 'Adjust',
          btnOpenClassName: fieldValue.hasAdjustedInventory ? ' btn fa fa-check btn-outline-success' : 'btn btn-outline-primary',
        }),
      },
    },
  },
};

/* eslint class-methods-use-this: ["error",{ "exceptMethods": ["checkForInitialPicksChanges"] }] */
class PickPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      statusCode: '',
      printPicksUrl: '',
    };

    this.props.showSpinner();
  }

  componentDidMount() {
    this.fetchLineItems()
      .then((resp) => {
        const { associations, statusCode } = resp.data.data;
        const { pickPageItems } = resp.data.data.pickPage;
        this.props.change('stock-movement-wizard', 'pickPageItems', []);
        this.props.change('stock-movement-wizard', 'pickPageItems', this.checkForInitialPicksChanges(pickPageItems));

        const printPicks = _.find(associations.documents, doc => doc.name === 'Print Picklist');
        this.setState({
          printPicksUrl: printPicks.uri,
          statusCode,
        });

        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

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

  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}?stepNumber=4`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  transitionToStep5() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}/status`;
    const payload = { status: 'PICKED' };

    return apiClient.post(url, payload);
  }

  nextPage() {
    this.props.showSpinner();
    if (this.state.statusCode === 'PICKING') {
      this.transitionToStep5()
        .then(() => this.props.onSubmit())
        .catch(() => this.props.hideSpinner());
    } else {
      this.props.onSubmit();
    }
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
        <form onSubmit={this.props.handleSubmit(() => this.nextPage())} className="print-mt">
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
            checkForInitialPicksChanges: this.checkForInitialPicksChanges,
            stockMovementId: this.props.stockMovementId,
          }))}
          <div className="d-print-none">
            <button type="button" className="btn btn-outline-primary btn-form" onClick={this.props.previousPage}>
              Previous
            </button>
            <button type="submit" className="btn btn-outline-primary btn-form float-right">Next</button>
          </div>
        </form>
      </div>
    );
  }
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  stockMovementId: selector(state, 'requisitionId'),
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
})(connect(mapStateToProps, { change, showSpinner, hideSpinner })(PickPage));

PickPage.propTypes = {
  change: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  stockMovementId: PropTypes.string.isRequired,
  handleSubmit: PropTypes.func.isRequired,
};
