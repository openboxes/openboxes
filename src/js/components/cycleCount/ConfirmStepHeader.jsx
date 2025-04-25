import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const ConfirmStepHeader = ({
  back,
  save,
  isSaveDisabled,
  setIsSaveDisabled,
}) => {
  const handleSaveClick = () => {
    setIsSaveDisabled(true);
    save();
  };

  const handleBackClick = () => {
    setIsSaveDisabled(false);
    back();
  };

  return (
    <div className="confirm-step-header">
      <Button
        onClick={handleBackClick}
        label="react.default.button.back.label"
        defaultLabel="Back"
        variant="primary-outline"
      />
      <Button
        onClick={handleSaveClick}
        label="react.default.button.save.label"
        defaultLabel="Save"
        variant="primary"
        disabled={isSaveDisabled}
      />
    </div>
  );
};

export default ConfirmStepHeader;

ConfirmStepHeader.propTypes = {
  back: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
  isSaveDisabled: PropTypes.bool.isRequired,
  setIsSaveDisabled: PropTypes.func.isRequired,
};
