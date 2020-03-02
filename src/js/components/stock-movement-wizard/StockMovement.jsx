import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import moment from 'moment';
import { getTranslate } from 'react-localize-redux';

import CreateStockMovement from './CreateStockMovement';
import AddItemsPage from './AddItemsPage';
import EditPage from './EditPage';
import PickPage from './PickPage';
import PackingPage from './PackingPage';
import SendMovementPage from './SendMovementPage';
import Wizard from '../wizard/Wizard';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner, fetchTranslations } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

// TODO: Add Inbound SM wizard
// TODO: Add PO SM wizard
// TODO: Add SM wizard for REQUEST type (see code below)
// const request = queryString.parse(window.location.search).type === 'REQUEST';
// if (request && (status === 'CREATED' || !status)) { [ createStockMovement, addItemsPage ] }
// TODO: check docs for SM wizard and Wizard related components
// TODO: create separate pages for each workflow

/** Main stock movement form's wizard component. */
class StockMovements extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: this.props.initialValues,
      currentPage: 1,
    };
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
    let newName = '';
    if (!values.movementNumber && !values.trackingNumber) {
      return '';
    }
    if (values.movementNumber && values.name && !values.trackingNumber) {
      newName = values.name;
    }
    if (values.trackingNumber) {
      const {
        origin, destination, dateRequested, stocklist, trackingNumber, description,
      } = values;
      const stocklistPart = stocklist && stocklist.name ? `${stocklist.name}.` : '';
      const dateReq = moment(dateRequested, 'MM/DD/YYYY').format('DDMMMYYYY');
      newName = `${origin.name}.${destination.name}.${dateReq}.${stocklistPart}${trackingNumber}.${description}`;
      newName.replace(/ /gi, '');
    }
    return `${values.movementNumber} - ${newName}`;
  }

  getAdditionalWizardTitle() {
    const { currentPage, values } = this.state;
    if (currentPage === 6) {
      return (
        <span className="shipment-status float-right">
          {`${values.shipmentStatus ? values.shipmentStatus : 'PENDING'}`}
        </span>
      );
    }
    return null;
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
          };

          let statuses = [];
          if (this.props.hasPackingSupport) {
            statuses = ['NEW', 'CREATED', 'VERIFYING', 'PICKING', 'PACKED', 'PICKED'];
          } else {
            statuses = ['NEW', 'CREATED', 'VERIFYING', 'PICKING', 'PICKED'];
          }

          let currentPage = 1;
          if (statuses.indexOf(values.statusCode) > 0) {
            currentPage = statuses.indexOf(values.statusCode) + 1;
          } else {
            currentPage = this.props.hasPackingSupport ? 6 : 5;
          }
          this.setState({ values, currentPage });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { values, currentPage } = this.state;
    const title = this.getWizardTitle();
    const additionalTitle = this.getAdditionalWizardTitle();
    const pageList = this.getPageList();
    const stepList = this.getStepList();

    return (
      <Wizard
        pageList={pageList}
        stepList={stepList}
        initialValues={values}
        title={title}
        additionalTitle={additionalTitle}
        currentPage={currentPage}
        prevPage={currentPage === 1 ? 1 : currentPage - 1}
      />
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  hasPackingSupport: state.session.currentLocation.hasPackingSupport,
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
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
  /** Initial components' data */
  initialValues: PropTypes.shape({
    shipmentStatus: PropTypes.string,
  }),
  locale: PropTypes.string.isRequired,
  stockMovementTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  /** Is true when currently selected location supports packing */
  hasPackingSupport: PropTypes.bool.isRequired,
};

StockMovements.defaultProps = {
  initialValues: {},
};
