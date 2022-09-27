import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
import ProductsListFilters from 'components/products/ProductsListFilters';
import ProductsListHeader from 'components/products/ProductsListHeader';
import ProductsListTable from 'components/products/ProductsListTable';


const ProductsList = (props) => {
  // Filter params are stored here, to be able to pass them to table component
  const [filterParams, setFilterParams] = useState({});

  useEffect(() => {
    props.fetchTranslations(props.locale, 'purchaseOrder');
  }, [props.locale]);

  return (
    <div className="d-flex flex-column list-page-main">
      <ProductsListHeader />
      <ProductsListFilters setFilterParams={setFilterParams} />
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

export default connect(mapStateToProps, mapDispatchToProps)(ProductsList);


ProductsList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
};
