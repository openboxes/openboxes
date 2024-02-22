import React from 'react';

import PropTypes from 'prop-types';
import { Portal } from 'react-overlays';

const RootPortalWrapper = ({ children }) => {
  const rootContainer = document.getElementById('root');

  return (
    <Portal container={rootContainer}>
      {children}
    </Portal>
  );
};

export default RootPortalWrapper;

RootPortalWrapper.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node,
  ]).isRequired,
};
