import React from 'react';

import ProductSupplierFormHeader
  from 'components/productSupplier/create/ProductSupplierFormHeader';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

const ProductSupplierForm = () => {
  useTranslation('productSupplier');

  return (
    <PageWrapper>
      <ProductSupplierFormHeader />
    </PageWrapper>
  );
};

export default ProductSupplierForm;
