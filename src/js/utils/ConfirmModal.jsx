import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

import 'utils/utils.scss';

const ConfirmModal = ({
  labels: { title, content },
  onClose,
  buttons,
  className,
}) => (
  <div className={`d-flex flex-column custom-modal-content justify-content-between bg-white ${className}`}>
    <div className="d-flex justify-content-between">
      {(title?.label && title?.default)
        && (
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
);

export default ConfirmModal;

ConfirmModal.propTypes = {
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
  onClose: PropTypes.func.isRequired,
  className: PropTypes.string,
  buttons: PropTypes.arrayOf(PropTypes.shape({
    variant: PropTypes.string,
    defaultLabel: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
  })),
};

ConfirmModal.defaultProps = {
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
