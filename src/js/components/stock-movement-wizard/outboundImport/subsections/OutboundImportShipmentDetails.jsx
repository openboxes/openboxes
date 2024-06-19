import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';

import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import { FormErrorPropType } from 'utils/propTypes';

const OutboundImportShipmentDetails = ({ control, errors }) => (
  <Subsection
    title={{
      label: 'react.outboundImport.form.shipmentDetails.label',
      defaultMessage: 'Shipment details',
    }}
    collapsable={false}
  >
    <div className="row">
      <div className="col-lg-3 col-md-6 px-2 pt-2">
        <Controller
          name="origin"
          control={control}
          render={({ field }) => (
            <SelectField
              {...field}
              disabled
              placeholder="Select"
              title={{
                id: 'react.stockMovement.origin.label',
                defaultMessage: 'Origin',
              }}
              hasErrors={Boolean(errors.origin?.message)}
              errorMessage={errors.origin?.message}
            />
          )}
        />
      </div>
      <div className="col-lg-3 col-md-6 px-2 pt-2">
        <Controller
          name="destination"
          control={control}
          render={({ field }) => (
            <SelectField
              {...field}
              disabled
              placeholder="Select"
              title={{
                id: 'react.stockMovement.destination.label',
                defaultMessage: 'Destination',
              }}
              hasErrors={Boolean(errors.destination?.message)}
              errorMessage={errors.destination?.message}
            />
          )}
        />
      </div>
      <div className="col-lg-3 px-2 pt-2 d-lg-block d-md-none" />
      <div className="col-lg-3 px-2 pt-2 d-lg-block d-md-none" />
      <div className="col-lg-3 col-md-6 px-2 pt-2">
        <Controller
          name="shipmentType"
          control={control}
          render={({ field }) => (
            <SelectField
              {...field}
              disabled
              placeholder="Select"
              title={{
                id: 'react.stockMovement.shipmentType.label',
                defaultMessage: 'Shipment type',
              }}
              hasErrors={Boolean(errors.shipmentType?.message)}
              errorMessage={errors.shipmentType?.message}
            />
          )}
        />
      </div>
      <div className="col-lg-3 col-md-6 px-2 pt-2">
        <Controller
          name="trackingNumber"
          control={control}
          render={({ field }) => (
            <TextInput
              {...field}
              disabled
              title={{
                id: 'react.stockMovement.trackingNumber.label',
                defaultMessage: 'Tracking Number',
              }}
              errorMessage={errors.trackingNumber?.message}
            />
          )}
        />
      </div>
      <div className="col-lg-3 col-md-6 px-2 pt-2">
        <Controller
          name="dateShipped"
          control={control}
          render={({ field }) => (
            <DateField
              {...field}
              disabled
              title={{
                id: 'react.stockMovement.shipDate.label',
                defaultMessage: 'Ship date',
              }}
              placeholder={{
                id: 'react.default.dateInput.placeholder.label',
                default: 'Select a date',
              }}
              showTimeSelect
              errorMessage={errors.dateShipped?.message}
            />
          )}
        />
      </div>
      <div className="col-lg-3 col-md-6 px-2 pt-2">
        <Controller
          name="expectedDeliveryDate"
          control={control}
          render={({ field }) => (
            <DateField
              {...field}
              disabled
              title={{
                id: 'react.stockMovement.expectedDeliveryDate.label',
                defaultMessage: 'Expected Delivery Date',
              }}
              errorMessage={errors.expectedDeliveryDate?.message}
            />
          )}
        />
      </div>
    </div>
  </Subsection>
);

export default OutboundImportShipmentDetails;

OutboundImportShipmentDetails.propTypes = {
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
