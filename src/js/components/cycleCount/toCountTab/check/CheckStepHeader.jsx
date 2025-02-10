import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const CheckStepHeader = ({ back, save }) => (

  <div className="d-flex justify-content-end align-items-end gap-8 pt-5">
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

export default CheckStepHeader;

CheckStepHeader.propTypes = {
  back: PropTypes.func.isRequired,
  save: PropTypes.func.isRequired,
};
