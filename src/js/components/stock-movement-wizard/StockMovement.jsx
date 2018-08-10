import React, { Component } from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';

import CreateStockMovement from './CreateStockMovement';
import AddItemsPage from './AddItemsPage';
import EditPage from './EditPage';
import PickPage from './PickPage';
import SendMovementPage from './SendMovementPage';
import WizardSteps from '../form-elements/WizardSteps';

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
  }

  /**
   * Returns array of form steps.
   * @public
   */
  static getStepList() {
    return ['Create', 'Add items', 'Edit', 'Pick', 'Send'];
  }

  /**
   * Returns array of form's components.
   * @public
   */
  getFormList() {
    return [
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
      />,
    ];
  }

  /**
   * Returns shipment's name containing shipment's origin, destination, requisition date,
   * tracking number given by user on the last step, description and stock list if chosen.
   * @public
   */
  getShipmentName() {
    if (this.state.values.trackingNumber) {
      const {
        origin, destination, dateRequested, stockList, trackingNumber, description,
      } = this.state.values;
      const stocklistPart = stockList.name ? `${stockList.name}.` : '';
      const dateReq = moment(dateRequested, 'MM/DD/YYYY').format('DDMMMYYYY');
      const newName = `${origin.name}.${destination.name}.${dateReq}.${stocklistPart}${trackingNumber}.${description}`;
      return newName.replace(/ /gi, '');
    }
    return this.state.values.shipmentName;
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

    const formList = this.getFormList();

    return (
      <div>
        <div>
          <WizardSteps steps={StockMovements.getStepList()} currentStep={page} />
        </div>
        <div className="panel panel-primary">
          <div className="panel-heading movement-number">
            {(values.movementNumber && values.shipmentName && !values.trackingNumber) &&
              <span>{`${values.movementNumber} - ${values.shipmentName}`}</span>
            }
            {values.trackingNumber &&
              <span>{`${values.movementNumber} - ${this.getShipmentName()}`}</span>
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

export default StockMovements;

StockMovements.propTypes = {
  /** Initial components' data */
  initialValues: PropTypes.shape({}),
};

StockMovements.defaultProps = {
  initialValues: {},
};
