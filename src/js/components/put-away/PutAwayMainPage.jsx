import React, { Component } from 'react';

import PutAwayPage from './PutAwayPage';
import PutAwaySecondPage from './PutAwaySecondPage';
import PutAwayCheckPage from './PutAwayCheckPage';

class PutAwayMainPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: 0,
      props: null,
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
    this.firstPage = this.firstPage.bind(this);
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
        firstPage={this.firstPage}
      />,
    ];
  }

  nextPage(props) {
    this.setState({ page: this.state.page + 1, props });
  }

  prevPage(props) {
    this.setState({ page: this.state.page - 1, props });
  }

  firstPage() {
    this.setState({ page: 0, props: null });
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
