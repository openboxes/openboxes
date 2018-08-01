import _ from 'lodash';
import React, { Component } from 'react';
import { initialize, formValueSelector } from 'redux-form';
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
      bins: [],
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
        bins={this.state.bins}
      />,
      <ReceivingCheckScreen
        prevPage={this.prevPage}
        shipmentId={this.props.match.params.shipmentId}
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
        this.fetchBins();
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchBins() {
    const url = '/openboxes/api/internalLocations';

    return apiClient.get(url)
      .then((response) => {
        const bins = _.map(response.data.data, bin => (
          { value: { id: bin.id, name: bin.name }, label: bin.name }
        ));
        this.setState({ bins }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const { page } = this.state;
    const formList = this.getFormList();

    return (
      <div>
        {this.props.shipmentNumber &&
        <h2 className="my-2 text-center">{`${this.props.shipmentNumber} ${this.props.shipmentName}`}</h2>}
        <div className="align-self-center">
          {formList[page]}
        </div>
      </div>
    );
  }
}

const selector = formValueSelector('partial-receiving-wizard');

const mapStateToProps = state => ({
  shipmentNumber: selector(state, 'shipment.shipmentNumber'),
  shipmentName: selector(state, 'shipment.name'),
});

export default connect(mapStateToProps, { initialize, showSpinner, hideSpinner })(ReceivingPage);

ReceivingPage.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({ shipmentId: PropTypes.string }),
  }).isRequired,
  initialize: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  shipmentNumber: PropTypes.string,
  shipmentName: PropTypes.string,
};

ReceivingPage.defaultProps = {
  shipmentNumber: '',
  shipmentName: '',
};
