import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';
import Alert from 'react-s-alert';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import { getTranslate } from 'react-localize-redux';
import { confirmAlert } from 'react-confirm-alert';

import 'react-confirm-alert/src/react-confirm-alert.css';

import { renderFormField } from '../../../utils/form-utils';
import { showSpinner, hideSpinner } from '../../../actions';
import apiClient from '../../../utils/apiClient';
import DateField from '../../form-elements/DateField';
import DocumentButton from '../../DocumentButton';
import SelectField from '../../form-elements/SelectField';
import TextField from '../../form-elements/TextField';
import LabelField from '../../form-elements/LabelField';
import { debounceLocationsFetch } from '../../../utils/option-utils';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';
import ArrayField from '../../form-elements/ArrayField';

const BASIC_FIELDS = {
  description: {
    label: 'react.stockMovement.description.label',
    defaultMessage: 'Description',
    type: (params) => {
      if (params.issued) {
        return <TextField {...params} />;
      }

      return <LabelField {...params} />;
    },
  },
  'origin.name': {
    label: 'react.stockMovement.origin.label',
    defaultMessage: 'Origin',
    type: LabelField,
  },
  destination: {
    label: 'react.stockMovement.destination.label',
    defaultMessage: 'Destination',
    fieldKey: '',
    type: (params) => {
      if (params.canBeEdited && !params.hasStockList) {
        return <SelectField {...params} />;
      }
      return <LabelField {...params} />;
    },
    getDynamicAttr: ({ canBeEdited, hasStockList, debouncedLocationsFetch }) => {
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
          filterOptions: options => options,
        };
      }
      return { formatValue: fieldValue => _.get(fieldValue, 'name') };
    },
  },
  'stocklist.name': {
    label: 'react.stockMovement.stocklist.label',
    defaultMessage: 'Stocklist',
    type: LabelField,
  },
  'requestedBy.name': {
    label: 'react.stockMovement.requestedBy.label',
    defaultMessage: 'Requested by',
    type: LabelField,
  },
  dateRequested: {
    label: 'react.stockMovement.dateRequested.label',
    defaultMessage: 'Date requested',
    type: LabelField,
  },
  name: {
    label: 'react.stockMovement.shipmentName.label',
    defaultMessage: 'Shipment name',
    type: LabelField,
  },
};

const SHIPMENT_FIELDS = {
  dateShipped: {
    type: DateField,
    label: 'react.stockMovement.shipDate.label',
    defaultMessage: 'Shipment date',
    attributes: {
      dateFormat: 'MM/DD/YYYY HH:mm Z',
      required: true,
      showTimeSelect: true,
      autoComplete: 'off',
    },
    getDynamicAttr: ({ issued, showOnly }) => ({
      disabled: issued || showOnly,
    }),
  },
  shipmentType: {
    type: SelectField,
    label: 'react.stockMovement.shipmentType.label',
    defaultMessage: 'Shipment type',
    attributes: {
      required: true,
      showValueTooltip: true,
    },
    getDynamicAttr: ({ shipmentTypes, received, showOnly }) => ({
      options: shipmentTypes,
      disabled: showOnly || received,
    }),
  },
  trackingNumber: {
    type: TextField,
    label: 'react.stockMovement.trackingNumber.label',
    defaultMessage: 'Tracking number',
    getDynamicAttr: ({ received, showOnly }) => ({
      disabled: showOnly || received,
    }),
  },
  driverName: {
    type: TextField,
    label: 'react.stockMovement.driverName.label',
    defaultMessage: 'Driver name',
    getDynamicAttr: ({ received, showOnly }) => ({
      disabled: showOnly || received,
    }),
  },
  comments: {
    type: TextField,
    label: 'react.stockMovement.comments.label',
    defaultMessage: 'Comments',
    getDynamicAttr: ({ received, showOnly }) => ({
      disabled: showOnly || received,
    }),
  },
};

const SUPPLIER_FIELDS = {
  tableItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    fields: {
      palletName: {
        type: LabelField,
        label: 'react.stockMovement.packLevel1.label',
        defaultMessage: 'Pack level 1',
      },
      boxName: {
        type: LabelField,
        label: 'react.stockMovement.packLevel2.label',
        defaultMessage: 'Pack level 2',
      },
      productCode: {
        type: LabelField,
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
      },
      'product.name': {
        type: LabelField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        attributes: {
          className: 'text-left',
        },
      },
      lotNumber: {
        type: LabelField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
      },
      quantityRequested: {
        type: LabelField,
        fixedWidth: '150px',
        label: 'react.stockMovement.quantityPicked.label',
        defaultMessage: 'Qty Picked',
      },
      'recipient.name': {
        type: LabelField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
      },
    },
  },
};

function validate(values) {
  const errors = {};

  if (!values.dateShipped) {
    errors.dateShipped = 'react.default.error.requiredField.label';
  }
  if (!values.shipmentType) {
    errors.shipmentType = 'react.default.error.requiredField.label';
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
      documents: [],
      files: [],
      values: { ...this.props.initialValues, tableItems: [] },
      totalCount: 0,
    };
    this.props.showSpinner();
    this.onDrop = this.onDrop.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);

    this.debouncedLocationsFetch =
      debounceLocationsFetch(this.props.debounceTime, this.props.minSearchLength);
  }

  componentDidMount() {
    this.props.showSpinner();
    if (this.props.stockMovementTranslationsFetched) {
      this.dataFetched = true;

      this.fetchStockMovementData();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchStockMovementData();
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

  onSave(values) {
    this.props.showSpinner();

    this.saveValues(values)
      .then(() => {
        this.props.hideSpinner();

        if (values.statusCode === 'DISPATCHED') {
          this.fetchStockMovementData();
        }
        Alert.success(this.props.translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'), { timeout: 3000 });
      })
      .catch(() => this.props.hideSpinner());
  }

  dataFetched = false;

  saveValues(values) {
    let payload = {
      dateShipped: values.dateShipped,
      'shipmentType.id': values.shipmentType,
      trackingNumber: values.trackingNumber || '',
      driverName: values.driverName || '',
      comments: values.comments || '',
    };

    if (values.statusCode === 'DISPATCHED') {
      payload = {
        'destination.id': values.destination.id,
        description: values.description,
        'shipmentType.id': values.shipmentType,
        trackingNumber: values.trackingNumber || '',
        driverName: values.driverName || '',
        comments: values.comments || '',
      };
    }

    return this.saveShipment(payload);
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
        const shipmentTypes = _.map(response.data.data, (type) => {
          const [en, fr] = _.split(type.name, '|fr:');
          return {
            value: type.id,
            label: this.props.locale === 'fr' && fr ? fr : en,
          };
        });

        this.setState({ shipmentTypes }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }


  fetchStockMovementItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=6`;
    apiClient.get(url)
      .then((response) => {
        const { data } = response.data;
        const tableItems = data;
        this.setState({
          values: {
            ...this.state.values,
            tableItems,
          },
        });
      });
  }

  loadMoreRows({ startIndex, stopIndex }) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${stopIndex - startIndex > 0 ? stopIndex - startIndex : 1}&stepNumber=6`;
    apiClient.get(url)
      .then((response) => {
        const { data } = response.data;
        this.setState({
          values: {
            ...this.state.values,
            tableItems: _.uniq(_.concat(this.state.values.tableItems, data)),
          },
        });
      });
  }

  isRowLoaded({ index }) {
    return !!this.state.values.tableItems[index];
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
        const { totalCount } = response.data;

        const documents = _.filter(associations.documents, doc => doc.stepNumber === 5);
        const destinationType = stockMovementData.destination.locationType;
        this.setState({
          documents,
          totalCount,
          values: {
            ...this.state.values,
            dateShipped: stockMovementData.dateShipped,
            shipmentType: _.get(stockMovementData, 'shipmentType.id'),
            trackingNumber: stockMovementData.trackingNumber,
            driverName: stockMovementData.driverName,
            comments: stockMovementData.comments,
            // Below values are reassigned in case of editing destination or description
            name: stockMovementData.name,
            description: stockMovementData.description,
            destination: {
              id: stockMovementData.destination.id,
              type: destinationType ? destinationType.locationTypeCode : null,
              name: stockMovementData.destination.name,
              label: `${stockMovementData.destination.name}
                [${destinationType ? destinationType.description : null}]`,
            },
          },
        }, () => {
          this.props.nextPage(this.state.values);
          this.fetchShipmentTypes();
          if (!this.props.isPaginated) {
            this.fetchStockMovementItems();
          }
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
   * Saves data with shipment details.
   * @param {object} payload
   * @public
   */
  saveShipment(payload) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/updateShipment`;

    return apiClient.post(url, payload);
  }

  /**
   * Updates stock movement status to DISPATCHED with post method.
   * @public
   */
  stateTransitionToSent() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'DISPATCHED' };

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
          .then(() => Alert.success(this.props.translate('react.stockMovement.alert.fileSuccess.label', 'File uploaded successfuly!'), { timeout: 3000 }))
          .catch(() => Alert.error(this.props.translate('react.stockMovement.alert.fileError.label', 'Error occured during file upload!')));
      });
    }

    const payload = {
      dateShipped: values.dateShipped,
      'shipmentType.id': values.shipmentType,
      trackingNumber: values.trackingNumber || '',
      driverName: values.driverName || '',
      comments: values.comments || '',
    };

    if ((this.props.currentLocationId !== values.origin.id) && (values.origin.type !== 'SUPPLIER' && values.hasManageInventory)) {
      Alert.error(this.props.translate(
        'react.stockMovement.alert.sendStockMovement.label',
        'You are not able to send shipment from a location other than origin. Change your current location.',
      ));
      this.props.hideSpinner();
    } else if (values.shipmentType === _.find(this.state.shipmentTypes, shipmentType => shipmentType.label === 'Default').value) {
      Alert.error(this.props.translate(
        'react.stockMovement.alert.populateShipmentType.label',
        'Please populate shipment type before continuing',
      ));
      this.props.hideSpinner();
    } else {
      this.saveShipment(payload)
        .then(() => {
          this.stateTransitionToSent()
            .then(() => {
              // redirect to requisition list
              window.location = `/openboxes/stockMovement/show/${this.state.values.stockMovementId}`;
            })
            .catch(() => this.props.hideSpinner());
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  /**
   * Saves changes made by user in this step and go back to previous page
   * @param {object} values
   * @param {boolean} invalid
   * @public
   */
  previousPage(values, invalid) {
    if (!invalid) {
      this.saveValues(values)
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

  /**
   * Saves changes made by user in this step and redirects to the shipment view page
   * @param {object} values
   * @public
   */
  saveAndExit(values) {
    const errors = validate(values);
    if (_.isEmpty(errors)) {
      this.saveValues(values)
        .then(() => {
          window.location = `/openboxes/stockMovement/show/${values.stockMovementId}`;
        });
    } else {
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmExit.label', 'Confirm save'),
        message: this.props.translate(
          'react.stockMovement.confirmExit.message',
          'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => { window.location = `/openboxes/stockMovement/show/${values.stockMovementId}`; },
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
          },
        ],
      });
    }
  }

  /**
   * Rollback stock movement if it has been shipped
   * @public
   */
  rollbackStockMovement(values) {
    this.props.showSpinner();
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { rollback: true };

    const isOrigin = this.props.currentLocationId === values.origin.id;
    const isDestination = this.props.currentLocationId === values.destination.id;

    if ((values.hasManageInventory && isOrigin) || (!values.hasManageInventory && isDestination)) {
      apiClient.post(url, payload)
        .then(() => {
          this.props.hideSpinner();
          window.location.reload();
        });
    } else {
      this.props.hideSpinner();
      Alert.error(this.props.translate(
        'react.stockMovement.alert.rollbackShipment.label',
        'You are not able to rollback shipment from your location.',
      ));
    }
  }

  render() {
    return (
      <div>
        <hr />
        <Form
          onSubmit={() => {}}
          validate={validate}
          mutators={{ ...arrayMutators }}
          initialValues={this.state.values}
          render={({ handleSubmit, values, invalid }) => (
            <form onSubmit={handleSubmit}>
              <div className="d-flex">
                <div id="stockMovementInfo" style={{ flexGrow: 2 }}>
                  {_.map(BASIC_FIELDS, (fieldConfig, fieldName) =>
                    renderFormField(fieldConfig, fieldName, {
                      canBeEdited: values.statusCode === 'DISPATCHED' && !values.received,
                      issued: values.statusCode === 'DISPATCHED',
                      hasStockList: !!_.get(values.stocklist, 'id'),
                      debouncedLocationsFetch: this.debouncedLocationsFetch,
                    }))}
                </div>
                <div className="print-buttons-container col-md-3 flex-grow-1">
                  {this.state.documents.length && _.map(this.state.documents, (document, idx) => {
                    if (document.hidden) {
                      return null;
                    }
                    return (<DocumentButton
                      link={document.uri}
                      buttonTitle={document.name}
                      {...document}
                      key={idx}
                      onClick={() => this.saveValues(values)}
                      disabled={false}
                    />);
                  })}
                  <div className="dropzone btn btn-outline-secondary">
                    <Dropzone
                      disabled={values.statusCode === 'DISPATCHED'}
                      onDrop={this.onDrop}
                      multiple
                    >
                      <span><i className="fa fa-upload pr-2" /><Translate id="react.stockMovement.uploadDocuments.label" defaultMessage="Upload Documents" /></span>
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
              <span>
                <button
                  type="button"
                  onClick={() => this.onSave(values)}
                  className="btn btn-outline-secondary float-right btn-form btn-xs"
                  disabled={invalid}
                >
                  <span><i className="fa fa-save pr-2" /><Translate id="react.default.button.save.label" defaultMessage="Save" /></span>
                </button>
                <button
                  type="button"
                  onClick={() => this.saveAndExit(values)}
                  className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                >
                  <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
                </button>
              </span>
              <div className="col-md-9 pl-0">
                {_.map(SHIPMENT_FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    shipmentTypes: this.state.shipmentTypes,
                    issued: values.statusCode === 'DISPATCHED',
                    received: values.received,
                  }))}
              </div>
              <div>
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form btn-xs"
                  disabled={values.statusCode === 'DISPATCHED'}
                  onClick={() => this.previousPage(values, invalid)}
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  onClick={() => { this.submitStockMovement(values); }}
                  className={`${values.shipped ? 'btn btn-outline-secondary' : 'btn btn-outline-success'} float-right btn-form btn-xs`}
                  disabled={invalid || values.statusCode === 'DISPATCHED'}
                ><Translate id="react.stockMovement.sendShipment.label" defaultMessage="Send shipment" />
                </button>
                {values.shipped && this.props.isUserAdmin ?
                  <button
                    type="submit"
                    onClick={() => { this.rollbackStockMovement(values); }}
                    className="btn btn-outline-success float-right btn-xs"
                    disabled={invalid || !(values.statusCode === 'DISPATCHED')}
                  >
                    <span><i className="fa fa-undo pr-2" /><Translate id="react.default.button.rollback.label" defaultMessage="Rollback" /></span>
                  </button> : null
                }
                <div className="my-2">
                  {_.map(SUPPLIER_FIELDS, (fieldConfig, fieldName) =>
                      renderFormField(fieldConfig, fieldName, {
                        hasBinLocationSupport: this.props.hasBinLocationSupport,
                        totalCount: this.state.totalCount,
                        loadMoreRows: this.loadMoreRows,
                        isRowLoaded: this.isRowLoaded,
                        isPaginated: this.props.isPaginated,
                      }))}
                </div>
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form btn-xs"
                  disabled={values.statusCode === 'DISPATCHED'}
                  onClick={() => this.previousPage(values, invalid)}
                > <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  onClick={() => { this.submitStockMovement(values); }}
                  className={`${values.shipped ? 'btn btn-outline-secondary' : 'btn btn-outline-success'} float-right btn-form btn-xs`}
                  disabled={invalid || values.statusCode === 'DISPATCHED'}
                ><Translate id="react.stockMovement.sendShipment.label" defaultMessage="Send shipment" />
                </button>
                {values.shipped && this.props.isUserAdmin ?
                  <button
                    type="submit"
                    onClick={() => { this.rollbackStockMovement(values); }}
                    className="btn btn-outline-success float-right  btn-xs"
                    disabled={invalid || !(values.statusCode === 'DISPATCHED')}
                  >
                    <span><i className="fa fa-undo pr-2" /><Translate id="react.default.button.rollback.label" defaultMessage="Rollback" /></span>
                  </button> : null
                }
              </div>
            </form>
          )}
        />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  currentLocationId: state.session.currentLocation.id,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  locale: state.session.activeLanguage,
  isUserAdmin: state.session.isUserAdmin,
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  isPaginated: state.session.isPaginated,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(SendMovementPage);

SendMovementPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  /** Name of the currently selected location */
  currentLocationId: PropTypes.string.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  locale: PropTypes.string.isRequired,
  isUserAdmin: PropTypes.bool.isRequired,
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
};
