import React from 'react';

import PropTypes from 'prop-types';

const DragHandle = ({ handleRef = null }) => (
  <span ref={handleRef} className="drag-handler">
    <i className="fa fa-ellipsis-v" />
  </span>
);

export default DragHandle;

DragHandle.propTypes = {
  handleRef: PropTypes.func,
};
