import React, { memo } from 'react';

import PropTypes from 'prop-types';
import { RiArrowGoBackFill } from 'react-icons/ri';

import Button from 'components/form-elements/Button';

const InboundSendFormNavigation = memo(({
  onPrevious,
  onRollback,
  isDispatched,
  isRollbackEnabled,
  rollbackButtonVisible,
}) => (
  <div className="submit-buttons">
    <Button
      label="react.default.button.previous.label"
      defaultLabel="Previous"
      variant="primary"
      onClick={onPrevious}
      disabled={isDispatched}
    />
    <div className="buttons-container">
      {rollbackButtonVisible && (
      <Button
        label="react.default.button.rollback.label"
        defaultLabel="Rollback"
        variant="primary-outline"
        onClick={onRollback}
        StartIcon={<RiArrowGoBackFill className="icon" />}
        disabled={!isRollbackEnabled}
      />
      )}
      <Button
        label="react.shipping.sendShipment.label"
        defaultLabel="Send shipment"
        variant="primary"
        type="submit"
        disabled={isDispatched}
      />
    </div>
  </div>
));

InboundSendFormNavigation.displayName = 'InboundSendFormNavigation';

InboundSendFormNavigation.propTypes = {
  onPrevious: PropTypes.func.isRequired,
  onRollback: PropTypes.func.isRequired,
  isDispatched: PropTypes.bool.isRequired,
  isRollbackEnabled: PropTypes.bool.isRequired,
  rollbackButtonVisible: PropTypes.bool.isRequired,
};

export default InboundSendFormNavigation;
