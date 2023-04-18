import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import { connect } from 'react-redux';

import { hideInfoBarModal } from 'actions';
import InfoBarVersionBox from 'components/infoBar/InfoBarVersionBox';
import { InfoBar, InfoBarVersionBoxVariant } from 'consts/infoBar';
import Translate from 'utils/Translate';

const AutosaveFeatureModalHeader = ({ versionLabel, handleClose }) => (
  <div className="header">
    <div className="d-flex align-items-center gap-8">
      <InfoBarVersionBox
        versionLabel={versionLabel}
        withIcon={false}
        variant={InfoBarVersionBoxVariant.FILLED}
      />
      <span className="title">
        <Translate id="react.autosaveFeatureModal.title.label" defaultMessage="New autosave feature is here!" />
      </span>
    </div>
    <div>
      <RiCloseFill
        onClick={() => handleClose(InfoBar.AUTOSAVE)}
        cursor="pointer"
      />
    </div>
  </div>
);

const mapStateToProps = state => ({
  versionLabel: state.infoBar.bars?.[InfoBar.AUTOSAVE].versionLabel,
});

const mapDispatchToProps = {
  handleClose: hideInfoBarModal,
};


export default connect(mapStateToProps, mapDispatchToProps)(AutosaveFeatureModalHeader);


AutosaveFeatureModalHeader.propTypes = {
  versionLabel: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
  handleClose: PropTypes.func.isRequired,
};
