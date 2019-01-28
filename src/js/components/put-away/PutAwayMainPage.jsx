import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import PutAwayPage from './PutAwayPage';
import PutAwaySecondPage from './PutAwaySecondPage';
import PutAwayCheckPage from './PutAwayCheckPage';
import apiClient, { parseResponse } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

/** Main put-away form's component. */
class PutAwayMainPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: props.match.params.putAwayId ? 1 : 0,
      props: { putAway: {} },
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
    this.firstPage = this.firstPage.bind(this);
  }

  componentDidMount() {
    if (this.props.match.params.putAwayId) {
      this.props.showSpinner();

      const url = `/openboxes/api/putaways/${this.props.match.params.putAwayId}`;

      apiClient.get(url)
        .then((response) => {
          const putAway = parseResponse(response.data.data);
          putAway.putawayItems = _.map(putAway.putawayItems, item => ({
            _id: _.uniqueId('item_'),
            ...item,
            splitItems: _.map(item.splitItems, splitItem => ({ _id: _.uniqueId('item_'), ...splitItem })),
          }));

          this.props.hideSpinner();

          this.setState({ props: { putAway }, page: putAway.putawayStatus === 'COMPLETED' ? 2 : 1 });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  /**
   * Returns array of form's components.
   * @public
   */
  getFormList(location) {
    return [
      <PutAwayPage
        nextPage={this.nextPage}
        locationId={location.id}
      />,
      <PutAwaySecondPage
        {...this.state.props}
        nextPage={this.nextPage}
        location={location}
      />,
      <PutAwayCheckPage
        {...this.state.props}
        prevPage={this.prevPage}
        firstPage={this.firstPage}
        location={location}
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
    this.props.history.push('/openboxes/putAway/create');
    this.setState({ page: 0, props: null });
  }

  render() {
    const { page } = this.state;
    const { location } = this.props;

    if (_.get(location, 'id')) {
      const formList = this.getFormList(location);

      return (
        <div>
          {formList[page]}
        </div>
      );
    }

    return null;
  }
}

const mapStateToProps = state => ({
  location: state.session.currentLocation,
});

export default withRouter(connect(mapStateToProps, { showSpinner, hideSpinner })(PutAwayMainPage));

PutAwayMainPage.propTypes = {
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ putAwayId: PropTypes.string }),
  }).isRequired,
  /** React router's object used to manage session history */
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
