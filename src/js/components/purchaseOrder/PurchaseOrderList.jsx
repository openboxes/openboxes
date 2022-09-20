import React, { useState } from 'react';

import PurchaseOrderListFilters from 'components/purchaseOrder/PurchaseOrderListFilters';
import PurchaseOrderListHeader from 'components/purchaseOrder/PurchaseOrderListHeader';
import PurchaseOrderListTable from 'components/purchaseOrder/PurchaseOrderListTable';

import 'components/purchaseOrder/PurchaseOrderListStyles.scss';

const PurchaseOrderList = () => {
  // Filter params are stored here, to be able to pass them to table component
  const [filterParams, setFilterParams] = useState({});
  return (
    <div className="d-flex flex-column purchase-order-list-main">
      <PurchaseOrderListHeader />
      <PurchaseOrderListFilters setFilterParams={setFilterParams} />
      <PurchaseOrderListTable filterParams={filterParams} />
    </div>
  );
};

export default PurchaseOrderList;
