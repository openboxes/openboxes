import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import Section from 'components/Layout/v2/Section';
import OutboundImportItems
  from 'components/stock-movement-wizard/outboundImport/subsections/OutboundImportItems';
import OutboundImportShipmentDetails
  from 'components/stock-movement-wizard/outboundImport/subsections/OutboundImportShipmentDetails';
import { FormErrorPropType } from 'utils/propTypes';

const OutboundImportConfirm = ({
  control,
  errors,
  previous,
  data,
  tableErrors,
  hasErrors,
}) => (
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
        variant={hasErrors ? 'primary' : 'secondary'}
        onClick={previous}
      />
      <Button
        label="react.outboundImport.form.finish.label"
        defaultLabel="Finish"
        type="submit"
        disabled={hasErrors}
        variant="primary"
      />
    </div>
    <OutboundImportItems data={data} errors={tableErrors} />
  </Section>
);

export default OutboundImportConfirm;

OutboundImportConfirm.defaultProps = {
  tableErrors: {},
};

OutboundImportConfirm.propTypes = {
  data: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  previous: PropTypes.func.isRequired,
  tableErrors: PropTypes.shape({}),
  errors: PropTypes.shape({
    origin: FormErrorPropType,
    destination: FormErrorPropType,
    dateShipped: FormErrorPropType,
    shipmentType: FormErrorPropType,
    trackingNumber: FormErrorPropType,
    expectedDeliveryDate: FormErrorPropType,
  }).isRequired,
  control: PropTypes.shape({}).isRequired,
  hasErrors: PropTypes.bool.isRequired,
};
