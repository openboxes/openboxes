import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import moment from 'moment';
import { getTranslate } from 'react-localize-redux';
import queryString from 'query-string';

import CreateStockMovement from './CreateStockMovement';
import AddItemsPage from './AddItemsPage';
import EditPage from './EditPage';
import PickPage from './PickPage';
import PackingPage from './PackingPage';
import SendMovementPage from './SendMovementPage';
import WizardSteps from '../form-elements/WizardSteps';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner, fetchTranslations } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

const request = queryString.parse(window.location.search).type === 'REQUEST';

/** Main stock movement form's wizard component. */
class StockMovements extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: 1,
      prevPage: 1,
      values: this.props.initialValues,
    };

    this.nextPage = this.nextPage.bind(this);
    this.previousPage = this.previousPage.bind(this);
    this.goToPage = this.goToPage.bind(this);
    this.setValues = this.setValues.bind(this);
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
  getStepList(status) {
    let stepList = [];
    if (request && (status === 'CREATED' || !status)) {
      stepList = [this.props.translate('react.stockMovement.create.label', 'Create'),
        this.props.translate('react.stockMovement.addItems.label', 'Add items')];
    } else if (this.props.hasPackingSupport) {
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
  getFormList(status) {
    let formList = [];
    if (request && (status === 'CREATED' || !status)) {
      formList = [
        <CreateStockMovement
          initialValues={this.state.values}
          onSubmit={this.nextPage}
        />,
        <AddItemsPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          goToPage={this.goToPage}
          onSubmit={this.nextPage}
        />,
      ];
    } else if (this.props.hasPackingSupport) {
      formList = [
        <CreateStockMovement
          initialValues={this.state.values}
          onSubmit={this.nextPage}
        />,
        <AddItemsPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          goToPage={this.goToPage}
          onSubmit={this.nextPage}
        />,
        <EditPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          onSubmit={this.nextPage}
        />,
        <PickPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          onSubmit={this.nextPage}
        />,
        <PackingPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          onSubmit={this.nextPage}
        />,
        <SendMovementPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          setValues={this.setValues}
        />,
      ];
    } else {
      formList = [
        <CreateStockMovement
          initialValues={this.state.values}
          onSubmit={this.nextPage}
        />,
        <AddItemsPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          goToPage={this.goToPage}
          onSubmit={this.nextPage}
        />,
        <EditPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          onSubmit={this.nextPage}
        />,
        <PickPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          onSubmit={this.nextPage}
        />,
        <SendMovementPage
          initialValues={this.state.values}
          previousPage={this.previousPage}
          setValues={this.setValues}
        />,
      ];
    }
    return formList;
  }

  setValues(values) {
    this.setState({ values });
  }

  /**
   * Returns shipment's name containing shipment's origin, destination, requisition date,
   * tracking number given by user on the last step, description and stock list if chosen.
   * @public
   */
  getShipmentName() {
    if (this.state.values.trackingNumber) {
      const {
        origin, destination, dateRequested, stocklist, trackingNumber, description,
      } = this.state.values;
      const stocklistPart = stocklist && stocklist.name ? `${stocklist.name}.` : '';
      const dateReq = moment(dateRequested, 'MM/DD/YYYY').format('DDMMMYYYY');
      const newName = `${origin.name}.${destination.name}.${dateReq}.${stocklistPart}${trackingNumber}.${description}`;
      return newName.replace(/ /gi, '');
    }
    return this.state.values.name;
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

          let page = 1;
          let prevPage = 1;
          switch (values.statusCode) {
            case 'CREATED':
              page = 2;
              prevPage = 1;
              break;
            case 'VERIFYING':
              page = 3;
              prevPage = 2;
              break;
            case 'PICKING':
              page = 4;
              prevPage = 3;
              break;
            case 'PICKED':
              page = 5;
              prevPage = 4;
              break;
            default:
              page = this.props.hasPackingSupport ? 6 : 5;
              if (values.origin.type === 'SUPPLIER' || !values.hasManageInventory) {
                prevPage = 2;
              } else if (this.props.hasPackingSupport) {
                prevPage = 5;
              } else {
                prevPage = 4;
              }
          }
          this.setState({ values, page, prevPage });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  /**
   * Sets current page state as a previous page and takes user to the next page.
   * @param {object} values
   * @public
   */
  nextPage(values) {
    this.setState({ prevPage: this.state.page, page: this.state.page + 1, values });
  }

  /**
   * Returns user to the previous page.
   * @param {object} values
   * @public
   */
  previousPage(values) {
    this.setState({ prevPage: this.state.prevPage - 1, page: this.state.prevPage, values });
  }

  /**
   * Sets current page state as a previous page and takes user to the given number page.
   * @param {object} values
   * @param {number} page
   * @public
   */
  goToPage(page, values) {
    this.setState({ prevPage: this.state.page, page, values });
  }

  render() {
    const { page, values } = this.state;

    const formList = this.getFormList(values.statusCode);
    const stepList = this.getStepList(values.statusCode);

    return (
      <div className="content-wrap">
        <div>
          <WizardSteps steps={stepList} currentStep={page} />
        </div>
        <div className="panel panel-primary">
          <div className="panel-heading movement-number">
            {(values.movementNumber && values.name && !values.trackingNumber) &&
              <span>{`${values.movementNumber} - ${values.name}`}</span>
            }
            {values.trackingNumber &&
              <span>{`${values.movementNumber} - ${this.getShipmentName()}`}</span>
            }
            {page === 6 ?
              <span className="shipment-status float-right"> {`${values.shipmentStatus ? values.shipmentStatus : 'PENDING'}`} </span> : null
            }
          </div>
          <div className="panelBody px-1">
            {formList[page - 1]}
          </div>
        </div>
      </div>
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
  /** React router's object which contains information about url varaiables and params */
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
