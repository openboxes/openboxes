import React from 'react';

import PropTypes from 'prop-types';
import { RiCheckboxCircleLine } from 'react-icons/all';

const InvalidItemsIndicator = ({ className, errorsCounter }) => (
  <div className={`invalid-items-indicator ${className}`}>
    <span>
      <RiCheckboxCircleLine className="mr-1" />
      {errorsCounter}
      {' '}
      Item(s) require attention
    </span>
  </div>
);

export default InvalidItemsIndicator;

InvalidItemsIndicator.propTypes = {
  className: PropTypes.string,
  errorsCounter: PropTypes.number,
};

InvalidItemsIndicator.defaultProps = {
  className: '',
  errorsCounter: 0,
};
