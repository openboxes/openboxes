import React from 'react';

import * as locales from 'date-fns/locale';
import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';
import { getCurrentLocale, getInboundHeaderInfo, getInboundHeaderStatus } from 'selectors';

import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const InboundHeader = ({ showHeaderStatus }) => {
  const headerInfo = useSelector(getInboundHeaderInfo);
  const headerStatus = useSelector(getInboundHeaderStatus);
  const currentLocale = useSelector(getCurrentLocale);
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
                <React.Fragment key={item.text}>
                  <span style={{ color: item.color }}>
                    {item.isDate
                      ? formatDateToString({
                        date: item.text,
                        dateFormat: DateFormatDateFns.DD_MMM_YYYY,
                        options: { locale: locales[currentLocale] },
                      })
                      : item.text}
                  </span>
                  {item.delimeter && <span>{item.delimeter}</span>}
                </React.Fragment>
              ))}
            </>
          )}
        </h5>
        {showHeaderStatus && headerStatus && (
          <span className="shipment-status">
            {headerStatus}
          </span>
        )}
      </div>
    </HeaderWrapper>
  );
};

export default InboundHeader;

InboundHeader.propTypes = {
  showHeaderStatus: PropTypes.bool,
};

InboundHeader.defaultProps = {
  showHeaderStatus: false,
};
