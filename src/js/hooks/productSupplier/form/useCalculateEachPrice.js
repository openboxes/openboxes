import { useEffect } from 'react';

import _ from 'lodash';
import { useWatch } from 'react-hook-form';

import { decimalParser } from 'utils/form-utils';

const useCalculateEachPrice = ({ control, setValue }) => {
  const packagePrice = useWatch({ control, name: 'packageSpecification.productPackagePrice' });
  const productPackageQuantity = useWatch({ control, name: 'packageSpecification.productPackageQuantity' });

  // eachPrice is a computed value from packagePrice and productPackageQuantity
  useEffect(() => {
    if (
      !_.isNil(packagePrice)
      && !_.isNil(productPackageQuantity)
      && productPackageQuantity !== 0
    ) {
      setValue('packageSpecification.eachPrice', decimalParser(packagePrice / productPackageQuantity, 4));
    } else {
      setValue('packageSpecification.eachPrice', '');
    }
  },
  [packagePrice, productPackageQuantity]);
};

export default useCalculateEachPrice;
