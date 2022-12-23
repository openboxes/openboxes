import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import filterFields from 'components/products/FilterFields';
import ProductsListFilters from 'components/products/ProductsListFilters';
import ProductsListHeader from 'components/products/ProductsListHeader';
import ProductsListTable from 'components/products/ProductsListTable';
import useProductFilters from 'hooks/useProductFilters';


const ProductsList = (props) => {
  const {
    defaultFilterValues, setFilterValues, categories, catalogs, tags, filterParams,
  } = useProductFilters();

  useEffect(() => {
    props.fetchTranslations(props.locale, 'productsList');
    props.fetchTranslations(props.locale, 'reactTable');
  }, [props.locale]);

  return (
    <div className="d-flex flex-column list-page-main">
      <ProductsListHeader />
      <ProductsListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{ categories, catalogs, tags }}
      />
      <ProductsListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

const mapDispatchToProps = {
  fetchTranslations,
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(ProductsList));


ProductsList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,

};
