import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';
import Alert from 'react-s-alert';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import { confirmAlert } from 'react-confirm-alert';

import { renderFormField } from '../../utils/form-utils';
import { showSpinner, hideSpinner } from '../../actions';
import apiClient from '../../utils/apiClient';
import DateField from '../form-elements/DateField';
import DocumentButton from '../DocumentButton';
import SelectField from '../form-elements/SelectField';
import TextField from '../form-elements/TextField';
import LabelField from '../form-elements/LabelField';
import { debouncedLocationsFetch } from '../../utils/option-utils';

const SHIPMENT_FIELDS = {
  description: {
    label: 'Description',
    type: LabelField,
  },
  'origin.name': {
    label: 'Origin',
    type: LabelField,
  },
  destination: {
    label: 'Destination',
    fieldKey: '',
    type: (params) => {
      if (params.canBeEdited && !params.hasStockList) {
        return <SelectField {...params} />;
      }
      return <LabelField {...params} />;
    },
    getDynamicAttr: ({ canBeEdited, hasStockList, onDestinationChange }) => {
      if (canBeEdited && !hasStockList) {
        return {
          required: true,
          async: true,
          showValueTooltip: true,
          openOnClick: false,
          autoload: false,
          loadOptions: debouncedLocationsFetch,
          cache: false,
          options: [],
          onChange: value => (value ? onDestinationChange(value) : null),
        };
      }
      return { formatValue: fieldValue => _.get(fieldValue, 'name') };
    },
  },
  'stockList.name': {
    label: 'Stock List',
    type: LabelField,
  },
  'requestedBy.name': {
    label: 'Requested By',
    type: LabelField,
  },
  dateRequested: {
    label: 'Date Requested',
    type: LabelField,
  },
  name: {
    label: 'Shipment Name',
    type: LabelField,
  },
};

const FIELDS = {
  dateShipped: {
    type: DateField,
    label: 'Ship Date',
    attributes: {
      dateFormat: 'MM/DD/YYYY HH:mm Z',
      required: true,
      showTimeSelect: true,
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
      tableItems: [],
      supplier: false,
      documents: [],
      files: [],
      values: this.props.initialValues,
    };
    this.props.showSpinner();
    this.onDrop = this.onDrop.bind(this);
    this.onDestinationChange = this.onDestinationChange.bind(this);
    this.saveNewDestination = this.saveNewDestination.bind(this);
  }

  componentDidMount() {
    this.props.showSpinner();
    this.fetchShipmentTypes();
    this.fetchStockMovementData();
  }

  onDestinationChange(value) {
    if (this.state && value.id !== this.state.stockMovementData.destination.id) {
      confirmAlert({
        title: 'Confirm change',
        message: 'Do you want to change destination?',
        buttons: [
          {
            label: 'No',
            onClick: () => this.fetchStockMovementData(),
          },
          {
            label: 'Yes',
            onClick: () => this.saveNewDestination(value),
          },
        ],
      });
    }
  }

  /**
   * Updates files' array after dropping them to dropzone area.
   * @param {object} newFiles
   * @public
   */
  onDrop(newFiles) {
    const { files } = this.state;
    const difference = _.differenceBy(files, newFiles, 'name');
    this.setState({
      files: _.concat(difference, newFiles),
    });
  }

  saveNewDestination(value) {
    this.props.showSpinner();
    const url = `/openboxes/api/stockMovements/${this.state.stockMovementData.id}`;
    const payload = {
      'destination.id': value.id,
    };

    apiClient.post(url, payload)
      .then(() => this.fetchStockMovementData())
      .catch(this.props.hideSpinner());
  }

  /**
   * Removes a file by name from files array
   * @param {string} name
   * @public
   */
  removeFile(name) {
    const { files } = this.state;
    _.remove(files, file => file.name === name);
    this.setState({ files });
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

    return apiClient.get(url)
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
        const destinationType = stockMovementData.destination.locationType;
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
            // Below values are reassigned in case of editing destination
            name: stockMovementData.name,
            destination: {
              id: stockMovementData.destination.id,
              type: destinationType ? destinationType.locationTypeCode : null,
              name: stockMovementData.destination.name,
              label: `${stockMovementData.destination.name} 
                [${destinationType ? destinationType.description : null}]`,
            },
          },
        }, () => {
          this.props.setValues(this.state.values);
          this.props.hideSpinner();
        });
      })
      .catch(() => this.props.hideSpinner());
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
                  {_.map(SHIPMENT_FIELDS, (fieldConfig, fieldName) =>
                    renderFormField(fieldConfig, fieldName, {
                      canBeEdited: values.statusCode === 'ISSUED' && values.shipmentStatus !== 'PARTIALLY_RECEIVED',
                      hasStockList: values.stockList,
                      onDestinationChange: this.onDestinationChange,
                    }))}
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
                        <div key={file.name} className="chosen-file d-flex justify-content-center align-items-center">
                          <div className="text-truncate">{file.name}</div>
                          <a
                            href="#"
                            className="remove-button"
                            onClick={(event) => {
                              this.removeFile(file.name);
                              event.stopPropagation();
                            }}
                          >
                            <span className="fa fa-remove" />
                          </a>
                        </div>
                      ))}
                    </Dropzone>
                  </div>
                </div>
              </div>
              <hr />
              <div className="col-md-9 pl-0">
                {_.map(FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    shipmentTypes: this.state.shipmentTypes,
                    issued: values.statusCode === 'ISSUED',
                  }))}
              </div>
              <div>
                <button
                  type="button"
                  className="btn btn-outline-primary btn-form btn-xs"
                  disabled={values.statusCode === 'ISSUED'}
                  onClick={() => previousPage(values)}
                >Previous
                </button>
                <button
                  type="submit"
                  className="btn btn-outline-success float-right btn-form btn-xs"
                  disabled={invalid || values.statusCode === 'ISSUED'}
                >Send Shipment
                </button>
                <table className="table table-striped text-center border my-2 table-xs">
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
                  className="btn btn-outline-primary btn-form btn-xs"
                  disabled={values.statusCode === 'ISSUED'}
                  onClick={() => previousPage(values)}
                >Previous
                </button>
                <button
                  type="submit"
                  className="btn btn-outline-success float-right btn-form btn-xs"
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
  setValues: PropTypes.func.isRequired,
};
