import React from 'react';

import { withRouter } from 'react-router-dom';

import filterFields from 'components/products/FilterFields';
import ProductsListFilters from 'components/products/ProductsListFilters';
import ProductsListHeader from 'components/products/ProductsListHeader';
import ProductsListTable from 'components/products/ProductsListTable';
import useProductFilters from 'hooks/list-pages/product/useProductFilters';
import useTranslation from 'hooks/useTranslation';


const ProductsList = () => {
  const {
    defaultFilterValues,
    setFilterValues,
    categories,
    catalogs,
    tags,
    glAccounts,
    filterParams,
    productGroups,
  } = useProductFilters();

  useTranslation('productsList', 'reactTable');

  return (
    <div className="d-flex flex-column list-page-main">
      <ProductsListHeader />
      <ProductsListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{
         categories, catalogs, tags, glAccounts, productGroups,
        }}
      />
      <ProductsListTable filterParams={filterParams} />
    </div>
  );
};

export default withRouter(ProductsList);
