/* TODO: Remove eslint disabler while finishing this page */
/* eslint-disable */
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import { showSpinner, hideSpinner } from '../../actions';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

class SendMovementPage extends Component {
  constructor(props) {
    super(props);
  }

  previousPage(values) {
    this.props.previousPage(values);
  }

  sendOutboundReturns(values) {}

  render() {
    return (
      <div>
        <div className="submit-buttons">
          <button
            type="submit"
            onClick={() => this.previousPage(this.props.initialValues)}
            className="btn btn-outline-primary btn-form btn-xs"
          >
            <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
          </button>
          <button
            type="submit"
            onClick={() => this.sendOutboundReturns(this.props.initialValues)}
            className="btn btn-outline-primary btn-form float-right btn-xs"
          >
            <Translate id="react.default.button.next.label" defaultMessage="Next" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  outboundReturnsTranslationsFetched: state.session.fetchedTranslations.outboundReturns,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(SendMovementPage);

SendMovementPage.propTypes = {
  initialValues: PropTypes.shape({}).isRequired,
  previousPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  outboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
};
