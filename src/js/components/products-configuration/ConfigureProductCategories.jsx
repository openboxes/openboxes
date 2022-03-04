import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import Translate, { translateWithDefaultMessage } from '../../utils/Translate';
import VerticalTabs from '../Layout/VerticalTabs';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

const INITIAL_STATE = {
  categoryOptions: [],
};

const PAGE_ID = 'configureCategories';

function getCategoryTreeContent(title) {
  return (
    <div className="d-flex justify-content-center p-5">
      <h3>{title}</h3>
    </div>
  );
}

function getImportFromExcelTabContent() {
  return (
    <div className="d-flex justify-content-center p-5">
      <h3><Translate id="react.productsConfiguration.importFromExcel.label" defaultMessage="Import from Excel" /></h3>
    </div>
  );
}

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

  getTabs() {
    const tabs = {};

    _.forEach(this.state.categoryOptions, (category) => {
      tabs[category.title] = getCategoryTreeContent(category.title);
    });

    tabs[`${this.props.translate('react.productsConfiguration.importFromExcel.label', 'Import from Excel')}`] = getImportFromExcelTabContent();

    return tabs;
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
