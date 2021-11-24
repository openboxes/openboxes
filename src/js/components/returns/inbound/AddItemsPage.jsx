import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import 'react-table/react-table.css';
import 'react-datepicker/dist/react-datepicker.css';

import { hideSpinner } from '../../../actions';
import Translate from '../../../utils/Translate';

const INITIAL_STATE = {
  inboundReturn: {},
};

class AddItemsPage extends Component {
  constructor(props) {
    super(props);
    this.props.hideSpinner();
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
          onClick={() => this.props.nextPage()}
          className="btn btn-outline-primary btn-form float-right btn-xs"
        ><Translate id="react.replenishment.next.label" defaultMessage="Next" />
        </button>
      </div>
    );
  }
}

const mapStateToProps = () => ({});

export default (connect(mapStateToProps, { hideSpinner })(AddItemsPage));

AddItemsPage.propTypes = {
  previousPage: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
};
