import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { getTranslate } from 'react-localize-redux';

import CreateStockTransfer from './CreateStockTransfer';
import StockTransferSecondPage from './StockTransferSecondPage';
import StockTransferCheckPage from './StockTransferCheckPage';
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

import './StockTransfer.scss';

/** Main stock transfer form's wizard component. */
class StockTransferWizard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: props.match.params.stockTransferId ? 2 : 1,
      stockTransfer: {},
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchBreadcrumbsConfig();
    this.props.fetchTranslations('', 'stockTransfer');

    if (this.props.stockTransferTranslationsFetched) {
      this.dataFetched = true;

      this.fetchStockTransfer();
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
      this.props.fetchTranslations(nextProps.locale, 'stockTransfer');
    }

    if (nextProps.stockTransferTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchStockTransfer();
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

  /**
   * @public
   */
  getStepList() {
    const stepList = [this.props.translate('react.stockTransfer.createStockTransfer.label', 'Create Stock Transfer'),
      this.props.translate('react.stockTransfer.startStockTransfer.label', 'Start Stock Transfer'),
      this.props.translate('react.stockTransfer.checkStockTransfer.label', 'Check Stock Transfer'),
    ];
    return stepList;
  }

  getWizardTitle() {
    const { stockTransfer } = this.state;
    const newName = stockTransfer ? `Stock Transfer - ${stockTransfer.orderNumber}` : '';
    return newName;
  }

  updateWizardValues(page, stockTransfer) {
    this.setState({ page, stockTransfer });
  }

  dataFetched = false;


  fetchStockTransfer() {
    if (this.props.match.params.stockTransferId) {
      this.props.showSpinner();
      const url = `/openboxes/api/stockTransfers/${this.props.match.params.stockTransferId}`;

      apiClient.get(url)
        .then((response) => {
          const stockTransfer = parseResponse(response.data.data);
          this.setState({ stockTransfer: { stockTransfer }, page: stockTransfer.status === 'COMPLETED' ? 3 : 2 });
          this.props.hideSpinner();
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { page, stockTransfer } = this.state;
    const { location, history, match } = this.props;
    const locationId = location.id;
    const title = this.getWizardTitle();
    const additionalTitle = null;
    const pageList = [CreateStockTransfer, StockTransferSecondPage, StockTransferCheckPage];
    const stepList = this.getStepList();

    if (_.get(location, 'id')) {
      return (
        <Wizard
          pageList={pageList}
          stepList={stepList}
          initialValues={stockTransfer}
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
  stockTransferTranslationsFetched: state.session.fetchedTranslations.stockTransfer,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  breadcrumbsConfig: state.session.breadcrumbsConfig.stockTransfer,
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
})(StockTransferWizard));

StockTransferWizard.propTypes = {
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  locale: PropTypes.string.isRequired,
  stockTransferTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ stockTransferId: PropTypes.string }),
  }).isRequired,
  /** React router's object used to manage session history */
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
  // Labels and url with translation
  breadcrumbsConfig: PropTypes.shape({
    actionLabel: PropTypes.string.isRequired,
    defaultActionLabel: PropTypes.string.isRequired,
    listLabel: PropTypes.string.isRequired,
    defaultListLabel: PropTypes.string.isRequired,
    listUrl: PropTypes.string.isRequired,
    actionUrl: PropTypes.string.isRequired,
  }),
  // Method to update breadcrumbs data
  updateBreadcrumbs: PropTypes.func.isRequired,
  fetchBreadcrumbsConfig: PropTypes.func.isRequired,
};

StockTransferWizard.defaultProps = {
  breadcrumbsConfig: {
    actionLabel: '',
    defaultActionLabel: '',
    listLabel: '',
    defaultListLabel: '',
    listUrl: '',
    actionUrl: '',
  },
};
