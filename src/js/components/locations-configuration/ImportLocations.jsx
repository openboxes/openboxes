import React, { Component } from 'react';

import axios from 'axios';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import { LOCATION_IMPORT, LOCATION_TEMPLATE, SUPPORT_LINKS } from 'api/urls';
import FileSelect from 'components/form-elements/v2/FileSelect';
import { DASHBOARD_URL, LOCATION_URL, PRODUCT_CONFIGURATION_URL } from 'consts/applicationUrls';
import AlertMessage from 'utils/AlertMessage';
import { handleError, handleSuccess } from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

const apiClient = axios.create({});

class ImportLocations extends Component {
  constructor(props) {
    super(props);

    this.state = {
      file: null,
      alertMessage: '',
      showAlert: false,
      showSuccessMessage: false,
      supportLinks: {},
    };

    this.onDrop = this.onDrop.bind(this);
    this.handleValidationErrors = this.handleValidationErrors.bind(this);
    this.getSupportLinks = this.getSupportLinks.bind(this);

    apiClient.interceptors.response.use(handleSuccess, this.handleValidationErrors);
  }

  componentDidMount() {
    this.getSupportLinks();
  }

  onDrop(file) {
    this.setState({ file });
  }

  getSupportLinks() {
    apiClient.get(SUPPORT_LINKS).then((response) => {
      const supportLinks = response.data.data;
      this.setState({ supportLinks });
    });
  }

  handleValidationErrors(error) {
    if (error.response.status === 400) {
      const errorMessage = _.get(error, 'response.data.errorMessage', '').split('\n');
      const errorMessages = _.get(error, 'response.data.errorMessages', '');
      const alertMessage = `${errorMessage.length > 0 ? errorMessage[0] : ''} \n ${_.join(errorMessages, '\n')}`;
      this.setState({ alertMessage, showAlert: true });

      return Promise.reject(error);
    }

    return handleError(error);
  }

  importLocations() {
    this.props.showSpinner();
    const formData = new FormData();

    formData.append('importFile', this.state.file.slice(0, this.state.file.size, 'text/csv'));
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };

    return apiClient.post(LOCATION_IMPORT, formData, config)
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.locationsConfiguration.importSuccess.label', 'Locations Created Successfully'));
        this.setState({ showSuccessMessage: true });
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  downloadLocationsTemplate() {
    this.props.showSpinner();
    apiClient.get(LOCATION_TEMPLATE)
      .then((response) => {
        fileDownload(response.data, 'Locations_template.csv', 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <div className="content-wrap">
        <div className="d-flex flex-column p-5 m-4 classic-card">
          {this.state.showSuccessMessage ? (
            <>
              <h3 className="align-self-center">
                <Translate
                  id="react.locationsConfiguration.importSuccess.label"
                  defaultMessage="Locations Created Successfully"
                />
              </h3>
              <div className="align-self-center">
                <Translate
                  id="react.locationsConfiguration.importSuccess.message"
                  defaultMessage="Your locations have been created. You can review or edit them at any time from the location list page."
                />
              </div>
              <a
                type="button"
                className="btn btn-outline-primary align-self-center w-auto mt-5"
                href={LOCATION_URL.list()}
              >
                <Translate id="react.locationsConfiguration.viewLocations.label" defaultMessage="View Location List" />
              </a>
              <Link
                type="button"
                className="btn btn-outline-primary align-self-center w-auto mt-3"
                to={PRODUCT_CONFIGURATION_URL.index()}
              >
                <Translate id="react.locationsConfiguration.productWizard.label" defaultMessage="Product Creation Wizard" />
              </Link>
              <Link
                className="align-self-center w-auto mt-3"
                to={DASHBOARD_URL.base}
              >
                <Translate id="react.locationsConfiguration.exitToDashboard.label" defaultMessage="Exit to Dashboard" />
              </Link>
            </>
          ) : (
            <>
              <h3><Translate id="react.locationsConfiguration.importLocationCsv.label" defaultMessage="Import Locations" /></h3>
              <div>
                <Translate
                  id="react.locationsConfiguration.csvUpload.label"
                  defaultMessage="On this screen, you can import a list of locations from .csv file."
                />
&nbsp;
                <Translate
                  id="react.locationsConfiguration.click.label"
                  defaultMessage="Click"
                />
&nbsp;
                <a href="#" onClick={() => this.downloadLocationsTemplate()}>
                  <Translate id="react.locationsConfiguration.here.label" defaultMessage="here" />
                </a>
&nbsp;
                <Translate
                  id="react.locationsConfiguration.templateInstructions.label"
                  defaultMessage="to download a template with instructions."
                />
&nbsp;
                <Translate id="react.locationsConfiguration.click.label" defaultMessage="Click" />
&nbsp;
                <a target="_blank" rel="noopener noreferrer" href={this.state.supportLinks.configureOrganizationsAndLocations || '#'}>
                  <Translate id="react.locationsConfiguration.here.label" defaultMessage="here" />
                </a>
&nbsp;
                <Translate
                  id="react.locationsConfiguration.createInstructions.label"
                  defaultMessage="to read more about location creation."
                />
              </div>
              <FileSelect className="my-2 p-5" onChange={this.onDrop} allowedExtensions={['csv']} />
              <AlertMessage className="m-3" show={this.state.showAlert} message={this.state.alertMessage} danger />
              <div className="align-self-end mt-5">
                <button
                  type="button"
                  className="btn btn-primary float-right"
                  onClick={() => this.importLocations()}
                  disabled={!this.state.file}
                >
                  <Translate id="react.locationsConfiguration.upload.label" defaultMessage="Upload" />
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(ImportLocations);

ImportLocations.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
