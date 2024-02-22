import React from 'react';

import PropTypes from 'prop-types';
import { RiCheckboxCircleLine } from 'react-icons/all';

import Translate from 'utils/Translate';

const InvalidItemsIndicator = ({ className, errorsCounter }) => (
  <div className={`invalid-items-indicator ${className}`}>
    <span>
      <RiCheckboxCircleLine className="mr-1" />
      {errorsCounter}
      {' '}
      <Translate
        id="react.productSupplier.form.invalidItemsIndicator.title"
        defaultMessage="Item(s) require attention"
      />
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
