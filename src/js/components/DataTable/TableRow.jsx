import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

const TableRow = ({
  children, className = undefined, error = {},
}) => (
  <div
    role="row"
    className={`rt-tr ${className} ${!_.isEmpty(error) ? 'rt-invalid' : ''}`}
  >
    {children}
  </div>
);

TableRow.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node,
  ]).isRequired,
  className: PropTypes.string,
  error: PropTypes.shape({}),
};

export default TableRow;
