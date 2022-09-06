import React from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { translateWithDefaultMessage } from 'utils/Translate';

const Breadcrumbs = ({ currentLocationName, breadcrumbsParams, translate }) => {
  const listToReturn = breadcrumbsParams.map(value =>
    (value.label === 'Openboxes' || value.label === '' ? null : (
      <a key={`item-${value.label}`} href={value.url} className="item-breadcrumbs">
        {value.defaultLabel ? translate(value.label, value.defaultLabel) : value.label}
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
      > {currentLocationName}
      </a>
      <img className="item-breadcrumbs" alt="/" src="/openboxes/images/bc_separator.png" />
      { listToReturn }
    </div>
  );
};

const mapStateToProps = state => ({
  currentLocationName: state.session.currentLocation.name,
  breadcrumbsParams: state.session.breadcrumbsParams,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps)(Breadcrumbs);

Breadcrumbs.propTypes = {
  currentLocationName: PropTypes.string.isRequired,
  breadcrumbsParams: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string,
    url: PropTypes.string.isRequired,
  })).isRequired,
  translate: PropTypes.func.isRequired,
};
