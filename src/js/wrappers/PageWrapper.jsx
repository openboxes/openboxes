import React from 'react';

import PropTypes from 'prop-types';

const PageWrapper = ({ children, className }) => (
  <div className={`d-flex flex-column list-page-main ${className}`}>
    {children}
  </div>
);

export default PageWrapper;

PageWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

PageWrapper.defaultProps = {
  className: '',
};
