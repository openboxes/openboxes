import React from 'react';

import PropTypes from 'prop-types';
import {
  RiDeleteBinLine,
  RiPictureInPictureExitLine,
  RiSave2Line,
} from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import DropzoneFileSelect from 'components/form-elements/v2/DropzoneFileSelect';
import DropdownButton from 'utils/DropdownButton';
import Translate from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

const InboundSendFormHeader = ({
  saveAndExit,
  onSave,
  isValid,
  documents,
  handleExportFile,
  handleDownloadFiles,
  files,
  handleRemoveFile,
  isDispatched,
}) => {
  const documentActions = documents.map((document) => (
    {
      label: document?.name,
      defaultLabel: document?.name,
      onClick: (e) => {
        e.preventDefault();
        handleExportFile(document);
      },
    }
  ));

  return (
    <div className="d-flex justify-content-between mb-3">
      <div className="font-size-md font-weight-normal">
        <Translate
          id="react.attribute.options.label"
          defaultMessage="Sending options"
        />
      </div>

      <div className="buttons-container">
        <div>
          <DropzoneFileSelect
            onChange={handleDownloadFiles}
            multiple
            isFormDisabled={isDispatched}
            showButtonOnly
            throwErrorOnInvalidFiles
            buttonLabel={{
              id: 'react.stockMovement.uploadDocuments.label',
              defaultMessage: 'Upload documents',
            }}
            buttonVariant="primary-outline"
          />
          <div className="d-flex flex-column align-items-center justify-content-center">
            {files.map((file) => (
              <CustomTooltip key={file.name} content={file.name}>
                <div className="uploaded-file">
                  <span className="text-truncate">
                    {file.name}
                  </span>
                  <RiDeleteBinLine
                    className={isDispatched ? 'disabled-icon' : 'cursor-pointer text-danger'}
                    onClick={() => handleRemoveFile(file)}
                    size={16}
                  />
                </div>
              </CustomTooltip>
            ))}
          </div>
        </div>
        <DropdownButton
          actions={documentActions}
          disabled={!isValid}
          buttonDefaultLabel="Download"
          buttonLabel="react.default.button.download.label"
          variant="primary-outline"
        />
        <Button
          onClick={() => saveAndExit()}
          StartIcon={<RiPictureInPictureExitLine className="icon" />}
          defaultLabel="Save and exit"
          label="react.default.button.saveAndExit.label"
          variant="primary-outline"
        />
        <Button
          onClick={() => onSave({ showNotification: true })}
          StartIcon={<RiSave2Line className="icon" />}
          defaultLabel="Save"
          label="react.default.button.save.label"
          variant="primary-outline"
          disabled={!isValid}
        />
      </div>
    </div>
  );
};

export default InboundSendFormHeader;

InboundSendFormHeader.propTypes = {
  saveAndExit: PropTypes.func.isRequired,
  onSave: PropTypes.func.isRequired,
  isValid: PropTypes.bool.isRequired,
  documents: PropTypes.arrayOf(PropTypes.shape({})),
  handleExportFile: PropTypes.func.isRequired,
  handleDownloadFiles: PropTypes.func.isRequired,
  files: PropTypes.arrayOf(PropTypes.shape({})),
  handleRemoveFile: PropTypes.func.isRequired,
  isDispatched: PropTypes.bool.isRequired,
};

InboundSendFormHeader.defaultProps = {
  documents: [],
  files: [],
};
