import React, { Component } from 'react';
import { formValueSelector } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

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
            { (this.props.movementNumber && this.props.description) &&
            <span>{`${this.props.movementNumber} - ${this.props.description}`}</span>
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
  description: selector(state, 'description'),
  movementNumber: selector(state, 'movementNumber'),
});

export default connect(mapStateToProps, {})(StockMovements);

StockMovements.propTypes = {
  movementNumber: PropTypes.string,
  description: PropTypes.string,
};

StockMovements.defaultProps = {
  movementNumber: '',
  description: '',
};
