import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

const AlertMessage = ({
  show,
  message,
  danger,
  close,
  className,
}) => {
  const getMessageElement = useMemo(() => {
    const alertMessages = typeof message !== 'string' && Array.isArray(message) ? message : [message];
    return alertMessages.map((msg) => (
      <div>
        <i className={`fa ${danger ? 'fa-times-circle' : 'fa-exclamation-circle'} pr-2`} />
        {msg}
      </div>
    ));
  }, [message]);

  if (show) {
    return (
      <div
        data-testid="alert-message"
        className={`${className} alert ${danger ? 'alert-danger' : 'alert-warning'}`}
        style={{
          cursor: close ? 'pointer' : 'default',
          whiteSpace: 'pre-line',
        }}
        role="presentation"
        onClick={() => close()}
      >

        {getMessageElement}
      </div>
    );
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
