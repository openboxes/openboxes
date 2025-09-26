import React from 'react';

import PropTypes from 'prop-types';
import {
  RiDownload2Line,
  RiPictureInPictureExitLine,
  RiSave2Line,
  RiUpload2Line,
} from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import requisitionStatus from 'consts/requisitionStatus';
import { FormErrorPropType } from 'utils/propTypes';
import Translate from 'utils/Translate';

const InboundSendFormHeader = ({
  errors,
  saveAndExit,
  onSave,
  statusCode,
}) => (
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
        disabled={statusCode === requisitionStatus.DISPATCHED}
      />
      <div className="btn-group">
        <Button
          isDropdown
          defaultLabel="Export"
          label="react.default.button.export.label"
          variant="primary-outline"
          StartIcon={<RiDownload2Line />}
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
              id="shipping.exportPackingList.label"
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
              id="shipping.downloadPackingList.label"
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
        onClick={saveAndExit}
        StartIcon={<RiPictureInPictureExitLine className="icon" />}
        defaultLabel="Save and exit"
        label="react.default.button.saveAndExit.label"
        variant="primary-outline"
      />
      <Button
        onClick={onSave}
        StartIcon={<RiSave2Line className="icon" />}
        defaultLabel="Save"
        label="react.default.button.save.label"
        variant="primary-outline"
        disabled={!!Object.keys(errors).length}
      />
    </div>
  </div>
);

export default InboundSendFormHeader;

InboundSendFormHeader.propTypes = {
  errors: PropTypes.shape({
    origin: FormErrorPropType,
    destination: FormErrorPropType,
    shipDate: FormErrorPropType,
    shipmentType: FormErrorPropType,
    trackingNumber: FormErrorPropType,
    driverName: FormErrorPropType,
    comments: FormErrorPropType,
    expectedDeliveryDate: FormErrorPropType,
  }),
  saveAndExit: PropTypes.func.isRequired,
  onSave: PropTypes.func.isRequired,
  statusCode: PropTypes.string,
};

InboundSendFormHeader.defaultProps = {
  errors: {},
  statusCode: '',
};
