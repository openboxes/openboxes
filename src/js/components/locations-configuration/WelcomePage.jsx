import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import Translate from 'utils/Translate';

class WelcomePage extends Component {
  componentDidMount() {
    this.props.fetchTranslations('', 'locationsConfiguration');
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'locationsConfiguration');
    }
  }

  skipConfiguration() {
    this.props.history.push('/openboxes');
  }

  createLocation() {
    this.props.history.push('/openboxes/locationsConfiguration/create');
  }

  importLocations() {
    this.props.history.push('/openboxes/locationsConfiguration/upload');
  }

  render() {
    return (
      <div className="modal-page">
        <div className="modal-page__content position-relative">
          <button
            className="btn btn-lg position-absolute"
            style={{ right: '1rem' }}
            onClick={() => this.skipConfiguration()}
          >
            <i className="fa fa-close" />
          </button>
          <h3 className="font-weight-bold text-center my-3">
            <Translate id="react.locationsConfiguration.welcomeHeader.label" defaultMessage="Welcome to OpenBoxes!" />
          </h3>
          <div className="my-3 px-5 text-center">
            <Translate id="react.locationsConfiguration.modalIntro.label" defaultMessage="Learn more about locations in OpenBoxes" />&nbsp;
            <a target="_blank" rel="noopener noreferrer" href="https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1291452471/Configure+Organizations+and+Locations">
              <Translate id="react.locationsConfiguration.here.label" defaultMessage="here" />
            </a>.&nbsp;
            <Translate id="react.locationsConfiguration.modalText1.label" defaultMessage="" />
            <span className="font-weight-bold"> <Translate id="react.locationsConfiguration.locationCreation.label" defaultMessage="" />, </span>
            <Translate id="react.locationsConfiguration.modalText2.label" defaultMessage="" />
            <span className="font-weight-bold"> &ldquo;<Translate id="react.locationsConfiguration.importLocations.label" defaultMessage="" />&rdquo; </span>
            <Translate id="react.locationsConfiguration.modalText3.label" defaultMessage="" />
          </div>
          <div className="d-flex justify-content-center align-items-center my-3">
            <button type="button" onClick={() => this.createLocation()} className="btn btn-outline-primary mr-3">
              <Translate id="react.locationsConfiguration.createLocation.label" defaultMessage="Create your first location" />
            </button>
            <button type="button" onClick={() => this.importLocations()} className="btn btn-outline-primary">
              <Translate id="react.locationsConfiguration.importLocations.label" defaultMessage="Import Location List" />
            </button>
          </div>
          <div className="align-self-end">
            <button type="button" onClick={() => this.skipConfiguration()} className="btn btn-link">
              <Translate id="react.locationsConfiguration.skipConfiguration.label" defaultMessage="Skip Configuration for now" />
            </button>
          </div>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

export default withRouter(connect(mapStateToProps, { fetchTranslations })(WelcomePage));

WelcomePage.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
