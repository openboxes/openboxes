import React from 'react';
import PropTypes from 'prop-types';

const Loading = ({ error, pastDelay, retry }) => {
  if (error) {
    return (
      <div className="text-center">
        <div className="my-3">Error occurred when loading the component</div>
        <button className="btn btn-outline-primary my-3" onClick={retry}>Retry</button>
      </div>
    );
  } else if (pastDelay) {
    return <div className="text-center">Loading...</div>;
  }

  return null;
};

export default Loading;

Loading.propTypes = {
  pastDelay: PropTypes.bool.isRequired,
  retry: PropTypes.func.isRequired,
  error: PropTypes.bool,
};

Loading.defaultProps = {
  error: false,
};
