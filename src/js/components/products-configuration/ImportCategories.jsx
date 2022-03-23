import React, { Component } from 'react';

import fileDownload from 'js-file-download';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

class ImportCategories extends Component {
  constructor(props) {
    super(props);

    this.state = { file: null };

    this.onDrop = this.onDrop.bind(this);
  }

  onDrop(newFiles) {
    if (newFiles && newFiles.length) {
      this.setState({
        file: newFiles[0],
      });
    }
  }

  importCategory() {
    this.props.showSpinner();
    const formData = new FormData();

    formData.append('importFile', this.state.file.slice(0, this.state.file.size, 'text/csv'));
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };

    const url = '/openboxes/api/productsConfiguration/importCategoryCsv';

    return apiClient.post(url, formData, config)
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.productsConfiguration.importSuccessful.label', 'Categories imported successfully'));
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  downloadCategoryTemplate() {
    this.props.showSpinner();
    apiClient.get('/openboxes/api/productsConfiguration/downloadCategoryTemplate')
      .then((response) => {
        fileDownload(response.data, 'Category_template.csv', 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <div className="d-flex flex-column p-5">
        <h3><Translate id="react.productsConfiguration.excelImport.label" defaultMessage="Excel Import" /></h3>
        <div>
          <Translate id="react.productsConfiguration.csvUpload.label" defaultMessage="Please upload .csv file. Download file template here:" />&nbsp;
          <a href="#" onClick={() => this.downloadCategoryTemplate()}>
            <Translate id="react.productsConfiguration.csvTemplate.label" defaultMessage=".csv file template" />
          </a>.&nbsp;
          <Translate id="react.productsConfiguration.csvReadMore.label" defaultMessage="You can read more on how to create a .csv file" />&nbsp;
          <a target="_blank" rel="noopener noreferrer" href="#">
            <Translate id="react.productsConfiguration.here.label" defaultMessage="here" />
          </a>
        </div>
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
        <div className="align-self-end mt-5">
          <button
            type="button"
            className="btn btn-primary float-right"
            onClick={() => this.importCategory()}
            disabled={!this.state.file}
          >
            <Translate id="react.productsConfiguration.importFile.label" defaultMessage="Import File" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(ImportCategories);

ImportCategories.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
