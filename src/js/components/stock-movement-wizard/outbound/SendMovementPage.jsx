import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import Dropzone from 'react-dropzone';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import Alert from 'react-s-alert';
import { Tooltip } from 'react-tippy';

import { hideSpinner, showSpinner } from 'actions';
import DocumentButton from 'components/DocumentButton';
import ArrayField from 'components/form-elements/ArrayField';
import DateField from 'components/form-elements/DateField';
import LabelField from 'components/form-elements/LabelField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import DateFormat from 'consts/dateFormat';
import AlertMessage from 'utils/AlertMessage';
import {
  apiClientCustomResponseHandler as apiClient,
  handleError,
  handleSuccess,
  handleValidationErrors,
} from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import { formatProductDisplayName } from 'utils/form-values-utils';
import { debounceLocationsFetch } from 'utils/option-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';
import splitTranslation, { formatDate } from 'utils/translation-utils';

import 'react-confirm-alert/src/react-confirm-alert.css';

const SHIPMENT_FIELDS = {
  'origin.name': {
    label: 'react.stockMovement.origin.label',
    defaultMessage: 'Origin',
    type: (params) => <TextField {...params} />,
    attributes: {
      disabled: true,
    },
  },
  destination: {
    label: 'react.stockMovement.destination.label',
    defaultMessage: 'Destination',
    fieldKey: '',
    type: (params) => {
      if (params.canBeEdited && !params.hasStockList) {
        return <SelectField {...params} />;
      }
      return null;
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
          filterOptions: (options) => options,
        };
      }
      return { formatValue: (fieldValue) => _.get(fieldValue, 'name') };
    },
  },
  'destination.name': {
    label: 'react.stockMovement.destination.label',
    defaultMessage: 'Destination',
    type: (params) => {
      if (params.canBeEdited && !params.hasStockList) {
        return null;
      }
      return <TextField {...params} />;
    },
    getDynamicAttr: ({ canBeEdited, hasStockList }) => ({
      disabled: !canBeEdited || hasStockList,
    }),
  },
  dateShipped: {
    type: DateField,
    label: 'react.stockMovement.shipDate.label',
    defaultMessage: 'Shipment date',
    attributes: {
      localizeDate: true,
      localizedDateFormat: DateFormat.DEFAULT,
      // It's necessary for properly setting up the time part of the localized date format
      dateFormat: 'MM/DD/YYYY HH:mm Z',
      required: true,
      showTimeSelect: true,
      autoComplete: 'off',
      showError: true,
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
      valueKey: 'id',
      labelKey: 'name',
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
  expectedDeliveryDate: {
    type: DateField,
    label: 'react.stockMovement.expectedDeliveryDate.label',
    defaultMessage: 'Expected receipt date',
    attributes: {
      localizeDate: true,
      required: true,
      showTimeSelect: false,
      autoComplete: 'off',
    },
  },
};

const FIELDS = {
  tableItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    getDynamicRowAttr: ({ rowValues }) => ({ className: rowValues.recalled ? 'recalled ' : '' }),
    fields: {
      recalled: {
        type: (params) => {
          const { fieldValue } = params;
          if (fieldValue) {
            return (
              <div className="d-flex align-items-center justify-content-center">
                <Tooltip
                  html={params.translate('react.stockMovement.recalledLot.message', 'This lot has been recalled')}
                  theme="transparent"
                  delay="150"
                  duration="250"
                  hideDelay="50"
                >
                  {/* &#x24C7; = hexadecimal circled letter R */}
                  <b>&#x24C7;</b>
                </Tooltip>
              </div>
            );
          }
          return null;
        },
        label: '',
        defaultMessage: '',
        flexWidth: '0.5',
      },
      palletName: {
        type: LabelField,
        label: 'react.stockMovement.packLevel1.label',
        defaultMessage: 'Pack level 1',
        flexWidth: '3',
        getDynamicAttr: ({ isPalletNameEmpty }) => ({ hide: isPalletNameEmpty }),
      },
      boxName: {
        type: LabelField,
        label: 'react.stockMovement.packLevel2.label',
        defaultMessage: 'Pack level 2',
        flexWidth: '3',
        getDynamicAttr: ({ isBoxNameEmpty }) => ({ hide: isBoxNameEmpty }),
      },
      productCode: {
        type: LabelField,
        label: 'react.stockMovement.code.label',
        defaultMessage: 'Code',
        flexWidth: '3.5',
      },
      product: {
        type: LabelField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        getDynamicAttr: ({ isBoxNameEmpty, fieldValue, isPalletNameEmpty }) => ({
          flexWidth: 7 + (isBoxNameEmpty ? 3 : 0) + (isPalletNameEmpty ? 3 : 0),
          showValueTooltip: !!fieldValue?.displayNames?.default,
          tooltipValue: fieldValue?.name,
          color: fieldValue?.color,
        }),
        attributes: {
          className: 'text-left',
          formatValue: formatProductDisplayName,
        },
      },
      lotNumber: {
        type: LabelField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '3.5',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '3.5',
        getDynamicAttr: ({ formatLocalizedDate }) => ({
          formatValue: (value) => formatLocalizedDate(value, DateFormat.COMMON),
        }),
      },
      quantityShipped: {
        type: LabelField,
        fixedWidth: '150px',
        label: 'react.stockMovement.quantityPicked.label',
        defaultMessage: 'Qty Picked',
      },
      binLocation: {
        type: LabelField,
        label: 'react.stockMovement.binLocation.label',
        flexWidth: '3.5',
        defaultMessage: 'Bin Location',
        getDynamicAttr: ({ hasBinLocationSupport }) => ({
          hide: !hasBinLocationSupport,
        }),
        attributes: {
          showValueTooltip: true,
          formatValue: (fieldValue) => fieldValue && (
            <div className="d-flex">
              {fieldValue.zoneName ? <div className="text-truncate" style={{ minWidth: 30, flexShrink: 20 }}>{fieldValue.zoneName}</div> : ''}
              <div className="text-truncate">{fieldValue.zoneName ? `: ${fieldValue.name}` : fieldValue.name}</div>
            </div>
          ),
        },
      },
      'recipient.name': {
        type: LabelField,
        flexWidth: '3.5',
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
      },
    },
  },
};

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
      isFirstPageLoaded: false,
      isDropdownVisible: false,
      showAlert: false,
      alertMessage: '',
    };
    this.props.showSpinner();
    this.onDrop = this.onDrop.bind(this);
    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.toggleDropdown = this.toggleDropdown.bind(this);
    this.validate = this.validate.bind(this);
    this.setState = this.setState.bind(this);

    this.debouncedLocationsFetch = debounceLocationsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
    );

    apiClient.interceptors.response.use(handleSuccess, handleValidationErrors(this.setState));
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
    const payload = {
      destination: { id: values.destination.id },
      dateShipped: values.dateShipped,
      shipmentType: { id: values.shipmentType.id },
      trackingNumber: values.trackingNumber || '',
      driverName: values.driverName || '',
      comments: values.comments || '',
      expectedDeliveryDate: values.expectedDeliveryDate || '',
    };

    return this.saveShipment(payload);
  }

  /**
   * Removes a file by name from files array
   * @param {string} name
   * @public
   */
  removeFile(name) {
    const { files } = this.state;
    _.remove(files, (file) => file.name === name);
    this.setState({ files });
  }

  /**
   * Removes a bulk of files by name from files array
   * @param {Array} names
   * @public
   */
  removeFiles(names) {
    const { files } = this.state;
    _.forEach(names, (name) => {
      _.remove(files, (file) => file.name === name);
    });
    this.setState({ files });
  }

  /**
   * Fetches available shipment types from API.
   * @public
   */
  fetchShipmentTypes() {
    const url = '/api/generic/shipmentType';

    return apiClient.get(url)
      .then((response) => {
        const shipmentTypes = _.map(response.data.data, (type) => {
          const [en, fr] = _.split(type.name, '|fr:');
          return {
            ...type,
            label: this.props.locale === 'fr' && fr ? fr : en,
          };
        });

        this.setState({ shipmentTypes }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchStockMovementItems() {
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?stepNumber=6`;
    apiClient.get(url)
      .then((response) => {
        const { data } = response.data;
        const tableItems = data;
        this.setState((prev) => ({
          values: {
            ...prev.values,
            tableItems,
          },
        }));
      });
  }

  loadMoreRows({ startIndex }) {
    if (this.state.totalCount) {
      const url = `/api/stockMovements/${this.state.values.stockMovementId}/stockMovementItems?offset=${startIndex}&max=${this.props.pageSize}&stepNumber=6`;
      apiClient.get(url)
        .then((response) => {
          const { data } = response.data;
          this.setState((prev) => ({
            values: {
              ...prev.values,
              tableItems: _.uniqBy(_.concat(prev.values.tableItems, data), 'shipmentItemId'),
            },
            isFirstPageLoaded: true,
          }), () => {
            if (this.state.values.tableItems.length !== this.state.totalCount) {
              this.loadMoreRows({
                startIndex: startIndex + this.props.pageSize,
              });
            }
          });
        });
    }
  }

  isRowLoaded({ index }) {
    return !!this.state.values.tableItems[index];
  }

  /**
   * Fetches 5th step data from current stock movement.
   * @public
   */
  fetchStockMovementData() {
    const url = `/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=6`;

    return apiClient.get(url)
      .then((response) => {
        const stockMovementData = response.data.data;
        const { associations } = response.data.data;
        const { totalCount } = response.data;

        const documents = _.filter(associations.documents, (doc) => doc.stepNumber === 5);
        const destinationType = stockMovementData.destination.locationType;
        this.setState((prev) => ({
          documents,
          totalCount,
          values: {
            ...prev.values,
            dateShipped: stockMovementData.dateShipped,
            shipmentType: {
              ...stockMovementData.shipmentType,
              label: splitTranslation(stockMovementData.shipmentType.name, this.props.locale),
            },
            trackingNumber: stockMovementData.trackingNumber,
            driverName: stockMovementData.driverName,
            comments: stockMovementData.comments,
            expectedDeliveryDate: stockMovementData.expectedDeliveryDate,
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
            requestType: {
              name: stockMovementData.requestType.name,
              label: stockMovementData.requestType.name,
            },
            shipmentStatus: stockMovementData.shipmentStatus,
          },
        }), () => {
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
    const url = `/stockMovement/uploadDocument/${this.state.values.stockMovementId}`;

    const data = new FormData();
    data.append('fileContents', file);

    return apiClient.post(url, data);
  }

  /**
   * Bulk send of files uploaded by user to backend.
   * @param {Array} files
   * @public
   */
  sendFiles(files) {
    const url = `/stockMovement/uploadDocuments/${this.state.values.stockMovementId}`;

    const data = new FormData();
    _.forEach(files, (file, idx) => {
      data.append(`filesContents[${idx}]`, file);
    });

    return apiClient.post(url, data);
  }

  /**
   * Saves data with shipment details.
   * @param {object} payload
   * @public
   */
  saveShipment(payload) {
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/updateShipment`;

    return apiClient.post(url, payload);
  }

  /**
   * Updates stock movement status to DISPATCHED with post method.
   * @public
   */
  stateTransitionToIssued() {
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status: 'DISPATCHED' };

    return apiClient.post(url, payload);
  }

  /**
   * Uploads files and sends the whole stock movement.
   * @param {object} values
   * @public
   */
  sendFilesAndSave(values) {
    const errors = this.validate(values);
    if (_.isEmpty(errors)) {
      this.props.showSpinner();
      const { files } = this.state;
      if (files.length > 1) {
        this.sendFiles(files)
          .then(() => {
            Alert.success(this.props.translate('react.stockMovement.alert.filesSuccess.label', 'Files uploaded successfuly!'), { timeout: 3000 });
            this.removeFiles(_.map(files, (file) => file.name));
            this.prepareRequestAndSubmitStockMovement(values);
          })
          .catch(() => Alert.error(this.props.translate('react.stockMovement.alert.filesError.label', 'Error occured during files upload!')))
          .finally(() => this.props.hideSpinner());
      } else if (files.length === 1) {
        this.sendFile(files[0])
          .then(() => {
            Alert.success(this.props.translate('react.stockMovement.alert.fileSuccess.label', 'File uploaded successfuly!'), { timeout: 3000 });
            this.removeFile(files[0].name);
            this.prepareRequestAndSubmitStockMovement(values);
          })
          .catch(() => Alert.error(this.props.translate('react.stockMovement.alert.fileError.label', 'Error occured during file upload!')))
          .finally(() => this.props.hideSpinner());
      } else {
        this.prepareRequestAndSubmitStockMovement(values);
      }
    }
  }

  prepareRequestAndSubmitStockMovement(values) {
    const payload = {
      dateShipped: values.dateShipped,
      shipmentType: {
        id: values.shipmentType.id,
      },
      trackingNumber: values.trackingNumber || '',
      driverName: values.driverName || '',
      comments: values.comments || '',
      expectedDeliveryDate: values.expectedDeliveryDate || '',
    };

    if ((this.props.currentLocationId !== values.origin.id) && (values.origin.type !== 'SUPPLIER' && values.hasManageInventory)) {
      Alert.error(this.props.translate(
        'react.stockMovement.alert.sendStockMovement.label',
        'You are not able to send shipment from a location other than origin. Change your current location.',
      ));
      this.props.hideSpinner();
    } else if (values.shipmentType.id === _.find(this.state.shipmentTypes, (shipmentType) => shipmentType.label === 'Default').id) {
      Alert.error(this.props.translate(
        'react.stockMovement.alert.populateShipmentType.label',
        'Please populate shipment type before continuing',
      ));
      this.props.hideSpinner();
    } else if (moment().startOf('day').diff(values.dateShipped) > 0) {
      confirmAlert({
        title: this.props.translate('react.stockMovement.message.confirmSend.label', 'Confirm send'),
        message: this.props.translate(
          'react.stockMovement.alert.sendWithPastDate.message.label',
          'You are sending a shipment with a ship date in the past. Would you like to update the ship date to today?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => {
              payload.dateShipped = moment(new Date()).format('MM/DD/YYYY HH:mm Z');
              this.saveAndTransitionToIssued(payload);
            },
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
            onClick: () => this.saveAndTransitionToIssued(payload),
          },
        ],
        closeOnEscape: false,
        closeOnClickOutside: false,
      });
    } else {
      this.saveAndTransitionToIssued(payload);
    }
  }

  saveAndTransitionToIssued(payload) {
    this.saveShipment(payload)
      .then(() => {
        this.stateTransitionToIssued()
          .then(() => {
            // redirect to requisition list
            window.location = STOCK_MOVEMENT_URL.show(this.state.values.stockMovementId);
          })
          .catch(() => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
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
    const errors = this.validate(values);
    if (_.isEmpty(errors)) {
      this.saveValues(values)
        .then(() => {
          window.location = STOCK_MOVEMENT_URL.show(values.stockMovementId);
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
            onClick: () => { window.location = STOCK_MOVEMENT_URL.show(values.stockMovementId); },
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
    const url = `/api/stockMovements/${this.state.values.stockMovementId}/status`;
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

  /**
   * Toggle the downloadable files
   */
  toggleDropdown() {
    this.setState((prev) => ({
      isDropdownVisible: !prev.isDropdownVisible,
    }));
  }

  validate(values) {
    const errors = {};
    const date = moment(this.props.minimumExpirationDate, 'MM/DD/YYYY');
    const dateShipped = moment(values.dateShipped, 'MM/DD/YYYY');
    const expectedDeliveryDate = moment(values.expectedDeliveryDate, 'MM/DD/YYYY');

    if (date.diff(dateShipped) > 0) {
      errors.dateShipped = 'react.stockMovement.error.invalidDate.label';
    }
    if (!values.dateShipped) {
      errors.dateShipped = 'react.default.error.requiredField.label';
    }
    if (!values.shipmentType) {
      errors.shipmentType = 'react.default.error.requiredField.label';
    }
    if (!values.expectedDeliveryDate) {
      errors.expectedDeliveryDate = 'react.default.error.requiredField.label';
    }
    if (moment(dateShipped).diff(expectedDeliveryDate) > 0) {
      errors.expectedDeliveryDate = 'react.stockMovement.error.deliveryDateBeforeShipDate.label';
      errors.dateShipped = 'react.stockMovement.error.deliveryDateBeforeShipDate.label';
    }

    return errors;
  }

  handleValidationErrors(error) {
    if (error.response.status === 400) {
      // FIXME: Refactor this error massage handling to get prettier default errorMessage fallback
      let alertMessage = _.join(_.get(error, 'response.data.errorMessages', ''), ' ');
      if (alertMessage === '') {
        alertMessage = _.get(error, 'response.data.errorMessage', '');
      }
      this.setState({ alertMessage, showAlert: true });

      return Promise.reject(error);
    }

    return handleError(error);
  }

  render() {
    const { showOnly } = this.props;

    return (
      <div>
        <Form
          onSubmit={() => {}}
          validate={this.validate}
          mutators={{ ...arrayMutators }}
          initialValues={this.state.values}
          render={({ handleSubmit, values, invalid }) => (
            <form onSubmit={handleSubmit}>
              <AlertMessage
                show={this.state.showAlert}
                message={this.state.alertMessage}
                danger
              />
              <div className="classic-form classic-form-condensed">
                <span className="buttons-container classic-form-buttons">
                  <div
                    className={`dropzone float-right mb-1 btn btn-outline-secondary ${values.statusCode === 'DISPATCHED' || showOnly ? 'disabled' : ''} align-self-end btn-xs mr-1`}
                  >
                    <Dropzone
                      disabled={values.statusCode === 'DISPATCHED' || showOnly}
                      onDrop={this.onDrop}
                      multiple
                    >
                      {({ getRootProps }) => (
                        <div {...getRootProps()}>
                          <span>
                            <i className="fa fa-upload pr-2" />
                            <Translate id="react.stockMovement.uploadDocuments.label" defaultMessage="Upload Documents" />
                          </span>
                          {_.map(this.state.files, (file) => (
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
                        </div>
                      )}
                    </Dropzone>
                  </div>
                  <div className="dropdown">
                    <button
                      type="button"
                      onClick={this.toggleDropdown}
                      className="dropdown-button float-right mb-1 btn btn-outline-secondary align-self-end btn-xs mr-1"
                    >
                      <span>
                        <i className="fa fa-sign-out pr-2" />
                        <Translate id="react.default.button.download.label" defaultMessage="Download" />
                      </span>
                    </button>
                    <div className={`dropdown-content print-buttons-container col-md-3 flex-grow-1
                      ${this.state.isDropdownVisible ? 'visible' : ''}`}
                    >
                      {this.state.documents.length && _.map(
                        this.state.documents,
                        (document, idx) => {
                          if (document.hidden) {
                            return null;
                          }
                          return (
                            <DocumentButton
                              link={document.uri}
                              buttonTitle={document.name}
                              {...document}
                              key={idx}
                              onClick={() => this.saveValues(values)}
                            />
                          );
                        },
                      )}
                    </div>
                  </div>
                  { !showOnly
                    ? (
                      <span>
                        <button
                          type="button"
                          onClick={() => this.onSave(values)}
                          className="btn btn-outline-secondary float-right btn-form btn-xs ml-1"
                          disabled={invalid}
                        >
                          <span>
                            <i className="fa fa-save pr-2" />
                            <Translate id="react.default.button.save.label" defaultMessage="Save" />
                          </span>
                        </button>
                        <button
                          type="button"
                          onClick={() => this.saveAndExit(values)}
                          className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                        >
                          <span>
                            <i className="fa fa-sign-out pr-2" />
                            <Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" />
                          </span>
                        </button>
                      </span>
                    )
                    : (
                      <button
                        type="button"
                        onClick={() => {
                          this.props.history.push(STOCK_MOVEMENT_URL.listOutbound());
                        }}
                        className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
                      >
                        <span>
                          <i className="fa fa-sign-out pr-2" />
                          {' '}
                          <Translate id="react.default.button.exit.label" defaultMessage="Exit" />
                          {' '}
                        </span>
                      </button>
                    ) }
                </span>
                <div className="form-title"><Translate id="react.stockMovement.sendingOptions.label" defaultMessage="Sending options" /></div>
                {_.map(SHIPMENT_FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    shipmentTypes: this.state.shipmentTypes,
                    issued: values.statusCode === 'DISPATCHED',
                    received: values.received,
                    showOnly,
                    canBeEdited: !values.received,
                    debouncedLocationsFetch: this.debouncedLocationsFetch,
                    hasStockList: !!_.get(values.stocklist, 'id'),
                  }))}
              </div>
              <div>
                <div className="submit-buttons">
                  <button
                    type="submit"
                    className="btn btn-outline-primary btn-form btn-xs"
                    disabled={values.statusCode === 'DISPATCHED' || showOnly}
                    onClick={() => this.previousPage(values, invalid)}
                  >
                    <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                  </button>
                  <button
                    type="submit"
                    onClick={() => { this.sendFilesAndSave(values); }}
                    className={`${values.shipped ? 'btn btn-outline-secondary' : 'btn btn-outline-success'} float-right btn-form btn-xs`}
                    disabled={values.statusCode === 'DISPATCHED' || showOnly}
                  >
                    <Translate id="react.stockMovement.sendShipment.label" defaultMessage="Send shipment" />
                  </button>
                  {values.shipped && this.props.isUserAdmin
                    ? (
                      <button
                        type="submit"
                        onClick={() => { this.rollbackStockMovement(values); }}
                        className="btn btn-outline-success float-right btn-xs"
                        disabled={invalid || values.statusCode !== 'DISPATCHED' || showOnly}
                      >
                        <span>
                          <i className="fa fa-undo pr-2" />
                          <Translate id="react.default.button.rollback.label" defaultMessage="Rollback" />
                        </span>
                      </button>
                    ) : null}
                </div>
                <div className="my-2 table-form">
                  {_.map(FIELDS, (fieldConfig, fieldName) =>
                    renderFormField(fieldConfig, fieldName, {
                      hasBinLocationSupport: this.props.hasBinLocationSupport,
                      totalCount: this.state.totalCount,
                      loadMoreRows: this.loadMoreRows,
                      isRowLoaded: this.isRowLoaded,
                      isPaginated: this.props.isPaginated,
                      isFirstPageLoaded: this.state.isFirstPageLoaded,
                      translate: this.props.translate,
                      // eslint-disable-next-line max-len
                      isBoxNameEmpty: _.every(this.state.values.tableItems, ({ boxName }) => !boxName),
                      // eslint-disable-next-line max-len
                      isPalletNameEmpty: _.every(this.state.values.tableItems, ({ palletName }) => !palletName),
                      formatLocalizedDate: this.props.formatLocalizedDate,
                    }))}
                </div>
              </div>
            </form>
          )}
        />
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  currentLocationId: state.session.currentLocation.id,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  locale: state.session.activeLanguage,
  isUserAdmin: state.session.isUserAdmin,
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  isPaginated: state.session.isPaginated,
  pageSize: state.session.pageSize,
  minimumExpirationDate: state.session.minimumExpirationDate,
  formatLocalizedDate: formatDate(state.localize),
});

export default withRouter(connect(mapStateToProps, { showSpinner, hideSpinner })(SendMovementPage));

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
  /** Return true if show only */
  showOnly: PropTypes.bool.isRequired,
  pageSize: PropTypes.number.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  formatLocalizedDate: PropTypes.func.isRequired,
};
