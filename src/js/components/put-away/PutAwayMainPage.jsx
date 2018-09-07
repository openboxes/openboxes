import React, { Component } from 'react';

import PutAwayPage from './PutAwayPage';
import PutAwaySecondPage from './PutAwaySecondPage';
import PutAwayCheckPage from './PutAwayCheckPage';

/** Main put-away form's component. */
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

  /**
   * Returns array of form's components.
   * @public
   */
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

  /**
   * Takes user to the next page of put-away.
   * @param {object} props
   * @public
   */
  nextPage(props) {
    this.setState({ page: this.state.page + 1, props });
  }

  /**
   * Returns user to the previous page of put-away.
   * @param {object} props
   * @public
   */
  prevPage(props) {
    this.setState({ page: this.state.page - 1, props });
  }

  /**
   * Takes user to the first page of put-away.
   * @public
   */
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
