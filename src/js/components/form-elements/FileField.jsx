import React from 'react';

import Dropzone from 'react-dropzone';

import BaseField from 'components/form-elements/BaseField';
import Translate from 'utils/Translate';

const FileField = (props) => {
  // eslint-disable-next-line react/prop-types
  const renderInput = ({ value, onChange }) => {
    const onDrop = (newFiles) => {
      if (newFiles && newFiles.length) {
        onChange(newFiles[0]);
      }
    };

    return (
      <div className="form-element-container">
        <Dropzone
          onDrop={onDrop}
          className="form-control form-control-xs file-field"
        >
          <div>
            <button type="button" className="btn btn-primary btn-xs">
              <Translate id="react.default.button.chooseFile.label" defaultMessage="Choose File" />
            </button>
            <span className="ml-3">
              {value ? value.name : <Translate id="react.default.button.noFileChosen.label" defaultMessage="No file chosen" />}
            </span>
          </div>
        </Dropzone>
      </div>
    );
  };

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default FileField;
