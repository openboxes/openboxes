import React from 'react';

import ProductSupplierHeader from 'components/productSupplier/ProductSupplierHeader';
import ProductSupplierListTable from 'components/productSupplier/ProductSupplierListTable';
import ProductSupplierTabs from 'components/productSupplier/ProductSupplierTabs';
import { DETAILS_TAB } from 'consts/productSupplierList';
import useQueryParams from 'hooks/useQueryParams';
import useTranslation from 'hooks/useTranslation';
import ListWrapper from 'wrappers/ListWrapper';

import './styles.scss';

const ProductSupplierList = () => {
  useTranslation('productSupplier');
  const parsedQueryParams = useQueryParams();

  return (
    <ListWrapper>
      <ProductSupplierHeader />
      <ProductSupplierTabs />
      {parsedQueryParams?.tab === DETAILS_TAB && <ProductSupplierListTable />}
    </ListWrapper>
  );
};

export default ProductSupplierList;
