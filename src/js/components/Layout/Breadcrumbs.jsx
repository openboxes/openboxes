/* eslint-disable react/no-array-index-key */
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { getTranslate } from 'react-localize-redux';
import PropTypes from 'prop-types';
import { showLocationChooser } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';
import { stringUrlInterceptor } from '../../utils/apiClient';

class Breadcrumbs extends Component {
  constructor(props) {
    super(props);

    this.openModal = this.openModal.bind(this);
  }

  openModal() {
    this.props.showLocationChooser();
  }

  render() {
    const separatorUrl = stringUrlInterceptor('static/images/bc_separator.png');
    const houseUrl = stringUrlInterceptor('static/images/skin/house.png');

    const listToReturn = this.props.breadcrumbsParams.map((value, id) =>
      (value.label === 'Openboxes' || value.label === '' ? null : (
        <a key={`item-${id}`} href={stringUrlInterceptor(value.url)} className="item-breadcrumbs">
          {value.defaultLabel ? this.props.translate(value.label, value.defaultLabel) : value.label}
          <img className="item-breadcrumbs" alt="/" src={separatorUrl} />
        </a>
      )));

    return (
      <div className="breadcrumbs-container d-flex">
        <a className="item-breadcrumbs" href={stringUrlInterceptor('/')}>
          <img alt="Breadcrumbs" src={houseUrl} />
        </a>
        <img className="item-breadcrumbs" alt="/" src={separatorUrl} />
        <a
          role="button"
          href="#"
          onClick={() => { this.openModal(); }}
          className="item-breadcrumbs"
        > {this.props.currentLocationName}
        </a>
        <img className="item-breadcrumbs" alt="/" src={separatorUrl} />
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
