import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
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
    this.props.fetchTranslations('', 'inboundReturns');
    this.props.fetchTranslations('', 'stockMovement');

    if (this.props.inboundReturnsTranslationsFetched) {
      this.dataFetched = true;

      this.fetchInitialValues();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'inboundReturns');
      this.props.fetchTranslations(nextProps.locale, 'stockMovement');
    }

    if (nextProps.inboundReturnsTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchInitialValues();
    }
  }

  get stepList() {
    return [
      this.props.translate('react.inboundReturns.create.label', 'Create'),
      this.props.translate('react.inboundReturns.addItems.label', 'Add items'),
      this.props.translate('react.inboundReturns.send.label', 'Send'),
    ];
  }

  get wizardTitle() {
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
  }

  render() {
    const { values, currentPage } = this.state;
    const pageList = [CreateInboundReturn, AddItemsPage, SendInboundReturn];
    const { location, history, match } = this.props;
    const locationId = location.id;

    return (
      <Wizard
        pageList={pageList}
        stepList={this.stepList}
        initialValues={values}
        title={this.wizardTitle}
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
  locale: state.session.activeLanguage,
  location: state.session.currentLocation,
  inboundReturnsTranslationsFetched: state.session.fetchedTranslations.inboundReturns,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
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
};
