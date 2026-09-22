import React from 'react';

import ProductSelectField from 'components/form-elements/v2/ProductSelectField';

import SelectCell from './SelectCell';

/**
 * A wrapper on SelectCell, configured for a product dropdown selector.
 */
const ProductSelectCell = (props) => (
  <SelectCell
    {...props}
    selectFieldComponent={ProductSelectField}
  />
);

export default ProductSelectCell;
