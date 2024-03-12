import React from 'react';

import PropTypes from 'prop-types';

const HeaderButtonsWrapper = ({ children, className }) => (
  <div className={`d-flex justify-content-end buttons align-items-center ${className}`}>
    {children}
  </div>
);

export default HeaderButtonsWrapper;

HeaderButtonsWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

HeaderButtonsWrapper.defaultProps = {
  className: '',
};
