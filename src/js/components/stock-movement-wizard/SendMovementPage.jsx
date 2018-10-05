import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';
import Alert from 'react-s-alert';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';

import { renderFormField } from '../../utils/form-utils';
import { showSpinner, hideSpinner } from '../../actions';
import apiClient from '../../utils/apiClient';
import DateField from '../form-elements/DateField';
import DocumentButton from '../DocumentButton';
import SelectField from '../form-elements/SelectField';
import TextField from '../form-elements/TextField';

const FIELDS = {
  dateShipped: {
    type: DateField,
    label: 'Ship Date',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      required: true,
    },
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
  shipmentType: {
    type: SelectField,
    label: 'Shipment Type',
    attributes: {
      required: true,
      showValueTooltip: true,
    },
    getDynamicAttr: ({ shipmentTypes, issued }) => ({
      options: shipmentTypes,
      disabled: issued,
    }),
  },
  trackingNumber: {
    type: TextField,
    label: 'Tracking #',
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
  driverName: {
    type: TextField,
    label: 'Driver',
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
  comments: {
    type: TextField,
    label: 'Comment',
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
};

function validate(values) {
  const errors = {};

  if (!values.dateShipped) {
    errors.dateShipped = 'This field is required';
  }
  if (!values.shipmentType) {
    errors.shipmentType = 'This field is required';
  }

  return errors;
}

/**
 * The last step of stock movement where user can see the whole movement,
 * print documents, upload documents, add additional information and send it.
 */
class SendMovementPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      shipmentTypes: [],
      stockMovementData: {},
      tableItems: [],
      supplier: false,
      documents: [],
      files: [],
      values: this.props.initialValues,
    };
    this.props.showSpinner();
    this.onDrop = this.onDrop.bind(this);
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
        if (!_.isEmpty(stockMovementData) && stockMovementData.packPage
          && stockMovementData.packPage.packPageItems.length) {
          tableItems = stockMovementData.packPage.packPageItems;
          supplier = false;
        } else {
          tableItems = stockMovementData.lineItems;
          supplier = true;
        }
        const documents = _.filter(associations.documents, doc => doc.stepNumber === 5);

        this.setState({
          stockMovementData,
          tableItems,
          supplier,
          documents,
          values: {
            ...this.state.values,
            dateShipped: stockMovementData.dateShipped,
            shipmentType: _.get(stockMovementData, 'shipmentType.id'),
            trackingNumber: stockMovementData.trackingNumber,
            driverName: stockMovementData.driverName,
            comments: stockMovementData.comments,
          },
        }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Updates files' array after dropping them to dropzone area.
   * @param {object} files
   * @public
   */
  onDrop(files) {
    this.setState({
      files,
    });
  }

  /**
   * Fetches available shipment types from API.
   * @public
   */
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

  /**
   * Fetches 5th step data from current stock movement.
   * @public
   */
  fetchStockMovementData() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=6`;

    return apiClient.get(url);
  }

  /**
   * Sends files uploaded by user to backend.
   * @param {object} file
   * @public
   */
  sendFile(file) {
    const url = `/openboxes/stockMovement/uploadDocument/${this.state.values.stockMovementId}`;

    const data = new FormData();
    data.append('fileContents', file);

    return apiClient.post(url, data);
  }

  /**
   * Sends data with shipment details.
   * @param {object} values
   * @public
   */
  sendShipment(values) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
    const payload = {
      id: this.state.values.stockMovementId,
      dateShipped: values.dateShipped,
      'shipmentType.id': values.shipmentType,
      trackingNumber: values.trackingNumber,
      driverName: values.driverName,
      comments: values.comments,
    };

    return apiClient.post(url, payload);
  }

  /**
   * Updates stock movement status to ISSUED with post method.
   * @public
   */
  stateTransitionToIssued() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'ISSUED' };

    return apiClient.post(url, payload);
  }

  /**
   * Uploads files and sends the whole stock movement.
   * @param {object} values
   * @public
   */
  submitStockMovement(values) {
    this.props.showSpinner();
    if (this.state.files.length) {
      _.forEach(this.state.files, (file) => {
        this.sendFile(file)
          .then(() => Alert.success('File uploaded successfuly!'))
          .catch(() => Alert.error('Error occured during file upload!'));
      });
    }
    this.sendShipment(values)
      .then(() => {
        this.stateTransitionToIssued()
          .then(() => {
            // redirect to requisition list
            window.location = `/openboxes/stockMovement/show/${this.state.values.stockMovementId}`;
          })
          .catch(() => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const { previousPage } = this.props;

    return (
      <div>
        <hr />
        <Form
          onSubmit={values => this.submitStockMovement(values)}
          validate={validate}
          mutators={{ ...arrayMutators }}
          initialValues={this.state.values}
          render={({ handleSubmit, values, invalid }) => (
            <form onSubmit={handleSubmit}>
              <div className="d-flex">
                <div id="stockMovementInfo" style={{ flexGrow: 2 }}>
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
                </div>
                <div className="print-buttons-container col-md-3 flex-grow-1">
                  {this.state.documents.length && _.map(this.state.documents, (document, idx) => (
                    <DocumentButton
                      link={document.uri}
                      buttonTitle={document.name}
                      {...document}
                      key={idx}
                    />
                  ))}
                  <div className="dropzone btn btn-outline-secondary">
                    <Dropzone
                      disabled={values.statusCode === 'ISSUED'}
                      onDrop={this.onDrop}
                      multiple
                    >
                      <span><i className="fa fa-upload pr-2" />Upload Documents</span>
                      {_.map(this.state.files, file => (
                        <div key={file.name} className="chosen-file"><span>{file.name}</span></div>
                      ))}
                    </Dropzone>
                  </div>
                </div>
              </div>
              <hr />
              <div>
                {_.map(FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    shipmentTypes: this.state.shipmentTypes,
                    issued: values.statusCode === 'ISSUED',
                  }))}
                <table className="table table-striped text-center border">
                  <thead>
                    <tr>
                      <th>Pallet</th>
                      <th>Box</th>
                      <th>Code</th>
                      <th>Product Name</th>
                      <th>Lot number</th>
                      <th>Expiry Date</th>
                      <th style={{ width: '150px' }}>Quantity Picked</th>
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
                            <td>{item.palletName}</td>
                            <td>{item.boxName}</td>
                            <td>{item.productCode || item.product.productCode}</td>
                            <td className="text-left">
                              <span className="ml-4">{item.productName || item.product.name}</span>
                            </td>
                            <td>{item.lotNumber}</td>
                            <td>
                              {item.expirationDate}
                            </td>
                            <td style={{ width: '150px' }}>
                              {(item.quantityShipped ? item.quantityShipped.toLocaleString('en-US') : item.quantityShipped) ||
                              (item.quantityRequested ? item.quantityRequested.toLocaleString('en-US') : item.quantityRequested)}
                            </td>
                            {!(this.state.supplier) &&
                              <td>{item.binLocationName}</td>
                            }
                            <td>
                              {item.recipient ? item.recipient.name : null}
                            </td>
                          </tr>
                        ),
                    )
                  }
                  </tbody>
                </table>
                <button
                  type="button"
                  className="btn btn-outline-primary btn-form"
                  disabled={values.statusCode === 'ISSUED'}
                  onClick={() => previousPage(values)}
                >Previous
                </button>
                <button
                  type="submit"
                  className="btn btn-outline-success float-right btn-form"
                  disabled={invalid || values.statusCode === 'ISSUED'}
                >Send Shipment
                </button>
              </div>
            </form>
          )}
        />
      </div>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(SendMovementPage);

SendMovementPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
};
