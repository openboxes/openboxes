import React from 'react';

import ProductSelect from 'components/product-select/ProductSelect';

import SelectField from './SelectField';

/**
 * A wrapper on SelectField, configured for a product dropdown selector
 */
const ProductSelectField = (props) => (
  <SelectField
    {...props}
    selectComponent={ProductSelect}
  />
);

export default ProductSelectField;
