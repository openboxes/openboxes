import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import Section from 'components/Layout/v2/Section';
import OutboundImportShipmentDetails
  from 'components/stock-movement-wizard/outboundImport/subsections/OutboundImportShipmentDetails';
import { FormErrorPropType } from 'utils/propTypes';

const OutboundImportConfirm = ({ control, errors, previous }) => {
  return (
    <Section
      title={{
        label: 'react.outboundImport.form.confirmation.label',
        defaultMessage: 'Confirmation',
      }}
    >
      <OutboundImportShipmentDetails control={control} errors={errors} />
      <div className="d-flex flex-row justify-content-between">
        <Button
          label="react.outboundImport.form.redoImport.label"
          defaultLabel="Redo import"
          variant="secondary"
          onClick={previous}
        />
        <Button
          label="react.outboundImport.form.finish.label"
          defaultLabel="Finish"
          variant="primary"
        />
      </div>
    </Section>
  );
};

export default OutboundImportConfirm;

OutboundImportConfirm.propTypes = {
  previous: PropTypes.func.isRequired,
  errors: PropTypes.shape({
    origin: FormErrorPropType,
    destination: FormErrorPropType,
    dateShipped: FormErrorPropType,
    shipmentType: FormErrorPropType,
    trackingNumber: FormErrorPropType,
    expectedDeliveryDate: FormErrorPropType,
  }).isRequired,
  control: PropTypes.shape({}).isRequired,
};
