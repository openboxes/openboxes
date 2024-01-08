import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import Modal from 'react-modal';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

import 'utils/utils.scss';

const CustomModal = ({
  labels: { title, content },
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
        {(title?.label && title?.default) && (
        <p className="custom-modal-title">
          <Translate id={title?.label} defaultMessage={title?.default} />
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
        {(content?.label && content?.default) && (
        <p className="custom-modal-text">
          <Translate id={content?.label} defaultMessage={content?.default} />
        </p>
        )}
      </div>
      <div className="d-flex justify-content-end">
        {buttons?.map?.((button) => (
          <Button
            key={button?.label}
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
  labels: PropTypes.shape({
    title: PropTypes.shape({
      label: PropTypes.string,
      default: PropTypes?.string,
    }),
    content: PropTypes.shape({
      label: PropTypes.string,
      default: PropTypes?.string,
    }),
  }),
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
  labels: {
    title: {
      label: '',
      content: '',
    },
    content: {
      label: '',
      content: '',
    },
  },
  buttons: [],
  className: '',
};
