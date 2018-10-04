import React, { Component } from 'react';
import _ from 'lodash';

import apiClient from '../../utils/apiClient';
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
      locationId: '',
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
    this.firstPage = this.firstPage.bind(this);
  }

  componentDidMount() {
    this.getCurrentLocation();
  }

  getCurrentLocation() {
    const url = '/openboxes/api/getSession';

    return apiClient.get(url)
      .then((response) => {
        const locationId = _.get(response, 'data.data.location.id');

        this.setState({ locationId });
      });
  }

  /**
   * Returns array of form's components.
   * @public
   */
  getFormList(locationId) {
    return [
      <PutAwayPage
        nextPage={this.nextPage}
        locationId={locationId}
      />,
      <PutAwaySecondPage
        {...this.state.props}
        nextPage={this.nextPage}
        locationId={locationId}
      />,
      <PutAwayCheckPage
        {...this.state.props}
        prevPage={this.prevPage}
        firstPage={this.firstPage}
        locationId={locationId}
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
    const { page, locationId } = this.state;

    if (locationId) {
      const formList = this.getFormList(locationId);

      return (
        <div>
          {formList[page]}
        </div>
      );
    }

    return null;
  }
}

export default PutAwayMainPage;
