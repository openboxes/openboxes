import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchBreadcrumbsConfig, fetchTranslations, hideSpinner, showSpinner, updateBreadcrumbs } from 'actions';
import AddItemsPage from 'components/returns/inbound/AddItemsPage';
import CreateInboundReturn from 'components/returns/inbound/CreateInboundReturn';
import SendInboundReturn from 'components/returns/inbound/SendInboundReturn';
import Wizard from 'components/wizard/Wizard';
import apiClient, { parseResponse } from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/stock-movement-wizard/StockMovement.scss';

class InboundReturns extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: this.props.initialValues,
      currentPage: 1,
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchBreadcrumbsConfig();
    this.props.fetchTranslations('', 'inboundReturns');

    if (this.props.inboundReturnsTranslationsFetched) {
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
      this.props.fetchTranslations(nextProps.locale, 'inboundReturns');
    }

    if (nextProps.inboundReturnsTranslationsFetched && !this.dataFetched) {
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
    return [
      this.props.translate('react.inboundReturns.create.label', 'Create'),
      this.props.translate('react.inboundReturns.addItems.label', 'Add items'),
      this.props.translate('react.inboundReturns.send.label', 'Send'),
    ];
  }

  getWizardTitle() {
    const { values } = this.state;
    if (!values.stockTransferNumber || !values.origin || !values.destination) {
      return '';
    }

    return [
      {
        text: 'Inbound Return',
        color: '#000000',
        delimeter: ' | ',
      },
      {
        text: values.stockTransferNumber,
        color: '#000000',
        delimeter: ' - ',
      },
      {
        text: values.origin.name,
        color: '#004d40',
        delimeter: ' to ',
      },
      {
        text: values.destination.name,
        color: '#01579b',
        delimeter: '',
      },
    ];
  }

  dataFetched = false;

  fetchInitialValues() {
    if (this.props.match.params.inboundReturnId) {
      this.props.showSpinner();
      const url = `/openboxes/api/stockTransfers/${this.props.match.params.inboundReturnId}`;

      apiClient.get(url)
        .then((response) => {
          const inboundReturn = parseResponse(response.data.data);
          this.setState({
            values: {
              ...inboundReturn,
              origin: {
                id: inboundReturn.origin.id,
                name: inboundReturn.origin.name,
                label: inboundReturn.origin.name,
              },
              destination: {
                id: inboundReturn.destination.id,
                name: inboundReturn.destination.name,
                label: inboundReturn.destination.name,
              },
            },
            currentPage: inboundReturn.status === 'PENDING' ? 2 : 3,
          });
          this.props.hideSpinner();
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  updateWizardValues(currentPage, values) {
    this.setState({
      currentPage,
      values: {
        ...values,
        origin: {
          id: values.origin.id,
          name: values.origin.name,
          label: values.origin.name,
        },
        destination: {
          id: values.destination.id,
          name: values.destination.name,
          label: values.destination.name,
        },
      },
    });
    if (values.stockTransferNumber && values.id) {
      const {
        actionLabel, defaultActionLabel, actionUrl, listLabel, defaultListLabel, listUrl,
      } = this.props.breadcrumbsConfig;
      this.props.updateBreadcrumbs([
        { label: listLabel, defaultLabel: defaultListLabel, url: listUrl },
        { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
        { label: values.stockTransferNumber, url: actionUrl, id: values.id },
      ]);
    }
  }

  render() {
    const { values, currentPage } = this.state;
    const title = this.getWizardTitle();
    const pageList = [CreateInboundReturn, AddItemsPage, SendInboundReturn];
    const stepList = this.getStepList();
    const { location, history, match } = this.props;
    const locationId = location.id;

    return (
      <Wizard
        pageList={pageList}
        stepList={stepList}
        initialValues={values}
        title={title}
        currentPage={currentPage}
        prevPage={currentPage === 1 ? 1 : currentPage - 1}
        additionalProps={{
          locationId, location, history, match,
        }}
        updateWizardValues={this.updateWizardValues}
      />
    );
  }
}

const mapStateToProps = state => ({
  breadcrumbsConfig: state.session.breadcrumbsConfig.inboundReturns,
  locale: state.session.activeLanguage,
  location: state.session.currentLocation,
  inboundReturnsTranslationsFetched: state.session.fetchedTranslations.inboundReturns,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
})(InboundReturns);

InboundReturns.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({ inboundReturnId: PropTypes.string }),
  }).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
  inboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
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
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  location: PropTypes.shape({
    name: PropTypes.string,
    id: PropTypes.string,
    locationType: PropTypes.shape({
      description: PropTypes.string,
      locationTypeCode: PropTypes.string,
    }),
  }).isRequired,
};

InboundReturns.defaultProps = {
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
