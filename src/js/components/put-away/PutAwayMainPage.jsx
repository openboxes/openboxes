import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
import PutAwayCheckPage from 'components/put-away/PutAwayCheckPage';
import PutAwayPage from 'components/put-away/PutAwayPage';
import PutAwaySecondPage from 'components/put-away/PutAwaySecondPage';
import Wizard from 'components/wizard/Wizard';
import apiClient, { parseResponse } from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/put-away/PutAway.scss';

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
  get stepList() {
    return [
      this.props.translate('react.putAway.createPutAway.label', 'Create Putaway'),
      this.props.translate('react.putAway.startPutAway.label', 'Start Putaway'),
      this.props.translate('react.putAway.completePutAway.label', 'Complete Putaway'),
    ];
  }

  get wizardTitle() {
    const { putAway } = this.state;
    if (putAway?.putAway?.putawayNumber) {
      return [
        {
          text: this.props.translate('react.putAway.putAway.label', 'Putaway'),
          color: '#000000',
          delimeter: ' | ',
        },
        {
          text: putAway.putAway.putawayNumber,
          color: '#000000',
          delimeter: '',
        },
      ];
    }
    return [];
  }

  updateWizardValues(page, putAway) {
    this.setState({ page, putAway });
  }

  dataFetched = false;

  fetchPutAway() {
    if (this.props.match.params.putAwayId) {
      this.props.showSpinner();

      const url = `/api/putaways/${this.props.match.params.putAwayId}`;

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
    const pageList = [PutAwayPage, PutAwaySecondPage, PutAwayCheckPage];

    if (_.get(location, 'id')) {
      return (
        <Wizard
          pageList={pageList}
          stepList={this.stepList}
          initialValues={putAway}
          title={this.wizardTitle}
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

const mapStateToProps = (state) => ({
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
