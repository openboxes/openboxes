import React, { Component } from 'react';
import { initialize } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import PartialReceivingPage from './PartialReceivingPage';
import ReceivingCheckScreen from './ReceivingCheckScreen';
import apiClient, { parseResponse } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

class ReceivingPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: 0,
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
  }

  componentDidMount() {
    this.fetchPartialReceiptCandidates();
  }

  getFormList() {
    return [
      <PartialReceivingPage
        onSubmit={this.nextPage}
        shipmentId={this.props.match.params.shipmentId}
      />,
      <ReceivingCheckScreen
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

  fetchPartialReceiptCandidates() {
    this.props.showSpinner();
    const url = `/openboxes/api/partialReceiving/${this.props.match.params.shipmentId}`;

    return apiClient.get(url)
      .then((response) => {
        this.props.initialize('partial-receiving-wizard', parseResponse(response.data.data), false);
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
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

export default connect(null, { initialize, showSpinner, hideSpinner })(ReceivingPage);

ReceivingPage.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({ shipmentId: PropTypes.string }),
  }).isRequired,
  initialize: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
};
