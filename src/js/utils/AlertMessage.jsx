import React from 'react';
import PropTypes from 'prop-types';

const AlertMessage = ({
  show, message, danger, close,
}) => {
  if (show) {
    return (
      <div
        className={`alert ${danger ? 'alert-danger' : 'alert-warning'}`}
        style={{ cursor: close ? 'pointer' : 'default' }}
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
  show: PropTypes.bool,
  message: PropTypes.string,
  danger: PropTypes.bool,
  close: PropTypes.func,
};

AlertMessage.defaultProps = {
  show: false,
  message: '',
  danger: false,
  close: () => null,
};
