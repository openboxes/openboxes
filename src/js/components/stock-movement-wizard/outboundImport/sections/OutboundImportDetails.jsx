import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import Section from 'components/Layout/v2/Section';
import OutboundImportBasicDetails
  from 'components/stock-movement-wizard/outboundImport/subsections/OutboundImportBasicDetails';
import OutboundImportDropzone
  from 'components/stock-movement-wizard/outboundImport/subsections/OutboundImportDropzone';
import OutboundImportSendingOptions
  from 'components/stock-movement-wizard/outboundImport/subsections/OutboundImportSendingOptions';
import useScrollToBottom from 'hooks/outboundImport/useScrollToBottom';
import { FormErrorPropType } from 'utils/propTypes';

const OutboundImportDetails = ({
  control,
  errors,
  isValid,
  trigger,
}) => {
  const { nextButtonRef } = useScrollToBottom();
  return (
    <Section
      title={{
        label: 'react.outboundImport.form.details.label',
        defaultMessage: 'Details',
      }}
    >
      <OutboundImportBasicDetails control={control} errors={errors} />
      <OutboundImportSendingOptions control={control} errors={errors} trigger={trigger} />
      <OutboundImportDropzone control={control} errors={errors} />
      <Button
        label="react.outboundImport.form.next.label"
        defaultLabel="Next"
        variant="primary"
        type="submit"
        className="fit-content align-self-end"
        disabled={!isValid}
        customRef={nextButtonRef}
      />
    </Section>
  );
};

export default OutboundImportDetails;

OutboundImportDetails.propTypes = {
  errors: PropTypes.shape({
    description: FormErrorPropType,
    origin: FormErrorPropType,
    destination: FormErrorPropType,
    requestedBy: FormErrorPropType,
    dateRequested: FormErrorPropType,
    dateShipped: FormErrorPropType,
    shipmentType: FormErrorPropType,
    trackingNumber: FormErrorPropType,
    expectedDeliveryDate: FormErrorPropType,
    packingList: FormErrorPropType,
  }).isRequired,
  control: PropTypes.shape({}).isRequired,
  isValid: PropTypes.bool.isRequired,
  trigger: PropTypes.func.isRequired,
};
