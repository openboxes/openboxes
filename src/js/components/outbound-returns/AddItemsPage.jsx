/* TODO: Remove eslint disabler while finishing this page */
/* eslint-disable */
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import { showSpinner, hideSpinner } from '../../actions';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

class AddItemsPage extends Component {
  constructor(props) {
    super(props);
  }

  nextPage(values) {
    this.props.nextPage(values);
  }

  previousPage(values) {
    this.props.previousPage(values);
  }

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
            onClick={() => this.nextPage(this.props.initialValues)}
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

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner,
})(AddItemsPage));

AddItemsPage.propTypes = {
  initialValues: PropTypes.shape({}).isRequired,
  previousPage: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  outboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
};
