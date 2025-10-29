import React from 'react';

import ListTitle from 'components/listPagesUtils/ListTitle';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const ReorderReportHeader = () => (
  <HeaderWrapper>
    <ListTitle label={{
      id: 'react.report.reorder.header.label',
      defaultMessage: 'Reorder Report',
    }}
    />
  </HeaderWrapper>
);

export default ReorderReportHeader;
