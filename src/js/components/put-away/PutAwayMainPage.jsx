import React, { Component } from 'react';

import PutAwayPage from './PutAwayPage';
import PutAwaySecondPage from './PutAwaySecondPage';
import PutAwayCheckPage from './PutAwayCheckPage';

class PutAwayMainPage extends Component {
  static showResults(values) {
    window.alert(`You submitted:\n\n${JSON.stringify(values, null, 2)}`);
  }

  constructor(props) {
    super(props);

    this.state = {
      page: 0,
      props: null,
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
  }

  getFormList() {
    return [
      <PutAwayPage
        nextPage={this.nextPage}
      />,
      <PutAwaySecondPage
        {...this.state.props}
        nextPage={this.nextPage}
      />,
      <PutAwayCheckPage
        {...this.state.props}
        prevPage={this.prevPage}
        nextPage={PutAwayMainPage.showResults}
      />,
    ];
  }

  nextPage(props) {
    this.setState({ page: this.state.page + 1, props });
  }

  prevPage(props) {
    this.setState({ page: this.state.page - 1, props });
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

export default PutAwayMainPage;
