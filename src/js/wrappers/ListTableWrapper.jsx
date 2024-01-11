import React from 'react';

import PropTypes from 'prop-types';

const ListTableWrapper = ({ children, className }) => (
  <div className={`list-page-list-section ${className}`}>
    {children}
  </div>
);

export default ListTableWrapper;

ListTableWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

ListTableWrapper.defaultProps = {
  className: '',
};
