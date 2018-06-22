import React from 'react';
import { connect } from 'react-redux';
import { reduxForm, formValueSelector } from 'redux-form';
import PropTypes from 'prop-types';
import _ from 'lodash';

import { validateSendMovement } from './validate';
import { renderFormField } from '../../utils/form-utils';
import TextField from '../form-elements/TextField';
import DateField from '../form-elements/DateField';

const FIELDS = {
  shipDate: {
    type: DateField,
    label: 'Ship Date',
    attributes: {
      dateFormat: 'YYYY/MM/DD',
      required: true,
    },
  },
  shipmentType: {
    type: TextField,
    label: 'Shipment Type',
    attributes: {
      required: true,
    },
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

const SendMovementPage = (props) => {
  const {
    handleSubmit, pristine, previousPage, submitting, pickPage, lineItems,
    description, origin, destination, stockList, requestedBy, dateRequested, movementNumber,
  } = props;

  const tableItems =
    pickPage.length ? _.filter(pickPage, pick => !!pick.lot && !pick.crossedOut) : lineItems;

  return (
    <div>
      <hr />
      <div className="print-buttons-container">
        <button type="button" className="py-1 mb-1 btn btn-outline-secondary d-print-none">
          <span><i className="fa fa-print pr-2" />Print Delivery Note</span>
        </button>
        <button type="button" className="py-1 mb-1 btn btn-outline-secondary d-print-none">
          <span><i className="fa fa-print pr-2" />Print Packing List</span>
        </button>
        <button type="button" className="py-1 mb-1 btn btn-outline-secondary d-print-none">
          <span><i className="fa fa-print pr-2" />Print Certificate of Donation</span>
        </button>
        <button type="button" className="py-1 mb-1 btn btn-outline-secondary d-print-none">
          <span><i className="fa fa-upload pr-2" />Upload documents</span>
        </button>
      </div>
      <form onSubmit={handleSubmit}>
        <div className="row">
          <span className="col-md-2 col-form-label text-right">
            Description
          </span>
          <span className="col-md-4 align-self-center">
            {description}
          </span>
        </div>
        <div className="row">
          <span className="col-md-2 col-form-label text-right">
            From
          </span>
          <span className="col-md-4 align-self-center">
            {origin.name}
          </span>
        </div>
        <div className="row">
          <span className="col-md-2 col-form-label text-right">
            To
          </span>
          <span className="col-md-4 align-self-center">
            {destination.name}
          </span>
        </div>
        <div className="row">
          <span className="col-md-2 col-form-label text-right">
            Stock List
          </span>
          <span className="col-md-4 align-self-center">
            {stockList}
          </span>
        </div>
        <div className="row">
          <span className="col-md-2 col-form-label text-right">
            Requested by
          </span>
          <span className="col-md-4 align-self-center">
            {requestedBy}
          </span>
        </div>
        <div className="row">
          <span className="pb-2 col-md-2 col-form-label text-right">
            Date requested
          </span>
          <span className="col-md-4 align-self-center">
            {dateRequested}
          </span>
        </div>
        <div className="row">
          <span className="pb-2 col-md-2 col-form-label text-right">
            Shipment name
          </span>
          <span className="col-md-4 align-self-center">
            {`"${origin.name}.${destination.name}.${dateRequested}.${stockList}.${movementNumber}.${description}"`}
          </span>
        </div>
        <hr />
        <div>
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}

          <table className="table table-striped text-center border">
            <thead>
              <tr>
                <th>Product</th>
                <th>Serial / Lot number</th>
                <th>Expiry Date</th>
                <th>Quantity</th>
                <th>Bin</th>
                <th>Recipient</th>
              </tr>
            </thead>
            <tbody>
              {
                _.map(
                  tableItems,
                  (item, index) =>
                  (
                    <tr key={index}>
                      <td>{item.product.name}</td>
                      <td>{item.lot}</td>
                      <td>
                        {item.expiryDate || item.expiry}
                      </td>
                      <td>
                        {item.qtyPicked || item.quantity}
                      </td>
                      <td>{item.bin}</td>
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
};

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  description: selector(state, 'description'),
  origin: selector(state, 'origin'),
  destination: selector(state, 'destination'),
  stockList: selector(state, 'stockList'),
  requestedBy: selector(state, 'requestedBy'),
  dateRequested: selector(state, 'dateRequested'),
  pickPage: selector(state, 'pickPage'),
  lineItems: selector(state, 'lineItems'),
  movementNumber: selector(state, 'movementNumber'),
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validateSendMovement,
})(connect(mapStateToProps, {})(SendMovementPage));

SendMovementPage.propTypes = {
  description: PropTypes.string.isRequired,
  origin: PropTypes.shape({
    name: PropTypes.string.isRequired,
  }).isRequired,
  destination: PropTypes.shape({
    name: PropTypes.string.isRequired,
  }).isRequired,
  stockList: PropTypes.string,
  requestedBy: PropTypes.string.isRequired,
  dateRequested: PropTypes.string.isRequired,
  movementNumber: PropTypes.string.isRequired,
  pickPage: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  lineItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  pristine: PropTypes.bool.isRequired,
  submitting: PropTypes.bool.isRequired,
};

SendMovementPage.defaultProps = {
  stockList: 'New Stock List',
};
