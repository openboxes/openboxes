import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import VerticalTabs from 'components/Layout/VerticalTabs';
import ImportCategories from 'components/products-configuration/ImportCategories';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const INITIAL_STATE = {
  categoryOptions: {},
};

const PAGE_ID = 'configureCategories';

class ConfigureProductCategories extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  componentDidMount() {
    this.props.showSpinner();

    const url = '/openboxes/api/productsConfiguration/categoryOptions';

    apiClient.get(url)
      .then((response) => {
        const categoryOptions = response.data.data;

        this.props.hideSpinner();

        this.setState({ categoryOptions });
      })
      .catch(() => this.props.hideSpinner());
  }

  getCategoryTreeContent(category, categoryName) {
    return (
      <div className="d-flex flex-column align-items-center p-5">
        <h3>{category.title}</h3>
        <div className="my-3">{category.description}</div>
        <div>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => this.importCategory(categoryName)}
          >
            <Translate id="react.productsConfiguration.importCategories.label" defaultMessage="Import Categories" />
          </button>
        </div>
      </div>
    );
  }

  getTabs() {
    const tabs = {};
    _.forEach(this.state.categoryOptions, (category, categoryName) => {
      tabs[category.title] = this.getCategoryTreeContent(category, categoryName);
    });

    tabs[`${this.props.translate('react.productsConfiguration.importFromExcel.label', 'Import from Excel')}`] = <ImportCategories />;

    return tabs;
  }

  importCategory(categoryName) {
    this.props.showSpinner();
    const url = `/openboxes/api/productsConfiguration/importCategories?categoryOption=${categoryName}`;

    apiClient.post(url)
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.productsConfiguration.importSuccessful.label', 'Categories imported successfully'));
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
          <button type="button" onClick={this.props.nextPage} className="btn btn-outline-primary float-right btn-xs">
            <Translate id="react.default.button.next.label" defaultMessage="Next" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(ConfigureProductCategories);

ConfigureProductCategories.propTypes = {
  nextPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
