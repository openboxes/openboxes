import React, { Component } from 'react';
import { formValueSelector } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import moment from 'moment';

import CreateStockMovement from './CreateStockMovement';
import AddItemsPage from './AddItemsPage';
import EditPage from './EditPage';
import PickPage from './PickPage';
import SendMovementPage from './SendMovementPage';
import WizardSteps from '../form-elements/WizardSteps';

/** Main stock movement form's component. */
class StockMovements extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: 1,
      prevPage: 1,
    };

    this.nextPage = this.nextPage.bind(this);
    this.previousPage = this.previousPage.bind(this);
    this.goToPage = this.goToPage.bind(this);
  }

  /**
   * Return array of form steps
   * @public
   */
  static getStepList() {
    return ['Create', 'Add items', 'Edit', 'Pick', 'Send'];
  }

  /**
   * Return array of form's components
   * @public
   */
  getFormList() {
    return [
      <CreateStockMovement
        onSubmit={this.nextPage}
      />,
      <AddItemsPage
        previousPage={this.previousPage}
        goToPage={this.goToPage}
        onSubmit={this.nextPage}
      />,
      <EditPage
        previousPage={this.previousPage}
        onSubmit={this.nextPage}
      />,
      <PickPage
        previousPage={this.previousPage}
        onSubmit={this.nextPage}
      />,
      <SendMovementPage
        previousPage={this.previousPage}
      />,
    ];
  }

  /**
   * Return shipment's name containing shipment's origin, destination, requisition date,
   * tracking number given by user on the last step, description and stock list if chosen
   * @public
   */
  getShipmentName() {
    if (this.props.trackingNumber) {
      const {
        origin, destination, dateRequested, stockList, trackingNumber, description,
      } = this.props;
      const stocklistPart = stockList.name ? `${stockList.name}.` : '';
      const dateReq = moment(dateRequested, 'MM/DD/YYYY').format('DDMMMYYYY');
      const newName = `${origin.name}.${destination.name}.${dateReq}.${stocklistPart}${trackingNumber}.${description}`;
      return newName.replace(/ /gi, '');
    }
    return this.props.shipmentName;
  }

  /**
   * Set current page state as a previous page and take user to the next page
   * @public
   */
  nextPage() {
    this.setState({ prevPage: this.state.page, page: this.state.page + 1 });
  }

  /**
   * Return user to the previous page
   * @public
   */
  previousPage() {
    this.setState({ prevPage: this.state.prevPage - 1, page: this.state.prevPage });
  }

  /**
   * Set current page state as a previous page and takes user to the given number page
   * @param {number} page
   * @public
   */
  goToPage(page) {
    this.setState({ prevPage: this.state.page, page });
  }

  render() {
    const { page } = this.state;

    const formList = this.getFormList();

    return (
      <div>
        <div>
          <WizardSteps steps={StockMovements.getStepList()} currentStep={this.state.page} />
        </div>
        <div className="panel panel-primary">
          <div className="panel-heading movement-number">
            {(this.props.movementNumber && this.props.shipmentName && !this.props.trackingNumber) &&
              <span>{`${this.props.movementNumber} - ${this.props.shipmentName}`}</span>
            }
            {this.props.trackingNumber &&
              <span>{`${this.props.movementNumber} - ${this.getShipmentName()}`}</span>
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

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  shipmentName: selector(state, 'shipmentName'),
  movementNumber: selector(state, 'movementNumber'),
  origin: selector(state, 'origin'),
  destination: selector(state, 'destination'),
  dateRequested: selector(state, 'dateRequested'),
  stockList: selector(state, 'stockList'),
  trackingNumber: selector(state, 'trackingNumber'),
  description: selector(state, 'description'),
});

export default connect(mapStateToProps, {})(StockMovements);

StockMovements.propTypes = {
  /** Automatically generated unique stock movement's number */
  movementNumber: PropTypes.string,
  /**
   * Shipment name containing shipment's origin, destination, requisition date,
   * tracking number, description and stock list if chosen
   */
  shipmentName: PropTypes.string,
  /** Chosen origin */
  origin: PropTypes.shape({
    /** Origin's ID */
    id: PropTypes.string,
    /** Origin's type. Can be either "depot" or "supplier" */
    type: PropTypes.string,
  }),
  /** Chosen destination */
  destination: PropTypes.shape({
    /** Destination's ID */
    id: PropTypes.string,
    /** Destination's type. Can be either "depot" or "supplier" */
    type: PropTypes.string,
  }),
  /** Date of the requisition */
  dateRequested: PropTypes.string,
  /** Chosen stock list */
  stockList: PropTypes.string,
  /** Tracking number given by the user on the last step */
  trackingNumber: PropTypes.string,
  /** A short description of the requisition */
  description: PropTypes.string,
};

StockMovements.defaultProps = {
  movementNumber: '',
  shipmentName: '',
  origin: {},
  destination: {},
  dateRequested: '',
  stockList: '',
  trackingNumber: '',
  description: '',
};
