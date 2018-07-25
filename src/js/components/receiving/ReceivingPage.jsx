import React, { Component } from 'react';

import PartialReceivingPage from './PartialReceivingPage';
import ReceivingCheckScreen from './ReceivingCheckScreen';

class ReceivingPage extends Component {
  static showResults(values) {
    window.alert(`You submitted:\n\n${JSON.stringify(values, null, 2)}`);
  }

  constructor(props) {
    super(props);

    this.state = {
      page: 0,
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
  }

  getFormList() {
    return [
      <PartialReceivingPage
        onSubmit={this.nextPage}
      />,
      <ReceivingCheckScreen
        onSubmit={ReceivingPage.showResults}
        prevPage={this.prevPage}
      />,
    ];
  }

  nextPage() {
    this.setState({ page: this.state.page + 1 });
  }

  prevPage() {
    this.setState({ page: this.state.page - 1 });
  }

  render() {
    const { page } = this.state;
    const formList = this.getFormList();

    return (
      <div>
        {formList[page]}
      </div>
    );
  }
}

export default ReceivingPage;
