import React from 'react';

import BaseField from 'components/form-elements/BaseField';
import ProductSelect from 'components/product-select/ProductSelect';

const ProductSelectField = (props) => {
  // eslint-disable-next-line react/prop-types
  const renderInput = ({ className, ...attributes }) => (
    <ProductSelect
      {...attributes}
      name={attributes.id}
      className={`select-xs ${className || ''}`}
      classNamePrefix="react-select"
    />
  );

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default ProductSelectField;
