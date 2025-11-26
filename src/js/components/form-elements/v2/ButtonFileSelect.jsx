import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useTranslate from 'hooks/useTranslate';
import Translate from 'utils/Translate';

const ButtonFileSelect = ({
  allowedExtensions,
  onFileUpload,
  label,
  defaultLabel,
  variant,
  disabled,
  className,
}) => {
  const translate = useTranslate();

  const getFileExtension = (file) => file.name.split('.')?.[1];

  const validateFileType = (files) => {
    if (!allowedExtensions.length
        || _.every(files, (file) => allowedExtensions.includes(getFileExtension(file)))) {
      return true;
    }

    notification(NotificationType.ERROR_FILLED)({
      message: 'Invalid file extension',
      details: translate(
        'react.default.error.invalidFilesExtension.label',
        `File extension should be one of: ${allowedExtensions.join(', ')}`,
        [allowedExtensions.join(', ')],
      ),
    });

    return false;
  };

  const getAcceptableExtensions = () =>
    allowedExtensions.map((extension) => `.${extension}`).join(',');

  return (
    <label
      htmlFor="csvInput"
      className={`${className} btn ${variant}-button ${disabled ? `${variant}-disabled` : ''} align-self-end btn-xs`}
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
          const { files } = e.target;
          if (validateFileType(files)) {
            onFileUpload(files);
          }
          // After upload, we need to clear the value,
          // due to the bug on Chrome that the same file can't be uploaded twice
          e.target.value = null;
        }}
        accept={getAcceptableExtensions()}
      />
    </label>
  );
};

export default ButtonFileSelect;

ButtonFileSelect.propTypes = {
  allowedExtensions: PropTypes.arrayOf(
    PropTypes.string,
  ).isRequired,
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
  className: PropTypes.string,
};

ButtonFileSelect.defaultProps = {
  label: 'react.default.importButton.label',
  defaultLabel: 'Import File',
  variant: 'primary',
  disabled: false,
  className: '',
};
