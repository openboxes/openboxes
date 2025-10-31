import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import LoadDemoDataInfo from 'components/load-demo-data/LoadDemoDataInfo';
import LoadDemoDataProgressScreen from 'components/load-demo-data/LoadDemoDataProgressScreen';
import LoadDemoDataWelcome from 'components/load-demo-data/LoadDemoDataWelcome';
import { DASHBOARD_URL, LOCATION_CONFIGURATION_URL } from 'consts/applicationUrls';
import LOAD_DATA_STEPS from 'consts/loadDataStep';
import apiClient from 'utils/apiClient';

class LoadDemoDataPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentStep: null,
      summaryData: { title: '', description: '' },
    };

    this.skipConfiguration = this.skipConfiguration.bind(this);
    this.stepHandler = this.stepHandler.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'loadData');

    apiClient.get('/api/loadData/listOfDemoData')
      .then((response) => {
        this.setState({ summaryData: response.data.data });
      });
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'loadData');
    }
  }

  skipConfiguration() {
    this.props.history.push(DASHBOARD_URL.base);
  }

  stepHandler(step) {
    switch (step) {
      case LOAD_DATA_STEPS.createFirstLocation:
        this.props.history.push(LOCATION_CONFIGURATION_URL.create());
        break;
      default:
        this.setState({ currentStep: step });
        break;
    }
  }

  render() {
    return (
      <div className="modal-page">
        <div className="modal-page__content position-relative" style={{ minHeight: '450px' }}>
          <button
            type="button"
            className="btn btn-lg position-absolute"
            style={{ right: '1rem' }}
            onClick={this.skipConfiguration}
          >
            <i className="fa fa-close" />
          </button>
          {
            !this.state.currentStep
            && (
            <LoadDemoDataWelcome
              skipConfiguration={this.skipConfiguration}
              goToStep={this.stepHandler}
            />
            )
          }
          {
            this.state.currentStep === LOAD_DATA_STEPS.loadDemoData
            && (
            <LoadDemoDataInfo
              summaryItemsTitle={this.state.summaryData.title}
              summaryItemsList={this.state.summaryData.description}
              goToStep={this.stepHandler}
            />
            )
          }
          {
            this.state.currentStep === LOAD_DATA_STEPS.proceedLoadingDemoData
              && <LoadDemoDataProgressScreen goToStep={this.stepHandler} />
          }
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  locale: state.session.activeLanguage,
});

LoadDemoDataPage.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};

export default withRouter(connect(mapStateToProps, {
  fetchTranslations,
})(LoadDemoDataPage));
