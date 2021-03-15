import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import { showSpinner, hideSpinner } from '../../actions';
import Translate from '../../utils/Translate';

/* eslint-disable */
class ConfirmInvoicePage extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  render() {
    return (
      <div className="submit-buttons">
        <button
          className="btn btn-outline-primary btn-form btn-xs"
          onClick={() => this.props.previousPage(this.props.initialValues)}
        >
          <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
        </button>
        <button
          type="submit"
          className="btn btn-outline-success float-right btn-form btn-xs"
        >
          <Translate id="react.invoice.confirm.label" defaultMessage="Confirm Invoice" />
        </button>
      </div>
    );
  }
}

const mapStateToProps = state => ({});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(ConfirmInvoicePage);

ConfirmInvoicePage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
};
