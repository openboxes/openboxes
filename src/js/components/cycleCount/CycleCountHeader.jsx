import React from 'react';

import ListTitle from 'components/listPagesUtils/ListTitle';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const CycleCountHeader = () => (
  <HeaderWrapper>
    <ListTitle label={{
      id: 'react.cycleCount.headerTitle.label',
      defaultMessage: 'Cycle Count',
    }}
    />
  </HeaderWrapper>
);

export default CycleCountHeader;
