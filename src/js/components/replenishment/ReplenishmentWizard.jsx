import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import {
  fetchTranslations,
  hideSpinner,
  showSpinner,
} from 'actions';
import CreateReplenishment from 'components/replenishment/CreateReplenishment';
import ReplenishmentCheckPage from 'components/replenishment/ReplenishmentCheckPage';
import ReplenishmentSecondPage from 'components/replenishment/ReplenishmentSecondPage';
import Wizard from 'components/wizard/Wizard';
import apiClient, { parseResponse } from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/replenishment/Replenishment.scss';


class ReplenishmentWizard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      replenishment: {},
      page: props.match.params.replenishmentId ? 2 : 1,
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'replenishment');

    if (this.props.replenishmentTranslationsFetched) {
      this.dataFetched = true;

      this.fetchReplenishment();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'replenishment');
    }

    if (nextProps.replenishmentTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchReplenishment();
    }
  }

  get stepList() {
    return [
      this.props.translate('react.replenishment.createReplenishment.label', 'Create Replenishment'),
      this.props.translate('react.replenishment.startReplenishment.label', 'Start Replenishment'),
      this.props.translate('react.replenishment.checkReplenishment.label', 'Check Replenishment'),
    ];
  }

  get wizardTitle() {
    const { replenishment } = this.state;
    if (replenishment?.replenishmentNumber) {
      return [
        {
          text: this.props.translate('react.binReplenishment.label', 'Bin replenishment'),
          color: '#000000',
          delimeter: ' | ',
        },
        {
          text: replenishment.replenishmentNumber,
          color: '#000000',
          delimeter: '',
        },
      ];
    }
    return '';
  }

  updateWizardValues(page, replenishment) {
    this.setState({ page, replenishment });
  }

  dataFetched = false;

  fetchReplenishment() {
    if (this.props.match.params.replenishmentId) {
      this.props.showSpinner();
      const url = `/openboxes/api/replenishments/${this.props.match.params.replenishmentId}`;

      apiClient.get(url)
        .then((response) => {
          const replenishment = parseResponse(response.data.data);
          this.setState(
            { replenishment, page: replenishment.status === 'PENDING' ? 2 : 3 },
            () => this.props.hideSpinner(),
          );
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { page, replenishment } = this.state;
    const { location, history, match } = this.props;
    const locationId = location.id;
    const pageList = [CreateReplenishment, ReplenishmentSecondPage, ReplenishmentCheckPage];

    if (_.get(location, 'id')) {
      return (
        <Wizard
          pageList={pageList}
          stepList={this.stepList}
          initialValues={replenishment}
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

const mapStateToProps = state => ({
  location: state.session.currentLocation,
  locale: state.session.activeLanguage,
  replenishmentTranslationsFetched: state.session.fetchedTranslations.replenishment,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(ReplenishmentWizard));

ReplenishmentWizard.propTypes = {
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  locale: PropTypes.string.isRequired,
  replenishmentTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({ replenishmentId: PropTypes.string }),
  }).isRequired,
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
