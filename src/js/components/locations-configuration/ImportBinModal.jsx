import React, { Component } from 'react';

import fileDownload from 'js-file-download';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'components/stock-movement-wizard/StockMovement.scss';


class ImportBinModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      file: null,
      supportLinks: {},
    };
    this.onDrop = this.onDrop.bind(this);
    this.importBinLocation = this.importBinLocation.bind(this);
    this.getSupportLinks = this.getSupportLinks.bind(this);
    this.downloadBinLocationsTemplate = this.downloadBinLocationsTemplate.bind(this);
  }

  componentDidMount() {
    this.getSupportLinks();
  }

  onDrop(newFiles) {
    if (newFiles && newFiles.length) {
      this.setState({
        file: newFiles[0],
        supportLinks: {},
      });
    }
  }

  getSupportLinks() {
    const url = '/openboxes/api/supportLinks';

    apiClient.get(url).then((response) => {
      const supportLinks = response.data.data;
      this.setState({ supportLinks });
    });
  }

  downloadBinLocationsTemplate() {
    this.props.showSpinner();
    apiClient.get('/openboxes/api/locations/binLocations/template')
      .then((response) => {
        fileDownload(response.data, 'BinLocations_template.xls', 'text/xls');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  importBinLocation() {
    this.props.showSpinner();
    const formData = new FormData();
    formData.append('fileContents', this.state.file);

    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };

    const url = `/openboxes/api/locations/${this.props.locationId}/binLocations/import`;

    return apiClient.post(url, formData, config)
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('Bin Location imported successfully', 'Bin Location imported successfully'));
        this.props.onResponse();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  render() {
    return (
      <ModalWrapper
        onSave={this.importBinLocation}
        btnOpenClassName="btn btn-outline-primary add-zonebin-btn"
        btnOpenText="react.locationsConfiguration.importBin.label"
        btnOpenDefaultText="Import Bin Locations"
        btnOpenIcon="fa-download"
        title="react.locationsConfiguration.importBin.label"
        defaultTitleMessage="Import Bin Location"
        btnContainerClassName="d-flex justify-content-between"
        btnSaveClassName="btn btn-primary"
        btnCancelClassName="btn btn-outline-primary"
        btnSaveText="default.button.import.label"
        btnSaveDefaultText="Import"
        btnSaveDisabled={!this.state.file}
      >
        <div className="form-subtitle mb-lg-4">
          <Translate
            id="react.locationsConfiguration.importBinLocations.importInstruction1.label"
          />
          {' '}
          <a href="#" onClick={this.downloadBinLocationsTemplate}>
            <Translate
              id="react.locationsConfiguration.importBinLocations.here.label"
              defaultMessage="here"
            />
          </a>
          {'. '}
          <Translate
            id="react.locationsConfiguration.importBinLocations.importInstruction2.label"
            data={this.state.supportLinks}
            options={{ renderInnerHtml: true }}
          />
          <div className="dropzone-container align-self-stretch mt-3">
            <Dropzone
              onDrop={this.onDrop}
              className="dropzone-content"
            >
              <h4 className="mb-5">
                <Translate id="react.productsConfiguration.dragAndDrop.label" defaultMessage="Drag and drop file here." />
              </h4>
              <button type="button" className="btn btn-dropzone">
                <Translate id="react.productsConfiguration.openDialog.label" defaultMessage="OPEN FILE DIALOG" />
              </button>
            </Dropzone>
          </div>
          <div className="align-self-start">
            <span style={{ color: '#5D9531' }} className="font-weight-bold">
              <Translate id="react.productsConfiguration.acceptedFile.label" defaultMessage="Accepted File" />:&nbsp;
            </span>
            {this.state.file ? this.state.file.name : ''}
          </div>
        </div>
      </ModalWrapper>
    );
  }
}


const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
};

ImportBinModal.propTypes = {
  locationId: PropTypes.string.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  onResponse: PropTypes.func.isRequired,
};

export default connect(mapStateToProps, mapDispatchToProps)(ImportBinModal);
