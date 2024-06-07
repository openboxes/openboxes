import React from 'react';

import useTranslate from "hooks/useTranslate";

const OutboundImportTitle = () => {
  const translate = useTranslate();
  return (
    <div className="create-page-title">
      <span className="create-page-tile-main-content">
        {translate('react.outboundImport.header.title', 'Import completed outbound')}
      </span>
    </div>
  );
};

export default OutboundImportTitle;
