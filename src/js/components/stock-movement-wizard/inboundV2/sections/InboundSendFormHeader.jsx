import React from 'react';

import PropTypes from 'prop-types';
import { IoMdExit } from 'react-icons/io';
import {
  RiDownload2Line,
  RiPictureInPictureExitLine,
  RiSave2Line,
  RiUpload2Line,
} from 'react-icons/ri';
import { useHistory } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import requisitionStatus from 'consts/requisitionStatus';
import Translate from 'utils/Translate';

const InboundSendFormHeader = ({
  saveAndExit,
  onSave,
  statusCode,
  hasErrors,
  matchesDestination,
}) => {
  const history = useHistory();

  // Upload Documents button is disabled if shipment is already dispatched
  // or selected destination doesn't match current location
  const uploadDocumentsButtonDisabled =
    statusCode === requisitionStatus.DISPATCHED || !matchesDestination;
  return (
    <div className="d-flex justify-content-between align-items-center mb-3">
      <div className="font-size-md font-weight-normal">
        <Translate
          id="react.attribute.options.label"
          defaultMessage="Sending options"
        />
      </div>

      <div className="buttons-container">
        <Button
          onClick={() => console.log('click')}
          StartIcon={<RiUpload2Line className="icon" />}
          defaultLabel="Upload documents"
          label="react.stockMovement.uploadDocuments.label"
          variant="primary-outline"
          disabled={uploadDocumentsButtonDisabled}
        />
        <div className="btn-group">
          <Button
            isDropdown
            defaultLabel="Export"
            label="react.default.button.export.label"
            variant="primary-outline"
            StartIcon={<RiDownload2Line />}
            disabled={!matchesDestination}
          />
          <div
            className="dropdown-menu dropdown-menu-right nav-item padding-8"
            aria-labelledby="dropdownMenuButton"
          >
            <a
              href="#"
              className="dropdown-item"
              onClick={() => console.log('click')}
              role="button"
            >
              <Translate
                id="react.shipping.exportPackingList.label"
                defaultMessage="Export Packing List (.xls)"
              />
            </a>
            <a
              href="#"
              className="dropdown-item"
              onClick={() => console.log('click')}
              role="button"
            >
              <Translate
                id="react.shipping.downloadPackingList.label"
                defaultMessage="Packing List"
              />
            </a>
            <a
              href="#"
              className="dropdown-item"
              onClick={() => console.log('click')}
              role="button"
            >
              <Translate
                id="react.stockMovement.downloadCertificateOfDonation.label"
                defaultMessage="Certificate of Donation"
              />
            </a>
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
          onClick={() => onSave()}
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
};

InboundSendFormHeader.defaultProps = {
  statusCode: '',
};
