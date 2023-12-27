import React from 'react';

import ProductSupplierHeader from 'components/productSupplier/ProductSupplierHeader';
import ProductSupplierTabs from 'components/productSupplier/ProductSupplierTabs';
import useTranslation from 'hooks/useTranslation';
import ListWrapper from 'wrappers/ListWrapper';

import './styles.scss';

const ProductSupplierList = () => {
  useTranslation('productSupplier');
  return (
    <ListWrapper>
      <ProductSupplierHeader />
      <ProductSupplierTabs />
    </ListWrapper>
  );
};

export default ProductSupplierList;
