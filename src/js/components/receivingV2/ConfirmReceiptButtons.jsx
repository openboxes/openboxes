import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const ConfirmReceiptButtons = ({ onBackToReceive, onCompleteReceipt, isCompleteDisabled }) => (
  <div className="d-flex gap-8">
    <Button
      label="react.receiving.backToReceive.label"
      defaultLabel="Back to Receive"
      tooltipLabel="react.receiving.backToReceive.tooltip.label"
      defaultTooltipLabel="Edit previously-entered receiving information"
      variant="primary-outline"
      onClick={onBackToReceive}
    />
    <Button
      label="react.receiving.completeReceipt.label"
      tooltipLabel="react.receiving.completeReceipt.tooltip.label"
      defaultTooltipLabel="Submit the receipt"
      defaultLabel="Complete Receipt"
      variant="primary"
      onClick={onCompleteReceipt}
      disabled={isCompleteDisabled}
    />
  </div>
);

ConfirmReceiptButtons.propTypes = {
  onBackToReceive: PropTypes.func,
  onCompleteReceipt: PropTypes.func,
  isCompleteDisabled: PropTypes.bool,
};

ConfirmReceiptButtons.defaultProps = {
  onBackToReceive: () => {},
  onCompleteReceipt: () => {},
  isCompleteDisabled: false,
};

export default ConfirmReceiptButtons;
