import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import Translate from 'utils/Translate';

const LOAD_DATA_OPTIONS = {
  loadDemoData: 'LOAD_DEMO_DATA',
  createFirstLocation: 'CREATE_FIRST_LOCATION',
};

const WelcomeContent = ({
  selectedLocationDataOption,
  skipConfiguration,
  selectOption,
  nextStep,
}) => (
  <React.Fragment>
    <h3 className="font-weight-bold my-3">
      <Translate id="react.loadData.welcomeHeader.label" defaultMessage="Welcome to OpenBoxes!" />
    </h3>
    <p className="my-3">
      <Translate id="react.loadData.welcomeDescription.label" />
    </p>
    <div onChange={selectOption}>
      <div>
        <input
          name="location-data"
          type="radio"
          id={LOAD_DATA_OPTIONS.loadDemoData}
          value={LOAD_DATA_OPTIONS.loadDemoData}
          checked={selectedLocationDataOption === LOAD_DATA_OPTIONS.loadDemoData}
        />
        <label htmlFor={LOAD_DATA_OPTIONS.loadDemoData} className="font-weight-bold ml-1">
          <Translate id="react.loadData.loadDemoData.label" defaultMessage="Load Demo Data" />
        </label>
        <p className="ml-4">
          <Translate id="react.loadData.loadDemoData.description.label" />
        </p>
      </div>
      <div>
        <input
          name="location-data"
          type="radio"
          id={LOAD_DATA_OPTIONS.createFirstLocation}
          value={LOAD_DATA_OPTIONS.createFirstLocation}
          checked={selectedLocationDataOption === LOAD_DATA_OPTIONS.createFirstLocation}
        />
        <label htmlFor={LOAD_DATA_OPTIONS.createFirstLocation} className="font-weight-bold ml-1">
          <Translate id="react.loadData.createFirstLocation.label" defaultMessage="Create your First Location" />
        </label>
        <p className="ml-4">
          <Translate id="react.loadData.createFirstLocation.description.label" />
        </p>
      </div>
    </div>
    <div className="d-flex justify-content-between align-items-center">
      <a href="#" onClick={skipConfiguration} className="btn btn-link">
        <Translate id="react.loadData.skipForNow.label" defaultMessage="Skip for now" />
      </a>
      <button disabled={!selectedLocationDataOption} type="button" onClick={nextStep} className="btn btn-outline-primary">
        <Translate id="default.button.next.label" defaultMessage="Next" />
      </button>
    </div>
  </React.Fragment>
);


class LocationConfigurationModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedLocationDataOption: null,
    };

    this.skipConfiguration = this.skipConfiguration.bind(this);
    this.selectLocationDataHandler = this.selectLocationDataHandler.bind(this);
    this.nextStepHandler = this.nextStepHandler.bind(this);
  }
  componentDidMount() {
    this.props.fetchTranslations('', 'loadData');
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'loadData');
    }
  }

  selectLocationDataHandler(e) {
    this.setState({ selectedLocationDataOption: e.target.value });
  }

  skipConfiguration() {
    this.props.history.push('/openboxes');
  }

  nextStepHandler() {
    switch (this.state.selectedLocationDataOption) {
      case LOAD_DATA_OPTIONS.createFirstLocation:
        this.props.history.push('/openboxes/locationsConfiguration/create');
        break;
      case LOAD_DATA_OPTIONS.loadDemoData:
        this.props.history.push('/openboxes');
        break;
      default:
        break;
    }
  }

  render() {
    return (
      <div className="modal-page">
        <div className="modal-page__content position-relative">
          <button
            className="btn btn-lg position-absolute"
            style={{ right: '1rem' }}
            onClick={this.skipConfiguration}
          >
            <i className="fa fa-close" />
          </button>
          <WelcomeContent
            selectedLocationDataOption={this.state.selectedLocationDataOption}
            skipConfiguration={this.skipConfiguration}
            selectOption={this.selectLocationDataHandler}
            nextStep={this.nextStepHandler}
          />
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

export default withRouter(connect(mapStateToProps, {
  fetchTranslations,
})(LocationConfigurationModal));

WelcomeContent.propTypes = {
  selectedLocationDataOption: PropTypes.string.isRequired,
  skipConfiguration: PropTypes.string.isRequired,
  nextStep: PropTypes.string.isRequired,
  selectOption: PropTypes.number.isRequired,
};

LocationConfigurationModal.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
