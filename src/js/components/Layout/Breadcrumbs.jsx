/* eslint-disable react/no-array-index-key */
import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { translateWithDefaultMessage } from 'utils/Translate';

// eslint-disable-next-line react/prefer-stateless-function
class Breadcrumbs extends Component {
  // eslint-disable-next-line no-useless-constructor
  constructor(props) {
    super(props);
  }


  render() {
    const listToReturn = this.props.breadcrumbsParams.map((value, id) =>
      (value.label === 'Openboxes' || value.label === '' ? null : (
        <a key={`item-${id}`} href={value.url} className="item-breadcrumbs">
          {value.defaultLabel ? this.props.translate(value.label, value.defaultLabel) : value.label}
          <img className="item-breadcrumbs" alt="/" src="/openboxes/images/bc_separator.png" />
        </a>
      )));

    return (
      <div className="breadcrumbs-container d-flex">
        <a className="item-breadcrumbs" href="/openboxes">
          <img alt="Breadcrumbs" src="/openboxes/images/skin/house.png" />
        </a>
        <img className="item-breadcrumbs" alt="/" src="/openboxes/images/bc_separator.png" />
        <a
          role="button"
          href="#"
          className="item-breadcrumbs"
        > {this.props.currentLocationName}
        </a>
        <img className="item-breadcrumbs" alt="/" src="/openboxes/images/bc_separator.png" />
        { listToReturn }
      </div>
    );
  }
}

const mapStateToProps = state => ({
  currentLocationName: state.session.currentLocation.name,
  breadcrumbsParams: state.session.breadcrumbsParams,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { })(Breadcrumbs);

Breadcrumbs.propTypes = {
  currentLocationName: PropTypes.string.isRequired,
  breadcrumbsParams: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string,
    url: PropTypes.string.isRequired,
  })).isRequired,
  translate: PropTypes.func.isRequired,
};
