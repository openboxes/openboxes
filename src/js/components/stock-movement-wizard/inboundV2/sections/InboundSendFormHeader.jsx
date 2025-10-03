import React from 'react';

import PropTypes from 'prop-types';
import { IoMdExit } from 'react-icons/io';
import {
  RiDeleteBinLine,
  RiDownload2Line,
  RiPictureInPictureExitLine,
  RiSave2Line,
} from 'react-icons/ri';
import { useHistory } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import DropzoneFileSelect from 'components/form-elements/v2/DropzoneFileSelect';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import requisitionStatus from 'consts/requisitionStatus';
import Translate from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

const InboundSendFormHeader = ({
  saveAndExit,
  onSave,
  statusCode,
  hasErrors,
  matchesDestination,
  documents,
  handleExportFile,
  handleDownloadFiles,
  files,
  handleRemoveFile,
}) => {
  const history = useHistory();

  // Upload Documents button is disabled if shipment is already dispatched
  // or selected destination doesn't match current location
  const uploadDocumentsButtonDisabled =
    statusCode === requisitionStatus.DISPATCHED || !matchesDestination;
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
            isFormDisabled={uploadDocumentsButtonDisabled}
            showButtonOnly
            buttonLabel={{
              id: 'react.stockMovement.uploadDocuments.label',
              defaultMessage: 'Upload documents',
            }}
            buttonVariant="primary-outline"
          />
          <div className="d-flex flex-column">
            {files.map((file) => (
              <CustomTooltip key={file.name} content={file.name}>
                <div className="uploaded-file">
                  <span className="text-truncate">
                    {file.name}
                  </span>
                  <RiDeleteBinLine
                    className={uploadDocumentsButtonDisabled ? 'disabled-icon' : 'cursor-pointer text-danger'}
                    onClick={() => handleRemoveFile(file)}
                    size={16}
                  />
                </div>
              </CustomTooltip>
            ))}
          </div>
        </div>
        <div className="btn-group">
          <Button
            isDropdown
            defaultLabel="Download"
            label="react.default.button.download.label"
            variant="primary-outline"
            StartIcon={<RiDownload2Line />}
            disabled={hasErrors || !matchesDestination}
          />
          <div
            className="dropdown-menu dropdown-menu-right nav-item padding-8"
            aria-labelledby="dropdownMenuButton"
          >
            {documents?.map((document) => (
              <a
                key={document?.uri}
                href="#"
                className="dropdown-item"
                onClick={(e) => {
                  e.preventDefault();
                  handleExportFile(document);
                }}
                role="button"
              >
                {document?.name}
              </a>
            ))}
          </div>
        </div>
        <Button
          onClick={() => saveAndExit()}
          StartIcon={<RiPictureInPictureExitLine className="icon" />}
          defaultLabel="Save and exit"
          label="react.default.button.saveAndExit.label"
          variant="primary-outline"
          disabled={!matchesDestination}
        />
        <Button
          onClick={() => onSave({ showNotification: true })}
          StartIcon={<RiSave2Line className="icon" />}
          defaultLabel="Save"
          label="react.default.button.save.label"
          variant="primary-outline"
          disabled={hasErrors || !matchesDestination}
        />
        {!matchesDestination && (
          <Button
            onClick={() => history.push(STOCK_MOVEMENT_URL.listInbound())}
            StartIcon={<IoMdExit className="icon" />}
            defaultLabel="Exit"
            label="react.default.button.exit.label"
            variant="danger-outline"
          />
        )}
      </div>
    </div>
  );
};

export default InboundSendFormHeader;

InboundSendFormHeader.propTypes = {
  saveAndExit: PropTypes.func.isRequired,
  onSave: PropTypes.func.isRequired,
  statusCode: PropTypes.string,
  hasErrors: PropTypes.bool.isRequired,
  matchesDestination: PropTypes.bool.isRequired,
  documents: PropTypes.arrayOf(PropTypes.shape({})),
  handleExportFile: PropTypes.func.isRequired,
  handleDownloadFiles: PropTypes.func.isRequired,
  files: PropTypes.arrayOf(PropTypes.shape({})),
  handleRemoveFile: PropTypes.func.isRequired,
};

InboundSendFormHeader.defaultProps = {
  statusCode: '',
  documents: [],
  files: [],
};
