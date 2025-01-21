import React from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const InboundHeader = ({ title, status }) => {
  const translate = useTranslate();

  return (
    <HeaderWrapper className="align-items-center h-100 py-3">
      <div className="create-page-title d-flex align-items-center justify-content-between w-100">
        <h5 className="create-page-tile-main-content m-0 ">
          <strong>{translate('react.stockMovement.label', 'Stock Movement')}</strong>
          {title && (
            <>
              <span>{' | '}</span>
              {title.map((item) => (
                <>
                  <span style={{ color: item.color }}>{item.text}</span>
                  {item.delimeter && <span>{item.delimeter}</span>}
                </>
              ))}
            </>
          )}
        </h5>
        {status && (
          <span
            className={status.className}
          >
              {status.children}
          </span>
        )}
      </div>
    </HeaderWrapper>
  );
};

InboundHeader.defaultProps = {
  title: undefined,
  status: undefined,
};

InboundHeader.propTypes = {
  title: PropTypes.arrayOf(
    PropTypes.shape({
      text: PropTypes.string.isRequired,
      color: PropTypes.string,
      delimeter: PropTypes.string,
    }),
  ),
  status: PropTypes.shape({
    children: PropTypes.string,
    className: PropTypes.string,
  }),
};

export default InboundHeader;
