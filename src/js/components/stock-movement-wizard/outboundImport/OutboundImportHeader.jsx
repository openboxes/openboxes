import React from 'react';

import useTranslate from 'hooks/useTranslate';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const OutboundImportHeader = () => {
  const translate = useTranslate();
  return (
    <HeaderWrapper className="align-items-end h-100 pt-3">
      <div className="create-page-title">
        <span className="create-page-tile-main-content">
          {translate('react.outboundImport.header.title', 'Import completed outbound')}
        </span>
      </div>
    </HeaderWrapper>
  );
};

export default OutboundImportHeader;
