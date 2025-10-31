import React from 'react';

import PropTypes from 'prop-types';

const Spinner = ({ useSpinnerStyling }) => (
  <div className="d-flex w-100 h-100 justify-content-center align-items-center">
    <div className={`${useSpinnerStyling && 'circle-spinner'} spinner-border`} role="status">
      <span className="sr-only">Loading...</span>
    </div>
  </div>
);

export default Spinner;

Spinner.propTypes = {
  useSpinnerStyling: PropTypes.bool,
};

Spinner.defaultProps = {
  useSpinnerStyling: true,
};
