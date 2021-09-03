/* TODO: Remove eslint disabler while finishing this page */
/* eslint-disable */
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { Tooltip } from 'react-tippy';

import 'react-table/react-table.css';

import { showSpinner, hideSpinner } from '../../actions';
import Translate from '../../utils/Translate';

class ReplenishmentSecondPage extends Component {
  constructor(props) {
    super(props);
  }

  nextPage() {
    this.props.nextPage({});
  }

  render() {
    return (
      <div className="replenishment">
        <div className="submit-buttons">
          <button
            type="button"
            onClick={() => this.nextPage()}
            className="btn btn-outline-primary btn-form float-right btn-xs"
          ><Translate id="react.default.button.next.label" defaultMessage="Next" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  replenishmentTranslationsFetched: state.session.fetchedTranslations.replenishment,
});

export default connect(
  mapStateToProps,
  {
    showSpinner, hideSpinner,
  },
)(ReplenishmentSecondPage);

ReplenishmentSecondPage.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({
    replenishment: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      replenishmentId: PropTypes.string,
    }),
  }).isRequired,
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  replenishmentTranslationsFetched: PropTypes.bool.isRequired,
};
