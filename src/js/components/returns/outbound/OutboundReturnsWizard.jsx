import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
import AddItemsPage from 'components/returns/outbound/AddItemsPage';
import CreateOutboundReturn from 'components/returns/outbound/CreateOutboundReturn';
import PickPage from 'components/returns/outbound/PickPage';
import SendOutboundReturns from 'components/returns/outbound/SendOutboundReturn';
import Wizard from 'components/wizard/Wizard';
import apiClient, { parseResponse } from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/stock-movement-wizard/StockMovement.scss';

class OutboundReturns extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: this.props.initialValues,
      currentPage: 1,
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'outboundReturns');
    this.props.fetchTranslations('', 'stockMovement');

    if (this.props.outboundReturnsTranslationsFetched) {
      this.dataFetched = true;

      this.fetchInitialValues();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'outboundReturns');
      this.props.fetchTranslations(nextProps.locale, 'stockMovement');
    }

    if (nextProps.outboundReturnsTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchInitialValues();
    }
  }

  get stepList() {
    return [
      this.props.translate('react.outboundReturns.create.label', 'Create'),
      this.props.translate('react.outboundReturns.addItems.label', 'Add items'),
      this.props.translate('react.outboundReturns.pick.label', 'Pick'),
      this.props.translate('react.outboundReturns.send.label', 'Send'),
    ];
  }

  get wizardTitle() {
    const { values } = this.state;
    if (!values.stockTransferNumber || !values.origin || !values.destination) {
      return '';
    }

    return [
      {
        text: ` ${this.props.translate('react.outboundReturns.outboundReturn.label', 'Outbound Return')} `,
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
        delimeter: ` ${this.props.translate('react.default.to.label', 'to')} `,
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
    if (this.props.match.params.outboundReturnId) {
      this.props.showSpinner();
      const url = `/openboxes/api/stockTransfers/${this.props.match.params.outboundReturnId}`;

      apiClient.get(url)
        .then((response) => {
          const outboundReturn = parseResponse(response.data.data);
          let currentPage;
          switch (outboundReturn.status) {
            case 'PENDING':
              currentPage = 2;
              break;
            case 'APPROVED':
              currentPage = 3;
              break;
            default:
              currentPage = 4;
              break;
          }
          this.setState({
            values: {
              ...outboundReturn,
              origin: {
                id: outboundReturn.origin.id,
                name: outboundReturn.origin.name,
                label: outboundReturn.origin.name,
              },
              destination: {
                id: outboundReturn.destination.id,
                name: outboundReturn.destination.name,
                label: outboundReturn.destination.name,
              },
            },
            currentPage,
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
    const pageList = [CreateOutboundReturn, AddItemsPage, PickPage, SendOutboundReturns];
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
  outboundReturnsTranslationsFetched: state.session.fetchedTranslations.outboundReturns,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(OutboundReturns);

OutboundReturns.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({ outboundReturnId: PropTypes.string }),
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

OutboundReturns.defaultProps = {
  initialValues: {},
};
