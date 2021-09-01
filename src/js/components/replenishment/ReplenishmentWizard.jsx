import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { getTranslate } from 'react-localize-redux';

import CreateReplenishment from './CreateReplenishment';
import ReplenishmentSecondPage from './ReplenishmentSecondPage';
import ReplenishmentCheckPage from './ReplenishmentCheckPage';
import Wizard from '../wizard/Wizard';
import apiClient, { parseResponse } from '../../utils/apiClient';
import {
  showSpinner,
  hideSpinner,
  fetchTranslations,
  updateBreadcrumbs,
  fetchBreadcrumbsConfig,
} from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

import './Replenishment.scss';

class ReplenishmentWizard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: props.match.params.replenishmentId ? 2 : 1,
      replenishment: {},
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchBreadcrumbsConfig();
    this.props.fetchTranslations('', 'replenishment');

    if (this.props.replenishmentTranslationsFetched) {
      this.dataFetched = true;

      this.fetchReplenishment();
    }

    const {
      actionLabel, defaultActionLabel, actionUrl, listLabel, defaultListLabel, listUrl,
    } = this.props.breadcrumbsConfig;
    this.props.updateBreadcrumbs([
      { label: listLabel, defaultLabel: defaultListLabel, url: listUrl },
      { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
    ]);
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'replenishment');
    }

    if (nextProps.replenishmentTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchReplenishment();
    }

    if (nextProps.breadcrumbsConfig &&
      nextProps.breadcrumbsConfig !== this.props.breadcrumbsConfig) {
      const {
        actionLabel, defaultActionLabel, actionUrl, listLabel, defaultListLabel, listUrl,
      } = nextProps.breadcrumbsConfig;

      this.props.updateBreadcrumbs([
        { label: listLabel, defaultLabel: defaultListLabel, url: listUrl },
        { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
      ]);
    }
  }

  getStepList() {
    return [this.props.translate('react.replenishment.createReplenishment.label', 'Create Replenishment'),
      this.props.translate('react.replenishment.startReplenishment.label', 'Start Replenishment'),
      this.props.translate('react.replenishment.checkReplenishment.label', 'Check Replenishment'),
    ];
  }

  getWizardTitle() {
    const { replenishment } = this.state;
    return replenishment ? `Replenishment - ${replenishment.orderNumber}` : '';
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
          this.setState({ replenishment: { replenishment }, page: replenishment.status === 'COMPLETED' ? 3 : 2 });
          this.props.hideSpinner();
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { page, replenishment } = this.state;
    const { location, history, match } = this.props;
    const locationId = location.id;
    const title = this.getWizardTitle();
    const additionalTitle = null;
    const pageList = [CreateReplenishment, ReplenishmentSecondPage, ReplenishmentCheckPage];
    const stepList = this.getStepList();

    if (_.get(location, 'id')) {
      return (
        <Wizard
          pageList={pageList}
          stepList={stepList}
          initialValues={replenishment}
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
  replenishmentTranslationsFetched: state.session.fetchedTranslations.replenishment,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  breadcrumbsConfig: state.session.breadcrumbsConfig.replenishment,
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
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
  breadcrumbsConfig: PropTypes.shape({
    actionLabel: PropTypes.string.isRequired,
    defaultActionLabel: PropTypes.string.isRequired,
    listLabel: PropTypes.string.isRequired,
    defaultListLabel: PropTypes.string.isRequired,
    listUrl: PropTypes.string.isRequired,
    actionUrl: PropTypes.string.isRequired,
  }),
  updateBreadcrumbs: PropTypes.func.isRequired,
  fetchBreadcrumbsConfig: PropTypes.func.isRequired,
};

ReplenishmentWizard.defaultProps = {
  breadcrumbsConfig: {
    actionLabel: '',
    defaultActionLabel: '',
    listLabel: '',
    defaultListLabel: '',
    listUrl: '',
    actionUrl: '',
  },
};
