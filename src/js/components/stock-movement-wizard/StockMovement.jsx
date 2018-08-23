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

  static getStepList() {
    return ['Create', 'Add items', 'Edit', 'Pick', 'Send'];
  }

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

  nextPage() {
    this.setState({ prevPage: this.state.page, page: this.state.page + 1 });
  }

  previousPage() {
    this.setState({ prevPage: this.state.prevPage - 1, page: this.state.prevPage });
  }

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
  movementNumber: PropTypes.string,
  shipmentName: PropTypes.string,
  origin: PropTypes.shape({
    id: PropTypes.string,
    type: PropTypes.string,
  }),
  destination: PropTypes.shape({
    id: PropTypes.string,
    type: PropTypes.string,
  }),
  dateRequested: PropTypes.string,
  stockList: PropTypes.string,
  trackingNumber: PropTypes.string,
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
