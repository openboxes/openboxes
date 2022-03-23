import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions/index';
import VerticalTabs from 'components/Layout/VerticalTabs';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const INITIAL_STATE = {
  productOptions: {},
  importSuccessful: false,
};

const PAGE_ID = 'configureProducts';

const getImportProductFromExcel = () => (
  <div className="d-flex flex-column align-items-center p-5">
    <h3><Translate id="react.productsConfiguration.createProduct.label" defaultMessage="Learn how to create a product" /></h3>
    <div className="my-3">
      <Translate id="react.productsConfiguration.excelImportDescription.label" />
    </div>
    <div>
      <a className="btn btn-primary" target="_blank" rel="noopener noreferrer" href="/openboxes/product/importAsCsv">
        <Translate id="react.productsConfiguration.importProducts.label" defaultMessage="Import Products List" />
      </a>
    </div>
  </div>
);

class ConfigureProducts extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  componentDidMount() {
    this.props.showSpinner();

    const url = '/openboxes/api/productsConfiguration/productOptions';

    apiClient.get(url)
      .then((response) => {
        const productOptions = response.data.data;

        this.props.hideSpinner();

        this.setState({ productOptions });
      })
      .catch(() => this.props.hideSpinner());
  }

  getProductImportContent(product, productName) {
    if (this.state.importSuccessful) {
      return (
        <div className="d-flex flex-column align-items-center p-5">
          <h3>
            <Translate id="react.productsConfiguration.importSuccess.label" defaultMessage="Success Message!" />
          </h3>
          <div className="my-3">
            <Translate id="react.productsConfiguration.importSuccessDetails.label" />
          </div>
          <div>
            <a className="btn btn-primary" target="_blank" rel="noopener noreferrer" href="/openboxes/product/list">
              <Translate id="react.productsConfiguration.viewProducts.label" defaultMessage="View Products List" />
            </a>
          </div>
        </div>
      );
    }

    return (
      <div className="d-flex flex-column align-items-center p-5">
        <h3>{product.title}</h3>
        <div className="my-3">{product.description}</div>
        <div>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => this.importProduct(productName)}
          >
            <Translate id="react.productsConfiguration.importProducts.label" defaultMessage="Import Products List" />
          </button>
        </div>
      </div>
    );
  }

  getTabs() {
    const tabs = {};
    _.forEach(this.state.productOptions, (product, productName) => {
      tabs[product.title] = this.getProductImportContent(product, productName);
    });

    tabs[`${this.props.translate('react.productsConfiguration.importFromExcel.label', 'Import from Excel')}`] = getImportProductFromExcel();

    return tabs;
  }

  importProduct(productName) {
    this.props.showSpinner();
    const url = `/openboxes/api/productsConfiguration/importProducts?productOption=${productName}`;

    apiClient.post(url)
      .then(() => {
        this.props.hideSpinner();
        this.setState({ importSuccessful: true });
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const tabs = this.getTabs();

    return (
      <div className="d-flex flex-column">
        <div className="submit-buttons">
          <button type="button" onClick={() => Alert.info(this.props.supportLinks[PAGE_ID])} className="btn btn-outline-primary float-right btn-xs">
            <i className="fa fa-question-circle-o" aria-hidden="true" />
            &nbsp;
            <Translate id="react.default.button.support.label" defaultMessage="Support" />
          </button>
        </div>
        <div className="configuration-wizard-content">
          <VerticalTabs tabs={tabs} />
        </div>
        <div className="submit-buttons">
          <button type="button" onClick={this.props.previousPage} className="btn btn-outline-primary float-left btn-xs">
            <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(ConfigureProducts);

ConfigureProducts.propTypes = {
  previousPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
