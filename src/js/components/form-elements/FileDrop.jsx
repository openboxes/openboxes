import React from 'react';

import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';

import Translate from 'utils/Translate';

const formatFileName = (fileName) => {
  const [name, extension] = fileName.split('.');
  return <span>{name}{extension && <small className="text-muted">{`.${extension}`}</small>}</span>;
};

const FileDrop = (props) => {
  const {
    onDrop,
    className,
    acceptedFileLabel,
    fileDialogLabel,
    dragDropHereLabel,
    file,
  } = props;

  const onDropHandler = (newFiles) => {
    if (newFiles && newFiles.length) {
      onDrop(newFiles[0]);
    }
  };

  return (
    <div className={`file-drop d-flex flex-column ${className}`}>
      <Dropzone
        style={{
          border: '2px dashed #a0a0a0',
          borderRadius: '5px',
        }}
        className="bg-light d-flex flex-column justify-content-center align-items-center p-4"
        onDrop={onDropHandler}
      >
        <h5 className="font-italic text-secondary my-3">
          <Translate id={dragDropHereLabel} defaultMessage="Drag and drop file here." />
        </h5>
        <button
          type="button"
          className="my-3 btn btn-light border border-secondary font-weight-bold"
        >
          <Translate id={fileDialogLabel} defaultMessage="OPEN FILE DIALOG" />
        </button>
      </Dropzone>
      <p className="align-self-start">
        <span className="font-weight-bold text-success">
          <Translate id={acceptedFileLabel} defaultMessage="Accepted File" />:&nbsp;
        </span>
        {file && file.name && formatFileName(file.name)}
      </p>
    </div>
  );
};

export default FileDrop;

FileDrop.defaultProps = {
  className: '',
  acceptedFileLabel: 'react.default.acceptedFile.label',
  dragDropHereLabel: 'react.default.dragDropHere.label',
  fileDialogLabel: 'react.default.fileDialog.label',
  file: {},
};

FileDrop.propTypes = {
  onDrop: PropTypes.func.isRequired,
  file: PropTypes.shape({}),
  className: PropTypes.string,
  acceptedFileLabel: PropTypes.string,
  fileDialogLabel: PropTypes.string,
  dragDropHereLabel: PropTypes.string,
};

