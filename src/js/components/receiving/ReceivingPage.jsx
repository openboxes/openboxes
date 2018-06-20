import React, { Component } from 'react';

import PartialReceivingPage from './PartialReceivingPage';

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
  }

  static getFormList() {
    return [
      <PartialReceivingPage
        onSubmit={ReceivingPage.showResults}
      />,
    ];
  }

  nextPage() {
    this.setState({ page: this.state.page + 1 });
  }

  render() {
    const { page } = this.state;
    const formList = ReceivingPage.getFormList();

    return (
      <div>
        {formList[page]}
      </div>
    );
  }
}

export default ReceivingPage;
