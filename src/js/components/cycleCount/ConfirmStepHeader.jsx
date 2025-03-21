import React, { useState } from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const ConfirmStepHeader = ({ back, save }) => {
  const [disabledSave, setDisabledSave] = useState(false);

  const handleSaveClick = () => {
    setDisabledSave(true);
    save();
  };

  return (
    <div className="confirm-step-header">
      <Button
        onClick={back}
        label="react.default.button.back.label"
        defaultLabel="Back"
        variant="primary-outline"
      />
      <Button
        onClick={handleSaveClick}
        label="react.default.button.save.label"
        defaultLabel="Save"
        variant="primary"
        disabled={disabledSave}
      />
    </div>
  );
};

export default ConfirmStepHeader;

ConfirmStepHeader.propTypes = {
  back: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
};
