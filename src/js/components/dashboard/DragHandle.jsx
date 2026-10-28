import React from 'react';

import PropTypes from 'prop-types';

const DragHandle = ({ handleRef }) => (
  <span ref={handleRef} className="drag-handler">
    <i className="fa fa-ellipsis-v" />
  </span>
);

export default DragHandle;

DragHandle.propTypes = {
  handleRef: PropTypes.func,
};

DragHandle.defaultProps = {
  handleRef: null,
};
