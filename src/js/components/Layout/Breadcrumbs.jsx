/* eslint-disable react/no-array-index-key */
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { getTranslate } from 'react-localize-redux';
import PropTypes from 'prop-types';
import { showLocationChooser } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

class Breadcrumbs extends Component {
  constructor(props) {
    super(props);

    this.openModal = this.openModal.bind(this);
  }

  openModal() {
    this.props.showLocationChooser();
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
          onClick={() => { this.openModal(); }}
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

export default connect(mapStateToProps, { showLocationChooser })(Breadcrumbs);

Breadcrumbs.propTypes = {
  currentLocationName: PropTypes.string.isRequired,
  // Function called to show the location chooser modal
  showLocationChooser: PropTypes.func.isRequired,
  breadcrumbsParams: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string,
    url: PropTypes.string.isRequired,
  })).isRequired,
  translate: PropTypes.func.isRequired,
};
