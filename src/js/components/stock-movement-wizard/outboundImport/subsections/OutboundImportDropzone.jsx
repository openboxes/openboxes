import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import { PACKING_LIST_TEMPLATE } from 'api/urls';
import Button from 'components/form-elements/Button';
import FileSelect from 'components/form-elements/v2/FileSelect';
import Subsection from 'components/Layout/v2/Subsection';
import FileFormat from 'consts/fileFormat';
import exportFileFromAPI from 'utils/file-download-util';
import { FormErrorPropType } from 'utils/propTypes';
import { RiDownload2Line } from 'react-icons/ri';

const OutboundImportDropzone = ({ control, errors }) => {
  const dispatch = useDispatch();

  const downloadPackingListTemplate = async () => {
    try {
      dispatch(showSpinner());
      await exportFileFromAPI({
        url: PACKING_LIST_TEMPLATE,
        format: FileFormat.XLS,
        filename: 'Import Packing List',
      });
    } finally {
      dispatch(hideSpinner());
    }
  };

  return (
    <Subsection collapsable={false}>
      <div className="col-12 px-2 pt-2">
        <Button
          className="mb-4"
          variant="secondary"
          defaultLabel="Export template"
          label="react.default.button.exportTemplate.label"
          onClick={downloadPackingListTemplate}
          EndIcon={<RiDownload2Line />}
        />
        <Controller
          name="packingList"
          control={control}
          render={({ field }) => (
            <FileSelect
              allowedExtensions={[FileFormat.XLS]}
              errorMessage={errors.packingList?.message}
              buttonVariant="primary"
              dropzoneText={{
                id: 'react.outboundImport.form.importPackingList.title',
                defaultMessage: 'Import packing list',
              }}
              {...field}
            />
          )}
        />
      </div>
    </Subsection>
  );
};

export default OutboundImportDropzone;

OutboundImportDropzone.propTypes = {
  errors: PropTypes.shape({
    dateShipped: FormErrorPropType,
    shipmentType: FormErrorPropType,
    trackingNumber: FormErrorPropType,
    expectedDeliveryDate: FormErrorPropType,
    packingList: FormErrorPropType,
  }).isRequired,
  control: PropTypes.shape({}).isRequired,
};
