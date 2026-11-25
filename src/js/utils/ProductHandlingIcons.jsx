import React from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import PropTypes from 'prop-types';

import productHandlingLabels from 'consts/productHandlingLabels';

const ProductHandlingIcons = ({ handlingLabels }) => {
  if (!handlingLabels?.length) {
    return null;
  }

  return (
    <span
      data-testid="product-handling-icons"
      className="d-inline-flex align-items-center align-middle"
    >
      {handlingLabels.map(({ labelCode, labelText }) => (
        <FontAwesomeIcon
          key={labelCode}
          className="ml-1"
          icon={productHandlingLabels[labelCode]?.icon}
          color={productHandlingLabels[labelCode]?.color}
          title={labelText}
        />
      ))}
    </span>
  );
};

export default ProductHandlingIcons;

ProductHandlingIcons.propTypes = {
  handlingLabels: PropTypes.arrayOf(PropTypes.shape({
    labelCode: PropTypes.string,
    labelText: PropTypes.string,
  })),
};

ProductHandlingIcons.defaultProps = {
  handlingLabels: [],
};
