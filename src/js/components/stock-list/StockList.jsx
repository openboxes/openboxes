import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import filterFields from 'components/stock-list/FilterFields';
import StockListFilters from 'components/stock-list/StockListFilters';
import StockListHeader from 'components/stock-list/StockListHeader';
import StockListTable from 'components/stock-list/StockListTable';
import useStockListFilters from 'hooks/list-pages/stock-list/useStockListFilters';


const StockList = (props) => {
  const {
    defaultFilterValues, setFilterValues, locations, filterParams,
  } = useStockListFilters();

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stocklists');
    props.fetchTranslations(props.locale, 'reactTable');
  }, [props.locale]);

  return (
    <div className="d-flex flex-column list-page-main">
      <StockListHeader />
      <StockListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{ locations }}
      />
      <StockListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentLocation: state.session.currentLocation,
});

const mapDispatchToProps = {
  fetchTranslations,
};

StockList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(StockList));
