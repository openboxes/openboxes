import React from 'react';

import { useSelector } from 'react-redux';

import useTranslate from 'hooks/useTranslate';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const InboundHeader = () => {
  const { headerInfo, headerStatus } = useSelector((state) => ({
    headerInfo: state.inbound.headerInfo,
    headerStatus: state.inbound.headerStatus,
  }));
  const translate = useTranslate();

  return (
    <HeaderWrapper className="align-items-center h-100 py-3">
      <div className="create-page-title d-flex align-items-center justify-content-between w-100">
        <h5 className="create-page-tile-main-content m-0 ">
          {headerInfo?.length > 0 && (
            <>
              <strong>{translate('react.stockMovement.label', 'Stock Movement')}</strong>
              <span>{' | '}</span>
              {headerInfo.map((item) => (
                <>
                  <span style={{ color: item.color }}>{item.text}</span>
                  {item.delimeter && <span>{item.delimeter}</span>}
                </>
              ))}
            </>
          )}
        </h5>
        {headerStatus?.text && (
          <span
            className={headerStatus.className}
          >
              {headerStatus.text}
          </span>
        )}
      </div>
    </HeaderWrapper>
  );
};

export default InboundHeader;
