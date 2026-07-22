import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const ConfirmReceiptButtons = ({ onBackToReceive, onCompleteReceipt }) => (
  <div className="d-flex gap-8">
    <Button
      label="react.receiving.backToReceive.label"
      defaultLabel="Back to Receive"
      variant="primary-outline"
      onClick={onBackToReceive}
    />
    <Button
      label="react.receiving.completeReceipt.label"
      defaultLabel="Complete Receipt"
      variant="primary"
      onClick={onCompleteReceipt}
    />
  </div>
);

ConfirmReceiptButtons.propTypes = {
  onBackToReceive: PropTypes.func,
  onCompleteReceipt: PropTypes.func,
};

ConfirmReceiptButtons.defaultProps = {
  onBackToReceive: () => {},
  onCompleteReceipt: () => {},
};

export default ConfirmReceiptButtons;
