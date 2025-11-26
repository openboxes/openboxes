import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import RedirectButton from 'utils/RedirectButton';

const ConfirmStepHeader = ({
  back,
  save,
  isSaveDisabled,
  setIsSaveDisabled,
  isFormDisabled,
  redirectTab,
  redirectLabel,
  redirectDefaultMessage,
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
    <div className="d-flex justify-content-sm-between align-items-end">
      <RedirectButton
        label={redirectLabel}
        defaultMessage={redirectDefaultMessage}
        redirectTo={CYCLE_COUNT.list(redirectTab)}
        className="pt-5"
      />
      <div className="d-flex gap-8">
        <Button
          onClick={handleBackClick}
          label="react.default.button.back.label"
          defaultLabel="Back"
          variant="primary-outline"
          disabled={isFormDisabled}
        />
        <Button
          onClick={handleSaveClick}
          label="react.default.button.save.label"
          defaultLabel="Save"
          variant="primary"
          disabled={isSaveDisabled || isFormDisabled}
        />
      </div>
    </div>
  );
};

export default ConfirmStepHeader;

ConfirmStepHeader.propTypes = {
  back: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
  isSaveDisabled: PropTypes.bool.isRequired,
  setIsSaveDisabled: PropTypes.func.isRequired,
  isFormDisabled: PropTypes.bool.isRequired,
  redirectTab: PropTypes.string.isRequired,
  redirectLabel: PropTypes.string.isRequired,
  redirectDefaultMessage: PropTypes.string.isRequired,
};
