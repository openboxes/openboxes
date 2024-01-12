import React from 'react';

import PropTypes from 'prop-types';

const ListWrapper = ({ children, className }) => (
  <div className={`d-flex flex-column list-page-main ${className}`}>
    {children}
  </div>
);

export default ListWrapper;

ListWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

ListWrapper.defaultProps = {
  className: '',
};
