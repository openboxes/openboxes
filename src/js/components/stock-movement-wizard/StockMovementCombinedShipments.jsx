import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
import AddItemsPage from 'components/stock-movement-wizard/combined-shipments/AddItemsPage';
import CreateStockMovement from 'components/stock-movement-wizard/combined-shipments/CreateStockMovement';
import SendMovementPage from 'components/stock-movement-wizard/combined-shipments/SendMovementPage';
import Wizard from 'components/wizard/Wizard';
import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/stock-movement-wizard/StockMovement.scss';

/** Main combined shipments stock movement form's wizard component. */
class StockMovementCombinedShipments extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: this.props.initialValues,
      currentPage: 1,
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'stockMovement');

    if (this.props.stockMovementTranslationsFetched) {
      this.dataFetched = true;
      this.fetchInitialValues();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'stockMovement');
    }

    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchInitialValues();
    }
  }

  get wizardTitle() {
    const { values } = this.state;
    if (!values.movementNumber && !values.trackingNumber) {
      return '';
    }
    return [
      {
        text: this.props.translate('react.stockMovement.label', 'Stock Movement'),
        color: '#000000',
        delimeter: ' | ',
      },
      {
        text: values.movementNumber,
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
        delimeter: ', ',
      },
      {
        text: values.dateRequested,
        color: '#4a148c',
        delimeter: ', ',
      },
      {
        text: values.description,
        color: '#770838',
        delimeter: '',
      },
    ];
  }

  get additionalWizardTitle() {
    const { currentPage, values } = this.state;
    const shipped = values.shipped ? 'SHIPPED' : '';
    const received = values.received ? 'RECEIVED' : '';
    if (currentPage === 3) {
      return (
        <span className="shipment-status float-right">
          {`${shipped || received || 'PENDING'}`}
        </span>
      );
    }
    return null;
  }

  /**
   * Returns array of form steps.
   * @public
   */
  get stepList() {
    return [
      this.props.translate('react.stockMovement.create.label', 'Create'),
      this.props.translate('react.stockMovement.addItems.label', 'Add items'),
      this.props.translate('react.stockMovement.send.label', 'Send'),
    ];
  }

  updateWizardValues(currentPage, values) {
    this.setState({ currentPage, values });
  }

  /**
   * Returns array of form's components.
   * @public
   */
  pageList = [CreateStockMovement, AddItemsPage, SendMovementPage];

  dataFetched = false;

  /**
   * Fetches initial values from API.
   * @public
   */
  fetchInitialValues() {
    if (this.props.match.params.stockMovementId) {
      this.props.showSpinner();
      const url = `/openboxes/api/stockMovements/${this.props.match.params.stockMovementId}`;
      apiClient.get(url)
        .then((response) => {
          const resp = response.data.data;
          const originType = resp.origin.locationType;
          const destinationType = resp.destination.locationType;
          const values = {
            ...resp,
            stockMovementId: resp.id,
            movementNumber: resp.identifier,
            origin: {
              id: resp.origin.id,
              type: originType ? originType.locationTypeCode : null,
              name: resp.origin.name,
              label: `${resp.origin.organizationCode ? `${resp.origin.organizationCode} - ` : ''}${resp.origin.name}`,
            },
            destination: {
              id: resp.destination.id,
              type: destinationType ? destinationType.locationTypeCode : null,
              name: resp.destination.name,
              label: `${resp.destination.name} [${destinationType ? destinationType.description : null}]`,
            },
          };

          let currentPage = 1;
          switch (values.statusCode) {
            case 'NEW':
              break;
            case 'PENDING':
              currentPage = 2;
              break;
            default:
              currentPage = 3;
              break;
          }

          this.setState({ values, currentPage });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { values, currentPage } = this.state;

    return (
      <Wizard
        pageList={this.pageList}
        stepList={this.stepList}
        initialValues={values}
        title={this.wizardTitle}
        additionalTitle={this.additionalWizardTitle}
        currentPage={currentPage}
        prevPage={currentPage === 1 ? 1 : currentPage - 1}
        updateWizardValues={this.updateWizardValues}
      />
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(StockMovementCombinedShipments);

StockMovementCombinedShipments.propTypes = {
  /** React router's object which contains information about url variables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ stockMovementId: PropTypes.string }),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Initial components' data */
  initialValues: PropTypes.shape({
    shipmentStatus: PropTypes.string,
  }),
  locale: PropTypes.string.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};

StockMovementCombinedShipments.defaultProps = {
  initialValues: {},
};
