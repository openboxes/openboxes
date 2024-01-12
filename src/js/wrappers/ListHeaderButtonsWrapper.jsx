import React from 'react';

import PropTypes from 'prop-types';

const ListHeaderButtonsWrapper = ({ children, className }) => (
  <div className={`d-flex justify-content-end buttons align-items-center ${className}`}>
    {children}
  </div>
);

export default ListHeaderButtonsWrapper;

ListHeaderButtonsWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

ListHeaderButtonsWrapper.defaultProps = {
  className: '',
};
