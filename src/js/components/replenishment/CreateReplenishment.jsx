/* TODO: Remove eslint disabler while finishing this page */
/* eslint-disable */
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import 'react-table/react-table.css';

import { showSpinner, hideSpinner } from '../../actions';
import Translate from '../../utils/Translate';

class CreateReplenishment extends Component {
  constructor(props) {
    super(props);
  }

  createReplenishment() {
    this.props.nextPage({});
  }

  render() {

    return (
      <div className="replenishment">
        <div className="submit-buttons">
          <button
            type="button"
            onClick={() => this.createReplenishment()}
            className="btn btn-outline-primary btn-form float-right btn-xs"
          ><Translate id="react.replenishment.startReplenishment.label" defaultMessage="Start Replenishment" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  replenishmentTranslationsFetched: state.session.fetchedTranslations.replenishment,
});

export default withRouter(connect(
  mapStateToProps,
  {
    showSpinner, hideSpinner,
  },
)(CreateReplenishment));

CreateReplenishment.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  locationId: PropTypes.string.isRequired,
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
  replenishmentTranslationsFetched: PropTypes.bool.isRequired,
};
