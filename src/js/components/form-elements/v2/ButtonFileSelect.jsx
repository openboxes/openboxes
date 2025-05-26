import React from 'react';

import PropTypes from 'prop-types';

import notification from 'components/Layout/notifications/notification';
import FileFormat from 'consts/fileFormat';
import NotificationType from 'consts/notificationTypes';
import useTranslate from 'hooks/useTranslate';
import Translate from 'utils/Translate';

const ButtonFileSelect = ({
  allowedExtension,
  onFileUpload,
  label,
  defaultLabel,
  variant,
  disabled,
}) => {
  const translate = useTranslate();

  const getFileExtension = (file) => file.name.split('.')?.[1];

  const validateFileType = (file) => {
    if (allowedExtension === getFileExtension(file)) {
      return true;
    }

    notification(NotificationType.ERROR_FILLED)({
      message: 'Invalid file extension',
      details: translate(
        'react.default.error.invalidFileExtension.label',
        `File extension should be: ${allowedExtension}`,
        [allowedExtension],
      ),
    });

    return false;
  };

  return (
    <label
      htmlFor="csvInput"
      className={`btn ${variant}-button ${disabled ? `${variant}-disabled` : ''} align-self-end btn-xs`}
    >
      <span>
        <i className="fa fa-download pr-2" />
        <Translate
          id={label}
          defaultMessage={defaultLabel}
        />
      </span>
      <input
        data-testid="file-uploader"
        id="csvInput"
        type="file"
        style={{ display: 'none' }}
        disabled={disabled}
        onChange={(e) => {
          const file = e.target.files[0];
          if (validateFileType(file)) {
            onFileUpload(file);
          }
        }}
        accept={allowedExtension}
      />
    </label>
  );
};

export default ButtonFileSelect;

ButtonFileSelect.propTypes = {
  allowedExtension: PropTypes.oneOf(Object.values(FileFormat)).isRequired,
  onFileUpload: PropTypes.func.isRequired,
  label: PropTypes.string,
  defaultLabel: PropTypes.string,
  variant: PropTypes.oneOf([
    'primary',
    'secondary',
    'transparent',
    'primary-outline',
    'grayed',
  ]),
  disabled: PropTypes.bool,
};

ButtonFileSelect.defaultProps = {
  label: 'react.default.importButton.label',
  defaultLabel: 'Import File',
  variant: 'primary',
  disabled: false,
};
