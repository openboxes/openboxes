import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { hideInfoBarModal } from 'actions';
import Button from 'components/form-elements/Button';
import { InfoBar } from 'consts/infoBar';

const AutosaveFeatureModalFooter = ({ handleClose }) => (
  <div className="d-flex justify-content-end">
    <Button
      defaultLabel="Got it"
      label="react.autosaveFeatureModal.close.button.label"
      onClick={() => handleClose(InfoBar.AUTOSAVE)}
    />
  </div>
);


const mapDispatchToProps = {
  handleClose: hideInfoBarModal,
};

export default connect(null, mapDispatchToProps)(AutosaveFeatureModalFooter);

AutosaveFeatureModalFooter.propTypes = {
  handleClose: PropTypes.func.isRequired,
};
