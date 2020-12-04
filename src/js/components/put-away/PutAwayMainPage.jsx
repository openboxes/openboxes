import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { getTranslate } from 'react-localize-redux';

import PutAwayPage from './PutAwayPage';
import PutAwaySecondPage from './PutAwaySecondPage';
import PutAwayCheckPage from './PutAwayCheckPage';
import Wizard from '../wizard/Wizard';
import apiClient, { parseResponse } from '../../utils/apiClient';
import { showSpinner, hideSpinner, fetchTranslations } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

import './PutAway.scss';
/** Main put-away form's component. */
class PutAwayMainPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: props.match.params.putAwayId ? 2 : 1,
      putAway: {},
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'putAway');

    if (this.props.putAwayTranslationsFetched) {
      this.dataFetched = true;

      this.fetchPutAway();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'putAway');
    }

    if (nextProps.putAwayTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchPutAway();
    }
  }

  /**
   * @public
   */
  getStepList() {
    const stepList = [this.props.translate('react.putAway.createPutAway.label', 'Create Putaway'),
      this.props.translate('react.putAway.startPutAway.label', 'Start Putaway'),
      this.props.translate('react.putAway.completePutAway.label', 'Complete Putaway'),
    ];
    return stepList;
  }

  getWizardTitle() {
    const { putAway } = this.state;
    const newName = putAway && putAway.putAway ? `Putaway - ${putAway.putAway.putawayNumber}` : '';
    return newName;
  }

  updateWizardValues(page, putAway) {
    this.setState({ page, putAway });
  }

  dataFetched = false;


  fetchPutAway() {
    if (this.props.match.params.putAwayId) {
      this.props.showSpinner();

      const url = `/openboxes/api/putaways/${this.props.match.params.putAwayId}`;

      apiClient.get(url)
        .then((response) => {
          const putAway = parseResponse(response.data.data);

          this.props.hideSpinner();

          this.setState({ putAway: { putAway }, page: putAway.putawayStatus === 'COMPLETED' ? 3 : 2 });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { page, putAway } = this.state;
    const { location, history, match } = this.props;
    const locationId = location.id;
    const title = this.getWizardTitle();
    const additionalTitle = null;
    const pageList = [PutAwayPage, PutAwaySecondPage, PutAwayCheckPage];
    const stepList = this.getStepList();

    if (_.get(location, 'id')) {
      return (
        <Wizard
          pageList={pageList}
          stepList={stepList}
          initialValues={putAway}
          title={title}
          additionalTitle={additionalTitle}
          currentPage={page}
          prevPage={page === 1 ? 1 : page - 1}
          updateWizardValues={this.updateWizardValues}
          additionalProps={{
            locationId, location, history, match,
          }}
        />
      );
    }

    return null;
  }
}

const mapStateToProps = state => ({
  location: state.session.currentLocation,
  locale: state.session.activeLanguage,
  putAwayTranslationsFetched: state.session.fetchedTranslations.putAway,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(PutAwayMainPage));

PutAwayMainPage.propTypes = {
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  locale: PropTypes.string.isRequired,
  putAwayTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ putAwayId: PropTypes.string }),
  }).isRequired,
  /** React router's object used to manage session history */
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
