import React from 'react';

import PropTypes from 'prop-types';

import ConfirmReceiptButtons from 'components/receivingV2/ConfirmReceiptButtons';
import useTranslate from 'hooks/useTranslate';

/**
 * Check step header rendered above the main content section: the "Confirm Receipt"
 * title with the action buttons on the right.
 */
const ConfirmReceiptHeader = ({ onBackToReceive, onCompleteReceipt, isCompleteDisabled }) => {
  const translate = useTranslate();

  return (
    <div className="d-flex justify-content-between align-items-center mb-3" data-testid="confirm-receipt-header">
      <h5 className="m-0 font-weight-500 font-size-md">
        {translate('react.receiving.confirmReceipt.label', 'Confirm Receipt')}
      </h5>
      <ConfirmReceiptButtons
        onBackToReceive={onBackToReceive}
        onCompleteReceipt={onCompleteReceipt}
        isCompleteDisabled={isCompleteDisabled}
      />
    </div>
  );
};

ConfirmReceiptHeader.propTypes = {
  onBackToReceive: PropTypes.func,
  onCompleteReceipt: PropTypes.func,
  isCompleteDisabled: PropTypes.bool,
};

ConfirmReceiptHeader.defaultProps = {
  onBackToReceive: undefined,
  onCompleteReceipt: undefined,
  isCompleteDisabled: false,
};

export default ConfirmReceiptHeader;
