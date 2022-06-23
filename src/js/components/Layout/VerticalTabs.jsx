import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import Translate from 'utils/Translate';


class VerticalTabs extends Component {
  constructor(props) {
    super(props);
    this.state = {
      activeTab: '',
    };
  }

  componentWillReceiveProps(nextProps) {
    if (Object.keys(this.props.tabs).length !== Object.keys(nextProps.tabs).length) {
      this.setState({ activeTab: Object.keys(nextProps.tabs)[0] });
    }
  }

  getTabTitles() {
    return _.map(Object.keys(this.props.tabs), tabTitle => (
      <div
        role="button"
        className={`p-3 d-flex justify-content-start align-items-center ${this.isActive(tabTitle) ? 'active ' : ''}`}
        onClick={() => this.setState({ activeTab: tabTitle })}
        onKeyPress={() => this.setState({ activeTab: tabTitle })}
        tabIndex={0}
      >
        <i className={`fa tab-title-icon ${this.isActive(tabTitle) ? 'fa-dot-circle-o active ' : 'fa-circle-o'}`} />
        <Translate id={tabTitle} defaultMessage={tabTitle} />
      </div>
    ));
  }

  isActive(tabTitle) {
    return tabTitle === this.state.activeTab;
  }

  render() {
    return (
      <div className="d-flex w-100 h-100">
        <div className="vartical-tabs col-3 pl-0 mb-0">
          {this.getTabTitles()}
        </div>
        <div className="vertical-tabs-content col-9">
          {this.props.tabs[this.state.activeTab]}
        </div>
      </div>
    );
  }
}

export default VerticalTabs;

VerticalTabs.propTypes = {
  tabs: PropTypes.func.isRequired,
};
