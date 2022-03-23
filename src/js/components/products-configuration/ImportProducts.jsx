import React, { Component } from 'react';

import fileDownload from 'js-file-download';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import apiClient from 'utils/apiClient';
import Translate from 'utils/Translate';

class ImportCategories extends Component {
  downloadProductTemplate() {
    this.props.showSpinner();
    apiClient.get('/openboxes/batch/downloadCsvTemplate?template=products.csv')
      .then((response) => {
        fileDownload(response.data, 'Product_template.csv', 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <div className="d-flex flex-column p-5">
        <h3><Translate id="react.productsConfiguration.createCustomProduct.label" defaultMessage="Create a Custom Product List" /></h3>
        <div className="my-3">
          <Translate id="react.productsConfiguration.excelImportDescription.label" />
        </div>
        <div>
          <a target="_blank" rel="noopener noreferrer" href="https://openboxes.helpscoutdocs.com/article/27-product-configuration-basics">
            <Translate id="react.productsConfiguration.basics.label" defaultMessage="Products Configuration Basics" />
          </a>
        </div>
        <div>
          <a target="_blank" rel="noopener noreferrer" href="https://openboxes.helpscoutdocs.com/article/37-create-a-product">
            <Translate id="react.productsConfiguration.createProduct.label" defaultMessage="Create a Product" />
          </a>
        </div>
        <div>
          <a target="_blank" rel="noopener noreferrer" href="https://openboxes.helpscoutdocs.com/article/30-import-products-from-excel">
            <Translate id="react.productsConfiguration.importProductsFromExcel.label" defaultMessage="Import Products form Excel" />
          </a>
        </div>
        <div className="my-3">
          <Translate id="react.productsConfiguration.downloadProductTemplate1.label" />&nbsp;
          <a href="#" onClick={() => this.downloadProductTemplate()}>
            <Translate id="react.productsConfiguration.here.label" defaultMessage="here" />
          </a>.&nbsp;
          <Translate id="react.productsConfiguration.downloadProductTemplate2.label" />&nbsp;
        </div>
        <div className="align-self-end">
          <a className="btn btn-primary" target="_blank" rel="noopener noreferrer" href="/openboxes/product/importAsCsv">
            <Translate id="react.productsConfiguration.importProducts.label" defaultMessage="Import Products List" />
          </a>
        </div>
      </div>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(ImportCategories);

ImportCategories.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
};
