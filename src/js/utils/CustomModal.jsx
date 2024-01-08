import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import Modal from 'react-modal';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

import 'utils/utils.scss';

const CustomModal = ({
  titleLabel,
  defaultTitle,
  contentLabel,
  defaultContent,
  isOpen,
  onClose,
  buttons,
  className,
}) => (
  <Modal
    isOpen={isOpen}
    className="modal-content custom-modal-content"
  >
    <div className={`d-flex flex-column ${className}`}>
      <div className="d-flex justify-content-between">
        {(titleLabel && defaultTitle) && (
        <p className="custom-modal-title">
          <Translate id={titleLabel} defaultMessage={defaultTitle} />
        </p>
        )}
        <RiCloseFill
          size="32px"
          className="cursor-pointer"
          role="button"
          onClick={onClose}
        />
      </div>
      <div>
        {(contentLabel && defaultContent) && (
        <p className="custom-modal-text">
          <Translate id={contentLabel} defaultMessage={defaultContent} />
        </p>
        )}
      </div>
      <div className="d-flex justify-content-end">
        {buttons && buttons.map((button) => (
          <Button
            variant={button?.variant}
            defaultLabel={button?.defaultLabel}
            label={button?.label}
            onClick={button?.onClick}
          />
        ))}
      </div>
    </div>
  </Modal>
);

export default CustomModal;

CustomModal.propTypes = {
  titleLabel: PropTypes.string,
  defaultTitle: PropTypes.string,
  contentLabel: PropTypes.string,
  defaultContent: PropTypes.string,
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.string.isRequired,
  className: PropTypes.string,
  buttons: PropTypes.arrayOf(PropTypes.shape({
    variant: PropTypes.string,
    defaultLabel: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
  })),
};

CustomModal.defaultProps = {
  titleLabel: '',
  defaultTitle: '',
  contentLabel: '',
  defaultContent: '',
  buttons: [],
  className: '',
};
