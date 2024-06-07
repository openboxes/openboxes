import React from 'react';

import OutboundImportTitle from 'components/stock-movement-wizard/outboundImport/OutboundImportTitle';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const OutboundImportHeader = () => (
  <HeaderWrapper className="align-items-end h-100 pt-3">
    <OutboundImportTitle />
  </HeaderWrapper>
);

export default OutboundImportHeader;
