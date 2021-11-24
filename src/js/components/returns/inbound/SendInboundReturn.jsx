import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import 'react-confirm-alert/src/react-confirm-alert.css';

import Translate from '../../../utils/Translate';

const INITIAL_STATE = {
  inboundReturn: {},
};

class SendInboundPage extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  render() {
    return (
      <div className="submit-buttons d-flex justify-content-between">
        <button
          type="button"
          onClick={() => this.props.previousPage()}
          className="btn btn-outline-primary btn-form float-right btn-xs"
        ><Translate id="react.replenishment.next.label" defaultMessage="Previous" />
        </button>
        <button
          type="button"
          className="btn btn-outline-primary btn-form float-right btn-xs"
        ><Translate id="react.replenishment.next.label" defaultMessage="Next" />
        </button>
      </div>
    );
  }
}

const mapStateToProps = () => ({});

export default connect(mapStateToProps, {})(SendInboundPage);

SendInboundPage.propTypes = {
  previousPage: PropTypes.func.isRequired,
};
