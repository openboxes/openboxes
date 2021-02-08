import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import CreateStockMovement from './outbound/CreateStockMovement';
import AddItemsPage from './outbound/AddItemsPage';
import EditPage from './outbound/EditPage';
import PickPage from './outbound/PickPage';
import PackingPage from './outbound/PackingPage';
import SendMovementPage from './outbound/SendMovementPage';
import Wizard from '../wizard/Wizard';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

import './StockMovement.scss';

// TODO: check docs for SM wizard and Wizard related components

/** Main outbound stock movement form's wizard component. */
class StockMovements extends Component {
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
    this.props.fetchTranslations('', 'stockMovement');

    if (this.props.stockMovementTranslationsFetched) {
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
      this.props.fetchTranslations(nextProps.locale, 'stockMovement');
    }

    if (nextProps.stockMovementTranslationsFetched && !this.dataFetched) {
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

  /**
   * Returns array of form steps.
   * @public
   */
  getStepList() {
    let stepList = [];
    if (this.props.hasPackingSupport) {
      stepList = [this.props.translate('react.stockMovement.create.label', 'Create'),
        this.props.translate('react.stockMovement.addItems.label', 'Add items'),
        this.props.translate('react.stockMovement.edit.label', 'Edit'),
        this.props.translate('react.stockMovement.pick.label', 'Pick'),
        this.props.translate('react.stockMovement.pack.label', 'Pack'),
        this.props.translate('react.stockMovement.send.label', 'Send')];
    } else {
      stepList = [this.props.translate('react.stockMovement.create.label', 'Create'),
        this.props.translate('react.stockMovement.addItems.label', 'Add items'),
        this.props.translate('react.stockMovement.edit.label', 'Edit'),
        this.props.translate('react.stockMovement.pick.label', 'Pick'),
        this.props.translate('react.stockMovement.send.label', 'Send')];
    }
    return stepList;
  }

  /**
   * Returns array of form's components.
   * @public
   */
  getPageList() {
    let formList = [];
    if (this.props.hasPackingSupport) {
      formList = [
        CreateStockMovement,
        AddItemsPage,
        EditPage,
        PickPage,
        PackingPage,
        SendMovementPage,
      ];
    } else {
      formList = [
        CreateStockMovement,
        AddItemsPage,
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
  getWizardTitle() {
    const { values } = this.state;
    if (!values.movementNumber && !values.trackingNumber) {
      return '';
    }
    return [
      {
        text: 'Stock Movement',
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

  getAdditionalWizardTitle() {
    const { currentPage, values } = this.state;
    const shipped = values.shipped ? 'SHIPPED' : '';
    const received = values.received ? 'RECEIVED' : '';
    if ((this.props.hasPackingSupport && currentPage === 6) ||
      (!this.props.hasPackingSupport && currentPage === 5)) {
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
    if (values.movementNumber && (values.id || values.stockMovementId)) {
      const {
        actionLabel, defaultActionLabel, actionUrl, listLabel, defaultListLabel, listUrl,
      } = this.props.breadcrumbsConfig;
      this.props.updateBreadcrumbs([
        { label: listLabel, defaultLabel: defaultListLabel, url: listUrl },
        { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
        { label: values.movementNumber, url: actionUrl, id: values.id || values.stockMovementId },
      ]);
    }
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
            case 'NEW':
              break;
            case 'CREATED':
            case 'REQUESTING':
              currentPage = 2;
              break;
            case 'REQUESTED':
            case 'VALIDATING':
              currentPage = 3;
              break;
            case 'VALIDATED':
            case 'PICKING':
              currentPage = 4;
              break;
            case 'PICKED':
            case 'PACKING':
              currentPage = 5;
              break;
            default:
              currentPage = this.props.hasPackingSupport ? 6 : 5;
              break;
          }

          this.setState({ values, currentPage });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { values, currentPage } = this.state;
    const { currentLocation } = this.props;
    const title = this.getWizardTitle();
    const additionalTitle = this.getAdditionalWizardTitle();
    const pageList = this.getPageList();
    const stepList = this.getStepList();
    const showOnly = values.origin && values.origin.id !== currentLocation.id;

    return (
      <Wizard
        pageList={pageList}
        stepList={stepList}
        initialValues={values}
        title={title}
        additionalTitle={additionalTitle}
        currentPage={currentPage}
        prevPage={currentPage === 1 ? 1 : currentPage - 1}
        updateWizardValues={this.updateWizardValues}
        additionalProps={{ showOnly }}
      />
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  hasPackingSupport: state.session.currentLocation.hasPackingSupport,
  currentLocation: state.session.currentLocation,
  breadcrumbsConfig: state.session.breadcrumbsConfig.outbound,
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
})(StockMovements);

StockMovements.propTypes = {
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

StockMovements.defaultProps = {
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
