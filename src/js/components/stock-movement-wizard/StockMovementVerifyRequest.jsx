import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
import PackingPage from 'components/stock-movement-wizard/outbound/PackingPage';
import PickPage from 'components/stock-movement-wizard/outbound/PickPage';
import SendMovementPage from 'components/stock-movement-wizard/outbound/SendMovementPage';
import EditPage from 'components/stock-movement-wizard/request/EditPage';
import Wizard from 'components/wizard/Wizard';
import apiClient from 'utils/apiClient';
import canEditRequest from 'utils/permissionUtils';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/stock-movement-wizard/StockMovement.scss';

/** Main Verify Request Stock movement form's wizard component. */
class StockMovementVerifyRequest extends Component {
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

  /**
   * Returns array of form steps.
   * @public
   */
  get stepList() {
    let stepList = [];
    if (this.props.hasPackingSupport) {
      stepList = [this.props.translate('react.stockMovement.edit.label', 'Edit'),
        this.props.translate('react.stockMovement.pick.label', 'Pick'),
        this.props.translate('react.stockMovement.pack.label', 'Pack'),
        this.props.translate('react.stockMovement.send.label', 'Send')];
    } else {
      stepList = [this.props.translate('react.stockMovement.edit.label', 'Edit'),
        this.props.translate('react.stockMovement.pick.label', 'Pick'),
        this.props.translate('react.stockMovement.send.label', 'Send')];
    }
    return stepList;
  }

  /**
   * Returns array of form's components.
   * @public
   */
  get pageList() {
    let formList = [];
    if (this.props.hasPackingSupport) {
      formList = [
        EditPage,
        PickPage,
        PackingPage,
        SendMovementPage,
      ];
    } else {
      formList = [
        EditPage,
        PickPage,
        SendMovementPage,
      ];
    }
    return formList;
  }

  /**
   * Returns shipment's name containing shipment's origin, destination, requisition date,
   * tracking number given by user on the last step, description and stock list if chosen.
   * @public
   */
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
        delimeter: ` ${this.props.translate('react.default.to.label', 'to')} `,
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
    if ((this.props.hasPackingSupport && currentPage === 4) ||
      (!this.props.hasPackingSupport && currentPage === 3)) {
      return (
        <span className="shipment-status float-right">
          {`${shipped || received || 'PENDING'}`}
        </span>
      );
    }
    return null;
  }

  updateWizardValues(currentPage, values) {
    this.setState({ currentPage, values });
  }

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
              label: `${resp.origin.name} [${originType ? originType.description : null}]`,
            },
            destination: {
              id: resp.destination.id,
              type: destinationType ? destinationType.locationTypeCode : null,
              name: resp.destination.name,
              label: `${resp.destination.name} [${destinationType ? destinationType.description : null}]`,
            },
            requestedBy: {
              id: resp.requestedBy.id,
              name: resp.requestedBy.name,
              label: resp.requestedBy.name,
            },
            requestType: {
              name: resp.requestType.name,
              label: resp.requestType.name,
            },
          };

          let currentPage = 1;
          switch (values.statusCode) {
            case 'REQUESTED':
            case 'VALIDATING':
            case 'PENDING_APPROVAL':
            case 'APPROVED':
              break;
            case 'VALIDATED':
            case 'PICKING':
              currentPage = 2;
              break;
            case 'PICKED':
            case 'PACKING':
              currentPage = 3;
              break;
            default:
              currentPage = this.props.hasPackingSupport ? 4 : 3;
              break;
          }

          this.setState({ values, currentPage });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { values, currentPage } = this.state;
    const { currentLocation, currentUser } = this.props;
    const showOnly = (values.origin && values.origin.id !== currentLocation.id) ||
      (values?.isElectronicType && !canEditRequest(currentUser, values, currentLocation));

    if (values.stockMovementId) {
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
          additionalProps={{ showOnly }}
        />
      );
    }
    return null;
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  hasPackingSupport: state.session.currentLocation.hasPackingSupport,
  currentLocation: state.session.currentLocation,
  currentUser: state.session.user,
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(StockMovementVerifyRequest);

StockMovementVerifyRequest.propTypes = {
  /** React router's object which contains information about url variables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ stockMovementId: PropTypes.string }),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  /** Is true when currently selected location supports packing */
  hasPackingSupport: PropTypes.bool.isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  /** Initial components' data */
  initialValues: PropTypes.shape({
    shipmentStatus: PropTypes.string,
  }),
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
  }).isRequired,
};

StockMovementVerifyRequest.defaultProps = {
  initialValues: {},
};
