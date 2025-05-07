import React from 'react';

import ListTitle from 'components/listPagesUtils/ListTitle';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const CycleCountReportingHeader = () => (
  <HeaderWrapper>
    <ListTitle label={{
      id: 'react.cycleCount.inventoryChangesReport.label',
      defaultMessage: 'Inventory Changes Report',
    }}
    />
  </HeaderWrapper>
);

export default CycleCountReportingHeader;
