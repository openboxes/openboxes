import React, { Component } from 'react';
import CreateStockMovement from './CreateStockMovement';
import AddItemsPage from './AddItemsPage';
import EditPage from './EditPage';
import PickPage from './PickPage';
import SendMovementPage from './SendMovementPage';
import WizardSteps from '../form-elements/WizardSteps';

class StockMovements extends Component {
  static getStepList() {
    return ['Create', 'Add items', 'Edit', 'Pick', 'Send'];
  }

  static showResults(values) {
    window.alert(`You submitted:\n\n${JSON.stringify(values, null, 2)}`);
  }

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
        onSubmit={StockMovements.showResults}
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
      <div className="container-fluid pt-2">
        <div className="pb-3">
          <WizardSteps steps={StockMovements.getStepList()} currentStep={this.state.page} />
        </div>
        <div>
          {formList[page - 1]}
        </div>
      </div>
    );
  }
}

export default StockMovements;
