import React, { useMemo } from 'react';

import { useSelector } from 'react-redux';
import { makeGetCycleCountProduct } from 'selectors';

const ProductDataHeader = ({ cycleCountId }) => {
  const getCycleCountProduct = useMemo(makeGetCycleCountProduct, []);

  const cycleCountProduct = useSelector(
    (state) => getCycleCountProduct(state, cycleCountId),
  );

  return (
    <p className="count-step-title pt-4 pl-4">
      {cycleCountProduct?.productCode}
      {' '}
      {cycleCountProduct?.name}
    </p>
  );
};

export default ProductDataHeader;
