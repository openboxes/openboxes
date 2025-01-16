import React from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import HeaderWrapper from 'wrappers/HeaderWrapper';

import '../StockMovement.scss';

const InboundHeader = ({ title, additionalTitle }) => {
  const translate = useTranslate();

  const renderTitle = () => {
    if (!Array.isArray(title)) {
      return null;
    }

    return title.map((item, index) => (
      // eslint-disable-next-line react/no-array-index-key
      <React.Fragment key={index}>
        <span style={{ color: item.color }}>{item.text}</span>
        {item.delimeter && <span>{item.delimeter}</span>}
      </React.Fragment>
    ));
  };

  return (
    <HeaderWrapper className="align-items-center h-100 py-3">
      <div className="create-page-title d-flex align-items-center justify-content-between w-100">
        <h5 className="create-page-tile-main-content m-0 ">
          <strong>{translate('react.stockMovement.label', 'Stock Movement')}</strong>
          {title && (
            <>
              <span>{' | '}</span>
              {renderTitle()}
            </>
          )}
        </h5>
        {additionalTitle && (
          <span
            className={`${additionalTitle.className}`}
          >
              {additionalTitle.children}
          </span>
        )}
      </div>
    </HeaderWrapper>
  );
};

InboundHeader.defaultProps = {
  title: undefined,
  additionalTitle: undefined,
};

InboundHeader.propTypes = {
  title: PropTypes.arrayOf(
    PropTypes.shape({
      text: PropTypes.string.isRequired,
      color: PropTypes.string,
      delimeter: PropTypes.string,
    }),
  ),
  additionalTitle: PropTypes.shape({
    children: PropTypes.string,
    className: PropTypes.string,
  }),
};

export default InboundHeader;
