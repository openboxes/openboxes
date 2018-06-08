import React from 'react';
import { reduxForm } from 'redux-form';
import PropTypes from 'prop-types';

import validate from './validate';

const SendMovementPage = (props) => {
  const {
    handleSubmit, pristine, previousPage, submitting,
  } = props;
  return (
    <form onSubmit={handleSubmit}>
      SEND MOVEMENT PAGE
      <div>
        <button type="button" className="btn btn-outline-primary" onClick={previousPage}>
          Previous
        </button>
        <button type="submit" className="btn btn-outline-success" disabled={pristine || submitting}>Submit</button>
      </div>
    </form>
  );
};

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(SendMovementPage);

SendMovementPage.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  pristine: PropTypes.bool.isRequired,
  submitting: PropTypes.bool.isRequired,
};
