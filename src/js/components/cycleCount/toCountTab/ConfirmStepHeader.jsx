import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const ConfirmStepHeader = ({ back, save }) => (

  <div className="confirm-step-header">
    <Button
      onClick={back}
      label="react.default.button.back.label"
      defaultLabel="Back"
      variant="primary-outline"
    />
    <Button
      onClick={save}
      label="react.default.button.save.label"
      defaultLabel="Save"
      variant="primary"
    />
  </div>
);

export default ConfirmStepHeader;

ConfirmStepHeader.propTypes = {
  back: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
};
