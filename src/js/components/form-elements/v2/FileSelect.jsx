import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { useDropzone } from 'react-dropzone';

import Button from 'components/form-elements/Button';
import useTranslate from 'hooks/useTranslate';

import './style.scss';

const FileSelect = ({
  height,
  width,
  dropzoneText,
  buttonLabel,
  buttonVariant,
  className,
  multiple,
  maxFiles,
  allowedExtensions,
  ...fieldProps
}) => {
  const onDrop = useCallback((acceptedFiles) => {
    fieldProps.onChange?.(multiple ? acceptedFiles : acceptedFiles[0]);
  }, []);

  const translate = useTranslate();

  const getFileExtension = (file) => file.path.split('.')?.[1];

  const validateFileType = (file) => {
    if (!allowedExtensions.length || allowedExtensions.includes(getFileExtension(file))) {
      return null;
    }

    return {
      code: 'invalid-extension',
      message: translate(
        'react.default.error.invalidFileExtension.label',
        `File extension should be one of: ${allowedExtensions.join(', ')}`,
        [allowedExtensions.join(', ')],
      ),
    };
  };

  const {
    getRootProps, getInputProps, open, acceptedFiles, fileRejections,
  } = useDropzone({
    onDrop,
    noClick: true,
    noKeyboard: true,
    validator: validateFileType,
    multiple,
    maxFiles,
  });

  const mapFiles = (files) => files.map((file) => {
    const data = file?.path ? file : file.file;
    return (
      <li key={data.path}>
        {data.path}
        {' '}
        (
        {data.size}
        {' '}
        bytes)
        {file?.errors?.length ? (
          <ul>
            {file.errors.map((e) => (
              <li key={e.code}>{e.message}</li>
            ))}
          </ul>
        ) : null}
      </li>
    );
  });

  return (
    <div style={{ width, height }}>
      <div {...getRootProps({ className: `dropzone d-flex flex-column justify-content-center align-items-center p-3 bg-light ${className}` })} {...fieldProps}>
        <input {...getInputProps()} />
        <h5 className="text-secondary font-italic">{translate(dropzoneText.id, dropzoneText.defaultMessage)}</h5>
        <Button className="mt-3" onClick={open} variant={buttonVariant} defaultLabel={buttonLabel.defaultMessage} label={buttonLabel.id} />
      </div>
      {acceptedFiles.length ? (
        <aside>
          <h6 className="text-success">
            {translate('react.default.acceptedFiles.label', 'Accepted Files')}
            :
          </h6>
          <ul>
            {mapFiles(acceptedFiles)}
          </ul>
        </aside>
      ) : null}
      {fileRejections.length ? (
        <aside>
          <h6 className="text-danger">
            {translate('react.default.rejectedFiles.label', 'Rejected Files')}
            :
          </h6>
          <ul>
            {mapFiles(fileRejections)}
          </ul>
        </aside>
      ) : null}
    </div>
  );
};

export default FileSelect;

FileSelect.propTypes = {
  // Text displayed on the dropzone
  dropzoneText: PropTypes.shape({
    id: PropTypes.string,
    defaultMessage: PropTypes.string,
  }),
  // Label displayed on the button for uploading files
  buttonLabel: PropTypes.shape({
    id: PropTypes.string,
    defaultMessage: PropTypes.string,
  }),
  buttonVariant: PropTypes.string,
  // width of the dropzone
  width: PropTypes.string,
  // height of the dropzone
  height: PropTypes.string,
  // classname applied to the dropzone
  className: PropTypes.string,
  // indicator whether we can upload multiple files
  multiple: PropTypes.bool,
  // maximal count of files to upload (disabled when set to null)
  maxFiles: PropTypes.number,
  // allowed extensions for importing (disabled when set to empty array)
  allowedExtensions: PropTypes.arrayOf(PropTypes.string),
};

FileSelect.defaultProps = {
  dropzoneText: {
    id: 'react.default.dragDropHere.label',
    defaultMessage: 'Drag and drop file here.',
  },
  buttonLabel: {
    id: 'react.default.fileDialog.label',
    defaultMessage: 'OPEN FILE DIALOG',
  },
  buttonVariant: 'grayed',
  width: 'auto',
  height: 'auto',
  className: '',
  multiple: false,
  maxFiles: null,
  allowedExtensions: [],
};
