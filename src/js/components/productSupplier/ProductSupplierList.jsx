import React from 'react';

import filterFields from 'components/productSupplier/FilterFields';
import ProductSupplierHeader from 'components/productSupplier/ProductSupplierHeader';
import ProductSupplierListFilters from 'components/productSupplier/ProductSupplierListFilters';
import ProductSupplierListTable from 'components/productSupplier/ProductSupplierListTable';
import ProductSupplierTabs from 'components/productSupplier/ProductSupplierTabs';
import { DETAILS_TAB } from 'consts/productSupplierList';
import useProductSupplierFilters from 'hooks/list-pages/productSupplier/useProductSupplierFilters';
import useQueryParams from 'hooks/useQueryParams';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import './styles.scss';

// Properties that should be ignored while clearing the filter form
const ignoreClearFilters = ['tab', 'active'];

const ProductSupplierList = () => {
  useTranslation('productSupplier');
  const parsedQueryParams = useQueryParams();

  const {
    defaultFilterValues,
    setFilterValues,
    filterParams,
  } = useProductSupplierFilters(ignoreClearFilters);

  return (
    <PageWrapper>
      <ProductSupplierHeader />
      <ProductSupplierTabs />
      <ProductSupplierListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        ignoreClearFilters={ignoreClearFilters}
      />
      {parsedQueryParams?.tab === DETAILS_TAB
        && <ProductSupplierListTable filterParams={filterParams} />}
    </PageWrapper>
  );
};

export default ProductSupplierList;
