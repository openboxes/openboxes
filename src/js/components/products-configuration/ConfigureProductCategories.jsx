import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';

import 'react-confirm-alert/src/react-confirm-alert.css';

import Translate from '../../utils/Translate';
import VerticalTabs from '../Layout/VerticalTabs';

const INITIAL_STATE = {};

const PAGE_ID = 'configureCategories';

function getDefaultCategoryTreeContent() {
  return (
    <div className="d-flex justify-content-center p-5">
      <h3><Translate id="react.productsConfiguration.defaultCategoryTree.label" defaultMessage="OpenBoxes default category tree" /></h3>
    </div>
  );
}

function getNSPSCCategoryTreeContent() {
  return (
    <div className="d-flex justify-content-center p-5">
      <h3><Translate id="react.productsConfiguration.unspscCategoryList.label" defaultMessage="NSPSC category list" /></h3>
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

  render() {
    const tabs = {
      'react.productsConfiguration.defaultCategoryTree.label': getDefaultCategoryTreeContent(),
      'react.productsConfiguration.unspscCategoryList.label': getNSPSCCategoryTreeContent(),
      'react.productsConfiguration.importFromExcel.label': getImportFromExcelTabContent(),
    };

    return (
      <div className="d-flex flex-column">
        <div className="submit-buttons">
          <button type="button" onClick={() => Alert.info(this.props.supportLinks[PAGE_ID])} className="btn btn-outline-primary float-right btn-xs">
            <i className="fa fa-question-circle-o" aria-hidden="true" />
            &nbsp;
            <Translate id="react.default.button.support.label" defaultMessage="Support" />
          </button>
        </div>
        <div className="products-configuration-content">
          <VerticalTabs tabs={tabs} activeTab="react.productsConfiguration.defaultCategoryTree.label" />
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

export default ConfigureProductCategories;

ConfigureProductCategories.propTypes = {
  nextPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
};
