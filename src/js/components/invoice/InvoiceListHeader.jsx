import React from 'react';

import { withRouter } from 'react-router-dom';

import Translate from 'utils/Translate';

const InvoiceListHeader = () => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.invoice.list.label" defaultMessage="Invoice List" />
    </span>
  </div>
);

export default withRouter(InvoiceListHeader);

