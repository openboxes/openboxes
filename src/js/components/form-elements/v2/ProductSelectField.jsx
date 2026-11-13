import React from 'react';

import PropTypes from 'prop-types';

import ProductSelect from 'components/product-select/ProductSelect';

import SelectField from './SelectField';

/**
 * A wrapper on SelectField, configured for a product dropdown selector
 */
const ProductSelectField = ({
  // Strip out any provided labelKey to ensure we always get the default display text behaviour
  // from ProductSelect (which is triggered when labelKey is falsy).
  labelKey = undefined, // eslint-disable-line no-unused-vars
  ...props
}) => (
  <SelectField
    {...props}
    selectComponent={ProductSelect}
  />
);

ProductSelectField.propTypes = {
  labelKey: PropTypes.string,
};

export default ProductSelectField;
