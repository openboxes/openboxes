import React, { Component } from 'react';

import PropTypes from 'prop-types';
import Alert from 'react-s-alert';

import Translate from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


const INITIAL_STATE = {};

const PAGE_ID = 'forecasting';

class Forecasting extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;
  }

  render() {
    return (
      <div className="d-flex flex-column">
        <div className="submit-buttons">
          <button type="button" onClick={() => Alert.info(this.props.supportLinks[PAGE_ID])} className="btn btn-outline-primary float-right btn-xs">
            <i className="fa fa-question-circle-o" aria-hidden="true" />
            &nbsp;
            <Translate id="react.default.button.support.label" defaultMessage="Support" />
          </button>
        </div>
        <div className="configuration-wizard-content" />
        <div className="submit-buttons">
          <button type="button" onClick={this.props.previousPage} className="btn btn-outline-primary float-left btn-xs">
            <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
          </button>
        </div>
      </div>
    );
  }
}

export default Forecasting;

Forecasting.propTypes = {
  previousPage: PropTypes.func.isRequired,
  supportLinks: PropTypes.shape({}).isRequired,
};
