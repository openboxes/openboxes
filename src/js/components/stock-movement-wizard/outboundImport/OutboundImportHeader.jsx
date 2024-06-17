import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import HeaderWrapper from 'wrappers/HeaderWrapper';

const OutboundImportHeader = ({ stockMovementDetails }) => {
  const translate = useTranslate();
  return (
    <HeaderWrapper className="align-items-center h-100 py-3">
      <div className="create-page-title d-flex flex-column w-100">
        <h5 className="create-page-tile-main-content m-0">
          <strong>{translate('react.outboundImport.header.title', 'Import completed outbound')}</strong>
          {
            !_.isEmpty(stockMovementDetails) && (
            <>
              <span>{' | '}</span>
              <span className="text-blue-primary text-nowrap">
                {stockMovementDetails.origin}
              </span>
              <span>{` ${translate('react.default.to.label', 'to')} `}</span>
              <span className="text-blue-400 text-nowrap">
                {stockMovementDetails.destination}
              </span>
              <span>{`, ${stockMovementDetails.dateRequested}`}</span>
              <span>{`, ${stockMovementDetails.description}`}</span>
            </>
            )
          }
        </h5>
      </div>
    </HeaderWrapper>
  );
};

OutboundImportHeader.defaultProps = {
  stockMovementDetails: {},
};

OutboundImportHeader.propTypes = {
  stockMovementDetails: PropTypes.shape({
    origin: PropTypes.string,
    destination: PropTypes.string,
    description: PropTypes.string,
    dateRequested: PropTypes.string,
  }),
};

export default OutboundImportHeader;
