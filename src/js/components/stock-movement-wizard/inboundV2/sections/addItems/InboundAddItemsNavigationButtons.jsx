import React, { memo } from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const InboundAddItemsNavigationButtons = memo(({
  onPrevious,
  onNext,
  isPreviousDisabled,
  isNextDisabled,
}) => (
  <div className="submit-buttons">
    <Button
      label="react.default.button.previous.label"
      defaultLabel="Previous"
      variant="primary"
      onClick={onPrevious}
      disabled={isPreviousDisabled}
    />
    <Button
      label="react.default.button.next.label"
      defaultLabel="Next"
      variant="primary"
      disabled={isNextDisabled}
      onClick={onNext}
    />
  </div>
));

InboundAddItemsNavigationButtons.displayName = 'InboundAddItemsNavigationButtons';

InboundAddItemsNavigationButtons.propTypes = {
  onPrevious: PropTypes.func.isRequired,
  onNext: PropTypes.func.isRequired,
  isPreviousDisabled: PropTypes.bool,
  isNextDisabled: PropTypes.bool,
};

InboundAddItemsNavigationButtons.defaultProps = {
  isPreviousDisabled: false,
  isNextDisabled: false,
};

export default InboundAddItemsNavigationButtons;
