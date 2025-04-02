import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';

import Button from 'components/form-elements/Button';
import translate from 'utils/Translate';

import 'utils/utils.scss';

const ConfirmModal = ({
  labels: { title, content },
  onClose,
  buttons,
  className,
  hideCloseButton,
}) => (
  <div
    className={`d-flex flex-column custom-modal-content justify-content-between bg-white ${className}`}
  >
    <div className="d-flex justify-content-between">
      {(title?.label && title?.default)
          && (
            <p className="custom-modal-title">
              {translate({
                id: title?.label,
                defaultMessage: title?.default,
                data: title?.data,
              })}
            </p>
          )}
      {!hideCloseButton && (
      <RiCloseFill
        size="32px"
        className="cursor-pointer"
        role="button"
        onClick={onClose}
      />
      )}
    </div>
    <div>
      {(content?.label && content?.default) && (
      <p className="custom-modal-text">
        {translate({
          id: content?.label,
          defaultMessage: content?.default,
          data: content?.data,
        })}
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
      data: PropTypes.shape({}),
    }),
    content: PropTypes.shape({
      label: PropTypes.string,
      default: PropTypes?.string,
      data: PropTypes.shape({}),
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
  hideCloseButton: PropTypes.bool,
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
  hideCloseButton: false,
};
