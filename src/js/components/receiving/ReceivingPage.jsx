import React, { Component } from 'react';
import PropTypes from 'prop-types';

import PartialReceivingPage from './PartialReceivingPage';
import ReceivingCheckScreen from './ReceivingCheckScreen';

class ReceivingPage extends Component {
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

ReceivingPage.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({ shipmentId: PropTypes.string }),
  }).isRequired,
};
