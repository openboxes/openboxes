import React, { Component } from 'react';

import PropTypes from 'prop-types';
import ReactHtmlParser from 'react-html-parser';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import apiClient from 'utils/apiClient';
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
      <Translate id="react.loadData.welcomeDescription.label" options={{ renderInnerHtml: true }} />
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
    <div className="d-flex justify-content-between align-items-center m-3">
      <a href="#" onClick={skipConfiguration} className="btn btn-link">
        <Translate id="react.loadData.skipForNow.label" defaultMessage="Skip for now" />
      </a>
      <button disabled={!selectedLocationDataOption} type="button" onClick={nextStep} className="btn btn-outline-primary">
        <Translate id="default.button.next.label" defaultMessage="Next" />
      </button>
    </div>
  </React.Fragment>
);


const LoadDataContent = ({
  stepBackHandler,
  proceedHandler,
  summaryItemsTitle,
  summaryItemsList,
}) => (
  <React.Fragment>
    <h3 className="font-weight-bold my-3">
      <Translate id="react.loadData.loadDataHeader.label" defaultMessage="Welcome to OpenBoxes!" />
    </h3>
    <p className="my-3">
      <Translate id="react.loadData.loadDataDescription.label" />
    </p>
    <div>
      <h5 className="font-weight-bold my-3">{summaryItemsTitle}:</h5>
      {ReactHtmlParser(summaryItemsList)}
    </div>
    <div className="d-flex justify-content-between m-3">
      <button type="button" onClick={stepBackHandler} className="btn btn-outline-primary">
        <Translate id="default.button.back.label" defaultMessage="Back" />
      </button>
      <button type="button" onClick={proceedHandler} className="btn btn-outline-primary">
        <Translate id="default.button.proceed.label" defaultMessage="Proceed" />
      </button>
    </div>
  </React.Fragment>
);

class LoadDataPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedLocationDataOption: null,
      showLoadData: false,
      summaryData: { title: '', description: '' },
    };

    this.skipConfiguration = this.skipConfiguration.bind(this);
    this.selectLocationDataHandler = this.selectLocationDataHandler.bind(this);
    this.nextStepHandler = this.nextStepHandler.bind(this);
    this.proceedHandler = this.proceedHandler.bind(this);
    this.stepBackHandler = this.stepBackHandler.bind(this);
  }
  componentDidMount() {
    this.props.fetchTranslations('', 'loadData');

    apiClient.get('/openboxes/api/loadData/listOfDemoData')
      .then((response) => {
        this.setState({ summaryData: response.data.data });
      });
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

  stepBackHandler() {
    this.setState({ showLoadData: false, selectedLocationDataOption: null });
  }

  proceedHandler() {
    // TODO: implement proceed loading data
    this.props.history.push('/openboxes');
  }

  nextStepHandler() {
    switch (this.state.selectedLocationDataOption) {
      case LOAD_DATA_OPTIONS.createFirstLocation:
        this.props.history.push('/openboxes/locationsConfiguration/create');
        break;
      case LOAD_DATA_OPTIONS.loadDemoData:
        this.setState({ showLoadData: true });
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
          {
            this.state.showLoadData
              ? <LoadDataContent
                proceedHandler={this.proceedHandler}
                stepBackHandler={this.stepBackHandler}
                summaryItemsTitle={this.state.summaryData.title}
                summaryItemsList={this.state.summaryData.description}
              />
              : <WelcomeContent
                selectedLocationDataOption={this.state.selectedLocationDataOption}
                skipConfiguration={this.skipConfiguration}
                selectOption={this.selectLocationDataHandler}
                nextStep={this.nextStepHandler}
              />
          }
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
})(LoadDataPage));

LoadDataContent.propTypes = {
  proceedHandler: PropTypes.func.isRequired,
  stepBackHandler: PropTypes.func.isRequired,
  summaryItemsTitle: PropTypes.string.isRequired,
  summaryItemsList: PropTypes.string.isRequired,
};

WelcomeContent.propTypes = {
  selectedLocationDataOption: PropTypes.string.isRequired,
  skipConfiguration: PropTypes.string.isRequired,
  nextStep: PropTypes.string.isRequired,
  selectOption: PropTypes.number.isRequired,
};

LoadDataPage.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
