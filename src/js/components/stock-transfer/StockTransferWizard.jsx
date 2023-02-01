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
import CreateStockTransfer from 'components/stock-transfer/CreateStockTransfer';
import StockTransferCheckPage from 'components/stock-transfer/StockTransferCheckPage';
import StockTransferSecondPage from 'components/stock-transfer/StockTransferSecondPage';
import Wizard from 'components/wizard/Wizard';
import apiClient, { parseResponse } from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/stock-transfer/StockTransfer.scss';


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
    this.props.fetchTranslations('', 'stockTransfer');

    if (this.props.stockTransferTranslationsFetched) {
      this.dataFetched = true;

      this.fetchStockTransfer();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'stockTransfer');
    }

    if (nextProps.stockTransferTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchStockTransfer();
    }
  }

  /**
   * @public
   */
  get stepList() {
    return [
      this.props.translate('react.stockTransfer.createStockTransfer.label', 'Create Stock Transfer'),
      this.props.translate('react.stockTransfer.startStockTransfer.label', 'Start Stock Transfer'),
      this.props.translate('react.stockTransfer.checkStockTransfer.label', 'Check Stock Transfer'),
    ];
  }

  get wizardTitle() {
    const { stockTransfer } = this.state;
    if (stockTransfer?.stockTransfer?.stockTransferNumber) {
      return [
        {
          text: this.props.translate('react.stockTransfer.label', 'Stock Transfer'),
          color: '#000000',
          delimeter: ' | ',
        },
        {
          text: stockTransfer.stockTransfer.stockTransferNumber,
          color: '#000000',
          delimeter: '',
        },
      ];
    }
    return '';
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
    const additionalTitle = null;
    const pageList = [CreateStockTransfer, StockTransferSecondPage, StockTransferCheckPage];

    if (_.get(location, 'id')) {
      return (
        <Wizard
          pageList={pageList}
          stepList={this.stepList}
          initialValues={stockTransfer}
          title={this.wizardTitle}
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
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
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
};
