import React, { useState } from 'react';

import InvoiceListFilters from 'components/invoice/InvoiceListFilters';
import InvoiceListHeader from 'components/invoice/InvoiceListHeader';
import InvoiceListTable from 'components/invoice/InvoiceListTable';
import useTranslation from 'hooks/useTranslation';


const InvoiceList = () => {
  // Filter params are stored here, to be able to pass them to table component
  const [filterParams, setFilterParams] = useState({});

  useTranslation('invoice', 'reactTable');

  return (
    <div className="d-flex flex-column list-page-main">
      <InvoiceListHeader />
      <InvoiceListFilters setFilterParams={setFilterParams} />
      <InvoiceListTable filterParams={filterParams} />
    </div>
  );
};

export default InvoiceList;
