import React from 'react';

import ListTitle from 'components/listPagesUtils/ListTitle';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const ExpirationHistoryReportHeader = () => (
  <HeaderWrapper>
    <ListTitle label={{
      id: 'react.report.expirationHistory.header.label',
      defaultMessage: 'Expiration History Report',
    }}
    />
  </HeaderWrapper>
);

export default ExpirationHistoryReportHeader;
