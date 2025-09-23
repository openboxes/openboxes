import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { useSelector } from 'react-redux';

import { fetchShipmentTypes } from 'actions';
import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import useOptionsFetch from 'hooks/options-data/useOptionsFetch';
import { FormErrorPropType } from 'utils/propTypes';

const OutboundImportSendingOptions = ({ control, errors, trigger }) => {
  useOptionsFetch([fetchShipmentTypes]);
  const {
    shipmentTypes,
  } = useSelector((state) => ({
    shipmentTypes: state.stockMovementCommon.shipmentTypes,
  }));
  return (
    <Subsection title={{ label: 'react.outboundImport.form.sendingOptions.label', defaultMessage: 'Sending options' }} collapsable={false}>
      <div className="row">
        <div className="col-lg-3 col-md-6 px-2 pt-2">
          <Controller
            name="shipmentType"
            control={control}
            render={({ field }) => (
              <SelectField
                title={{ id: 'react.outboundImport.form.shipmentType.title', defaultMessage: 'Shipment Type' }}
                required
                hasErrors={Boolean(errors.shipmentType?.message)}
                errorMessage={errors.shipmentType?.message}
                labelKey="displayName"
                options={shipmentTypes}
                {...field}
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
                title={{ id: 'react.outboundImport.form.trackingNumber.title', defaultMessage: 'Tracking number' }}
                errorMessage={errors.trackingNumber?.message}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-3 col-md-6 px-2 pt-2">
          <Controller
            name="dateShipped"
            control={control}
            render={({ field }) => (
              <DateFieldDateFns
                title={{
                  id: 'react.outboundImport.form.dateShipped.title',
                  defaultMessage: 'Ship date',
                }}
                placeholder={{
                  id: 'react.default.dateInput.placeholder.label',
                  default: 'Select a date',
                }}
                errorMessage={errors.dateShipped?.message}
                required
                showTimeSelect
                {...field}
                onBlur={() => {
                  field.onBlur();
                  trigger('expectedDeliveryDate');
                }}
              />
            )}
          />
        </div>
        <div className="col-lg-3 col-md-6 px-2 pt-2">
          <Controller
            name="expectedDeliveryDate"
            control={control}
            render={({ field }) => (
              <DateFieldDateFns
                title={{
                  id: 'react.outboundImport.form.expectedDeliveryDate.title',
                  defaultMessage: 'Expected delivery date',
                }}
                placeholder={{
                  id: 'react.default.dateInput.placeholder.label',
                  default: 'Select a date',
                }}
                errorMessage={errors.expectedDeliveryDate?.message}
                required
                {...field}
                onBlur={() => {
                  field.onBlur();
                  trigger('dateShipped');
                }}
              />
            )}
          />
        </div>
      </div>
    </Subsection>
  );
};

export default OutboundImportSendingOptions;

OutboundImportSendingOptions.propTypes = {
  errors: PropTypes.shape({
    dateShipped: FormErrorPropType,
    shipmentType: FormErrorPropType,
    trackingNumber: FormErrorPropType,
    expectedDeliveryDate: FormErrorPropType,
    packingList: FormErrorPropType,
  }).isRequired,
  control: PropTypes.shape({}).isRequired,
  trigger: PropTypes.func.isRequired,
};
