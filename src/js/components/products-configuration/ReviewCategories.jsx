import React, { Component } from 'react';

import fileDownload from 'js-file-download';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import apiClient from 'utils/apiClient';
import Translate from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const INITIAL_STATE = {
  categoriesCount: 0,
};


class ReviewCategories extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  componentDidMount() {
    this.getCategoriesCount();
  }

  getCategoriesCount() {
    this.props.showSpinner();
    apiClient.get('/openboxes/api/productsConfiguration/categoriesCount')
      .then((response) => {
        this.setState(
          { categoriesCount: response.data ? response.data.data : 0 },
          () => this.props.hideSpinner(),
        );
      })
      .catch(() => this.props.hideSpinner());
  }

  downloadCategories = () => {
    this.props.showSpinner();
    apiClient.get('/openboxes/api/productsConfiguration/downloadCategories')
      .then((response) => {
        fileDownload(response.data, 'Categories.csv', 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const {
      initialValues, previousPage, nextPage,
    } = this.props;
    const { categoriesCount } = this.state;

    return (
      <div className="d-flex flex-column">
        <div className="configuration-wizard-content">
          <div className=" configuration-wizard-card">
            {categoriesCount > 0 ? (
              <div className="d-flex flex-column justify-content-middle align-items-center col-6 offset-3">
                <h3>
                  <Translate id="react.productsConfiguration.importSuccess.label" defaultMessage="Import Complete!" />
                </h3>
                <span style={{ textAlign: 'center' }}>
                  <Translate id="react.productsConfiguration.successfulCategoriesImport.message" />
                </span>
                <button type="button" onClick={this.downloadCategories} className="btn btn-outline-primary float-right mt-3">
                  <Translate id="react.productsConfiguration.downloadCsvFile.label" defaultMessage="Download CSV file" />
                </button>
                <a href="/openboxes/category/tree" className="btn btn-primary float-right mt-3" target="_blank">
                  <Translate id="react.productsConfiguration.viewCategoryPage.label" defaultMessage="View Category Page" />
                </a>
              </div>) : (
                <div className="d-flex flex-column justify-content-middle align-items-center col-6 offset-3">
                  <h3>
                    <Translate id="react.productsConfiguration.missingCategories.title" defaultMessage="No categories found" />
                  </h3>
                  <span style={{ textAlign: 'center' }}>
                    <Translate id="react.productsConfiguration.missingCategories.message" />
                  </span>
                </div>
            )}
          </div>
        </div>
        <div className="submit-buttons">
          <button type="button" onClick={() => previousPage(initialValues)} className="btn btn-outline-primary float-left btn-xs">
            <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
          </button>
          <button type="button" onClick={() => nextPage(initialValues)} className="btn btn-outline-primary float-right btn-xs">
            <Translate id="react.default.button.next.label" defaultMessage="Next" />
          </button>
        </div>
      </div>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(ReviewCategories);

ReviewCategories.propTypes = {
  hideSpinner: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({}).isRequired,
  nextPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
};
