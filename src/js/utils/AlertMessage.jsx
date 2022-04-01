import React from 'react';

import PropTypes from 'prop-types';

const AlertMessage = ({
  show, message, danger, close, className,
}) => {
  if (show) {
    return (
      <div
        className={`${className} alert ${danger ? 'alert-danger' : 'alert-warning'}`}
        style={{ cursor: close ? 'pointer' : 'default', whiteSpace: 'pre-line' }}
        role="presentation"
        onClick={() => close()}
      >
        <i className={`fa ${danger ? 'fa-times-circle' : 'fa-exclamation-circle'} pr-2`} />
        {message}
      </div>);
  }

  return null;
};

export default AlertMessage;

AlertMessage.propTypes = {
  className: PropTypes.string,
  close: PropTypes.func,
  danger: PropTypes.bool,
  message: PropTypes.string,
  show: PropTypes.bool,
};

AlertMessage.defaultProps = {
  className: '',
  close: () => null,
  danger: false,
  message: '',
  show: false,
};
