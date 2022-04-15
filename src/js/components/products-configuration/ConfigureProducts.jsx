import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import ReactHtmlParser from 'react-html-parser';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions/index';
import VerticalTabs from 'components/Layout/VerticalTabs';
import ImportProducts from 'components/products-configuration/ImportProducts';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const INITIAL_STATE = {
  productOptions: {},
  importSuccessful: false,
};


class ConfigureProducts extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;

    this.prevPage = this.prevPage.bind(this);
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
            <Translate id="react.productsConfiguration.importSuccess.label" defaultMessage="Import Complete!" />
          </h3>
          <div className="my-3">
            <Translate id="react.productsConfiguration.importSuccessDetails.label" />
          </div>
          <div className="my-3">
            <Translate id="react.productsConfiguration.productListInfo1.label" />&nbsp;
            <a className="font-weight-bold" target="_blank" rel="noopener noreferrer" href="https://openboxes.helpscoutdocs.com/article/27-product-configuration-basics">
              <Translate id="react.productsConfiguration.basics.label" defaultMessage="Products Configuration Basics" />
            </a>&nbsp;
            <Translate id="react.productsConfiguration.and.label" />&nbsp;
            <a className="font-weight-bold" target="_blank" rel="noopener noreferrer" href="https://openboxes.helpscoutdocs.com/article/37-create-a-product">
              <Translate id="react.productsConfiguration.createProduct.label" defaultMessage="Create a Product" />
            </a>.&nbsp;
            <Translate id="react.productsConfiguration.productListInfo2.label" />
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
      <div className="d-flex flex-column p-5">
        <h3>{product.title}</h3>
        <div className="my-3">{ReactHtmlParser(product.description)}</div>
        <div className="align-self-center">
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

    tabs[`${this.props.translate('react.productsConfiguration.importFromExcel.label', 'Import from Excel')}`] = <ImportProducts />;

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

  prevPage() {
    if (this.props.initialValues.categoriesImported) {
      this.props.previousPage(this.props.initialValues);
    } else {
      this.props.goToPage(1, this.props.initialValues);
    }
  }

  render() {
    const tabs = this.getTabs();

    return (
      <div className="d-flex flex-column">
        <div className="configuration-wizard-content">
          <VerticalTabs tabs={tabs} />
        </div>
        <div className="submit-buttons">
          <button type="button" onClick={this.prevPage} className="btn btn-outline-primary float-left btn-xs">
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
  goToPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({
    categoriesImported: PropTypes.bool.isRequired,
  }).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
