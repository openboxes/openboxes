import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import { showSpinner, hideSpinner } from '../../actions';
import Translate from '../../utils/Translate';

/* eslint-disable */
class AddItemsPage extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  render() {
    return (
      <div className="submit-buttons">
        <button
          onClick={() => this.props.previousPage(this.props.initialValues)}
          className="btn btn-outline-primary btn-form btn-xs"
        >
          <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
        </button>
        <button
          onClick={() => this.props.nextPage(this.props.initialValues)}
          className="btn btn-outline-primary btn-form float-right btn-xs"
        >
          <Translate id="react.default.button.next.label" defaultMessage="Next" />
        </button>
      </div>
    );
  }
}

const mapStateToProps = state => ({});

export default (connect(mapStateToProps, { showSpinner, hideSpinner })(AddItemsPage));

AddItemsPage.propTypes = {
  initialValues: PropTypes.shape({}).isRequired,
  previousPage: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
};
