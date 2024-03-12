import React from 'react';

import PropTypes from 'prop-types';

const HeaderWrapper = ({ children, className }) => (
  <div className={`d-flex list-page-header ${className}`}>
    {children}
  </div>
);

export default HeaderWrapper;

HeaderWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

HeaderWrapper.defaultProps = {
  className: '',
};
