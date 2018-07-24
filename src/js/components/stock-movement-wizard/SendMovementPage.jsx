import React, { Component } from 'react';
import { connect } from 'react-redux';
import { reduxForm, formValueSelector } from 'redux-form';
import PropTypes from 'prop-types';
import _ from 'lodash';

import { validateSendMovement } from './validate';
import { renderFormField } from '../../utils/form-utils';
import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import DateField from '../form-elements/DateField';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

const FIELDS = {
  shipDate: {
    type: DateField,
    label: 'Ship Date',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      required: true,
    },
  },
  shipmentType: {
    type: SelectField,
    label: 'Shipment Type',
    attributes: {
      required: true,
    },
    getDynamicAttr: ({ shipmentTypes }) => ({
      options: shipmentTypes,
    }),
  },
  trackingNumber: {
    type: TextField,
    label: 'Tracking #',
  },
  driver: {
    type: TextField,
    label: 'Driver',
  },
  comment: {
    type: TextField,
    label: 'Comment',
  },
};

class SendMovementPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      shipmentTypes: [],
      stockMovementData: {},
      tableItems: [],
      supplier: false,
      printDeliveryNote: '',
      printPackingList: '',
      printCertOfDonation: '',
    };
    this.props.showSpinner();
  }

  componentDidMount() {
    this.props.showSpinner();
    this.fetchShipmentTypes();
    this.fetchStockMovementData()
      .then((response) => {
        const stockMovementData = response.data.data;
        const { associations } = response.data.data;

        let tableItems;
        let supplier;
        if (!_.isEmpty(stockMovementData) && stockMovementData.pickPage.pickPageItems.length) {
          tableItems = _.reduce(
            stockMovementData.pickPage.pickPageItems,
            (result, item) => _.concat(result, item.picklistItems), [],
          );
          supplier = false;
        } else {
          tableItems = this.props.lineItems;
          supplier = true;
        }
        const printDeliveryNote = _.find(associations.documents, doc => doc.name === 'Delivery Note');
        const printPackingList = _.find(associations.documents, doc => doc.name === 'Download Packing List');
        const printCertOfDonation = _.find(associations.documents, doc => doc.name === 'Download Suitcase Letter');

        this.setState({
          stockMovementData,
          tableItems,
          supplier,
          printDeliveryNote: !_.isEmpty(printDeliveryNote) ? printDeliveryNote.uri : '',
          printPackingList: !_.isEmpty(printPackingList) ? printPackingList.uri : '',
          printCertOfDonation: !_.isEmpty(printCertOfDonation) ? printCertOfDonation.uri : '',
        }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchShipmentTypes() {
    const url = '/openboxes/api/generic/shipmentType';

    return apiClient.get(url)
      .then((response) => {
        const shipmentTypes = _.map(response.data.data, type => (
          { value: type.id, label: _.split(type.name, '|')[0] }
        ));
        this.setState({ shipmentTypes }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchStockMovementData() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}?stepNumber=5`;

    return apiClient.get(url);
  }

  render() {
    const {
      handleSubmit, pristine, previousPage, submitting,
    } = this.props;

    return (
      <div>
        <hr />
        <div className="print-buttons-container">
          <a
            href={this.state.printDeliveryNote}
            className="py-1 mb-1 btn btn-outline-secondary"
            target="_blank"
            rel="noopener noreferrer"
          >
            <span><i className="fa fa-print pr-2" />Print Delivery Note</span>
          </a>
          <a
            href={this.state.printPackingList}
            className="py-1 mb-1 btn btn-outline-secondary"
            target="_blank"
            rel="noopener noreferrer"
          >
            <span><i className="fa fa-print pr-2" />Print Packing List</span>
          </a>
          <a
            href={this.state.printCertOfDonation}
            className="py-1 mb-1 btn btn-outline-secondary"
            target="_blank"
            rel="noopener noreferrer"
          >
            <span><i className="fa fa-print pr-2" />Print Certificate of Donation</span>
          </a>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="row">
            <span className="col-md-2 col-form-label text-right">
            Description
            </span>
            <span className="col-md-4 align-self-center">
              {this.state.stockMovementData.description}
            </span>
          </div>
          <div className="row">
            <span className="col-md-2 col-form-label text-right">
            From
            </span>
            <span className="col-md-4 align-self-center">
              {this.state.stockMovementData.origin ? this.state.stockMovementData.origin.name : ''}
            </span>
          </div>
          <div className="row">
            <span className="col-md-2 col-form-label text-right">
            To
            </span>
            <span className="col-md-4 align-self-center">
              {this.state.stockMovementData.destination ? this.state.stockMovementData.destination.name : ''}
            </span>
          </div>
          <div className="row">
            <span className="col-md-2 col-form-label text-right">
            Stock List
            </span>
            <span className="col-md-4 align-self-center">
              {this.state.stockMovementData.stockList ? this.state.stockMovementData.stockList.name : ''}
            </span>
          </div>
          <div className="row">
            <span className="col-md-2 col-form-label text-right">
            Requested by
            </span>
            <span className="col-md-4 align-self-center">
              {this.state.stockMovementData.requestedBy ? this.state.stockMovementData.requestedBy.name : ''}
            </span>
          </div>
          <div className="row">
            <span className="pb-2 col-md-2 col-form-label text-right">
            Date requested
            </span>
            <span className="col-md-4 align-self-center">
              {this.state.stockMovementData.dateRequested}
            </span>
          </div>
          <div className="row">
            <span className="pb-2 col-md-2 col-form-label text-right">
            Shipment name
            </span>
            <span className="col-md-4 align-self-center">
              {this.state.stockMovementData.name}
            </span>
          </div>
          <hr />
          <div>
            {_.map(FIELDS, (fieldConfig, fieldName) =>
              renderFormField(fieldConfig, fieldName, { shipmentTypes: this.state.shipmentTypes }))}

            <table className="table table-striped text-center border">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Product Name</th>
                  <th>Lot number</th>
                  <th>Expiry Date</th>
                  <th>Quantity Picked</th>
                  {(this.state.supplier) &&
                    <th>Pallet</th>
                  }
                  {(this.state.supplier) &&
                    <th>Box</th>
                  }
                  {!(this.state.supplier) &&
                    <th>Bin</th>
                  }
                  <th>Recipient</th>
                </tr>
              </thead>
              <tbody>
                {
                _.map(
                  this.state.tableItems,
                  (item, index) =>
                    (
                      <tr key={index}>
                        <td>{item.productCode || item.product.productCode}</td>
                        <td>{item['product.name'] || item.product.name}</td>
                        <td>{item.lotNumber}</td>
                        <td>
                          {item.expirationDate}
                        </td>
                        <td>
                          {item.quantityPicked || item.quantity}
                        </td>
                        {(this.state.supplier) &&
                          <td>{item.pallet}</td>
                        }
                        {(this.state.supplier) &&
                          <td>{item.box}</td>
                        }
                        {!(this.state.supplier) &&
                          <td>{item['binLocation.name']}</td>
                        }
                        <td>
                          {item.recipient ? <span className="fa fa-user" /> : null}
                        </td>
                      </tr>
                    ),
                )
              }
              </tbody>
            </table>

            <button type="button" className="btn btn-outline-primary" onClick={previousPage}>
              Previous
            </button>
            <button type="submit" className="btn btn-outline-success float-right" disabled={pristine || submitting}>Send Shipment</button>
          </div>
        </form>
      </div>
    );
  }
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  lineItems: selector(state, 'lineItems'),
  stockMovementId: selector(state, 'requisitionId'),
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validateSendMovement,
})(connect(mapStateToProps, { showSpinner, hideSpinner })(SendMovementPage));

SendMovementPage.propTypes = {
  lineItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  pristine: PropTypes.bool.isRequired,
  submitting: PropTypes.bool.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  stockMovementId: PropTypes.string.isRequired,
};
