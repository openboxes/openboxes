import React from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';
import { connect } from 'react-redux';

import { hideInfoBarModal } from 'actions';
import AutosaveFeatureModalContent from 'components/infoBar/modals/autosave/AutosaveFeatureModalContent';
import AutosaveFeatureModalFooter from 'components/infoBar/modals/autosave/AutosaveFeatureModalFooter';
import AutosaveFeatureModalHeader from 'components/infoBar/modals/autosave/AutosaveFeatureModalHeader';
import { InfoBar } from 'consts/infoBar';
import useTranslation from 'hooks/useTranslation';

import 'components/infoBar/modals/autosave/AutosaveFeatureModal.scss';

const AutosaveFeatureModal = ({ isOpen }) => {
  useTranslation('autosaveFeatureModal');

  return (
    <Modal
      isOpen={isOpen}
      portalClassName="autosave-feature-modal"
    >
      <div className="autosave-feature-modal-wrapper">
        <AutosaveFeatureModalHeader />
        <AutosaveFeatureModalContent />
        <AutosaveFeatureModalFooter />
      </div>
    </Modal>
  );
};

const mapStateToProps = state => ({
  isOpen: state.infoBar.bars?.[InfoBar.AUTOSAVE]?.isModalOpen,
  versionLabel: state.infoBar.bars?.[InfoBar.AUTOSAVE]?.versionLabel,
});

const mapDispatchToProps = {
  handleClose: hideInfoBarModal,
};

export default connect(mapStateToProps, mapDispatchToProps)(AutosaveFeatureModal);

AutosaveFeatureModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
};
