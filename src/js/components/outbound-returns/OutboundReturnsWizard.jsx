import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import CreateOutboundReturns from './CreateOutboundReturns';
import AddItemsPage from './AddItemsPage';
import SendOutboundReturns from './SendOutboundReturns';
import Wizard from '../wizard/Wizard';
import apiClient, { parseResponse } from '../../utils/apiClient';
import { showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

import '../stock-movement-wizard/StockMovement.scss';

class OutboundReturns extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: this.props.initialValues,
      currentPage: 1,
    };
  }

  componentDidMount() {
    this.props.fetchBreadcrumbsConfig();
    this.props.fetchTranslations('', 'outboundReturns');

    if (this.props.outboundReturnsTranslationsFetched) {
      this.dataFetched = true;

      this.fetchInitialValues();
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
      this.props.fetchTranslations(nextProps.locale, 'outboundReturns');
    }

    if (nextProps.outboundReturnsTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchInitialValues();
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
    return [this.props.translate('react.outboundReturns.create.label', 'Create'),
      this.props.translate('react.outboundReturns.addItems.label', 'Add items'),
      this.props.translate('react.outboundReturns.send.label', 'Send')];
  }

  getWizardTitle() {
    const { values } = this.state;
    return values.movementNumber ? values.movementNumber : '';
  }

  dataFetched = false;

  fetchInitialValues() {
    if (this.props.match.params.outboundReturnsId) {
      this.props.showSpinner();
      const url = `/openboxes/api/stockTransfers/${this.props.match.params.outboundReturnsId}`;

      apiClient.get(url)
        .then((response) => {
          const outboundReturns = parseResponse(response.data.data);
          this.setState({ values: { ...outboundReturns }, currentPage: outboundReturns.status === 'COMPLETED' ? 3 : 2 });
          this.props.hideSpinner();
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { values, currentPage } = this.state;
    const title = this.getWizardTitle();
    const pageList = [CreateOutboundReturns, AddItemsPage, SendOutboundReturns];
    const stepList = this.getStepList();

    return (
      <Wizard
        pageList={pageList}
        stepList={stepList}
        initialValues={values}
        title={title}
        currentPage={currentPage}
        prevPage={currentPage === 1 ? 1 : currentPage - 1}
      />
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  outboundReturnsTranslationsFetched: state.session.fetchedTranslations.outboundReturns,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  breadcrumbsConfig: state.session.breadcrumbsConfig.returns,
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
})(OutboundReturns);

OutboundReturns.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({ outboundReturnsId: PropTypes.string }),
  }).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
  outboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({
    shipmentStatus: PropTypes.string,
  }),
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

OutboundReturns.defaultProps = {
  initialValues: {},
  breadcrumbsConfig: {
    actionLabel: '',
    defaultActionLabel: '',
    listLabel: '',
    defaultListLabel: '',
    listUrl: '',
    actionUrl: '',
  },
};
