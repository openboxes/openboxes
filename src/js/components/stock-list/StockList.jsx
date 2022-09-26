import React, { useState } from 'react';

import StockListFilters from 'components/stock-list/StockListFilters';
import StockListHeader from 'components/stock-list/StockListHeader';
import StockListTable from 'components/stock-list/StockListTable';

const StockList = () => {
  const [filterParams, setFilterParams] = useState({});
  return (
    <div className="d-flex flex-column list-page-main">
      <StockListHeader />
      <StockListFilters setFilterParams={setFilterParams} />
      <StockListTable filterParams={filterParams} />
    </div>
  );
};

export default StockList;
