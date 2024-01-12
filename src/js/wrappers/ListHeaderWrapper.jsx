import React from 'react';

import PropTypes from 'prop-types';

const ListHeaderWrapper = ({ children, className }) => (
  <div className={`d-flex list-page-header ${className}`}>
    {children}
  </div>
);

export default ListHeaderWrapper;

ListHeaderWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

ListHeaderWrapper.defaultProps = {
  className: '',
};
