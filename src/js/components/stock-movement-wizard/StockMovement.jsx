import React, { Component } from 'react';
import CreateStockMovement from './CreateStockMovement';
import AddItemsPage from './AddItemsPage';
import EditPage from './EditPage';
import PickPage from './PickPage';
import SendMovementPage from './SendMovementPage';

class StockMovements extends Component {
  static showResults(values) {
    window.alert(`You submitted:\n\n${JSON.stringify(values, null, 2)}`);
  }

  constructor(props) {
    super(props);
    this.nextPage = this.nextPage.bind(this);
    this.previousPage = this.previousPage.bind(this);
    this.state = {
      page: 1,
    };
  }

  getFormList() {
    return [
      <CreateStockMovement
        onSubmit={this.nextPage}
      />,
      <AddItemsPage
        previousPage={this.previousPage}
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
    this.setState({ page: this.state.page + 1 });
  }

  previousPage() {
    this.setState({ page: this.state.page - 1 });
  }

  render() {
    const { page } = this.state;

    const formList = this.getFormList();

    return (
      <div className="container-fluid pt-2">
        <div>
          {formList[page - 1]}
        </div>
      </div>
    );
  }
}

export default StockMovements;
