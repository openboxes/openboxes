import React from 'react';

import { withRouter } from 'react-router-dom';

import filterFields from 'components/stock-list/FilterFields';
import StockListFilters from 'components/stock-list/StockListFilters';
import StockListHeader from 'components/stock-list/StockListHeader';
import StockListTable from 'components/stock-list/StockListTable';
import useStockListFilters from 'hooks/list-pages/stock-list/useStockListFilters';
import useTranslation from 'hooks/useTranslation';


const StockList = () => {
  const {
    defaultFilterValues, setFilterValues, locations, filterParams,
  } = useStockListFilters();

  useTranslation('stocklists', 'reactTable');

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

export default withRouter(StockList);
