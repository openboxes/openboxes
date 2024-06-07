import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { useSelector } from 'react-redux';

import { fetchShipmentTypes } from 'actions';
import Button from 'components/form-elements/Button';
import DateField from 'components/form-elements/v2/DateField';
import FileSelect from 'components/form-elements/v2/FileSelect';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Section from 'components/Layout/v2/Section';
import Subsection from 'components/Layout/v2/Subsection';
import useOptionsFetch from 'hooks/options-data/useOptionsFetch';
import { debounceLocationsFetch, debouncePeopleFetch } from 'utils/option-utils';
import { FormErrorPropType } from 'utils/propTypes';

const OutboundImportDetails = ({ control, errors, isValid }) => {
  const {
    debounceTime,
    minSearchLength,
    shipmentTypes,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
    shipmentTypes: state.stockMovementCommon.shipmentTypes,
  }));

  useOptionsFetch([fetchShipmentTypes]);

  return (
    <Section
      title={{
        label: 'react.outboundImport.form.details.label',
        defaultMessage: 'Details',
      }}
    >
      <Subsection
        title={{ label: 'react.outboundImport.form.basicDetails.label', defaultMessage: 'Basic details' }}
        collapsable={false}
      >
        <div className="row">
          <div className="col-12 px-2 pt-2">
            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextInput
                  title={{ id: 'react.outboundImport.form.description.title', defaultMessage: 'Description' }}
                  errorMessage={errors.description?.message}
                  required
                  {...field}
                />
              )}
            />
          </div>
          <div className="col-6 px-2 pt-2">
            <Controller
              name="origin"
              control={control}
              render={({ field }) => (
                <SelectField
                  title={{ id: 'react.outboundImport.form.origin.title', defaultMessage: 'Origin' }}
                  placeholder="Select Origin"
                  required
                  hasErrors={Boolean(errors.origin?.message)}
                  errorMessage={errors.origin?.message}
                  async
                  loadOptions={debounceLocationsFetch(debounceTime, minSearchLength)}
                  {...field}
                />
              )}
            />
          </div>
          <div className="col-6 px-2 pt-2">
            <Controller
              name="destination"
              control={control}
              render={({ field }) => (
                <SelectField
                  title={{ id: 'react.outboundImport.form.destination.title', defaultMessage: 'Destination' }}
                  placeholder="Select Destination"
                  required
                  hasErrors={Boolean(errors.destination?.message)}
                  errorMessage={errors.destination?.message}
                  async
                  loadOptions={debounceLocationsFetch(debounceTime, minSearchLength)}
                  {...field}
                />
              )}
            />
          </div>
          <div className="col-6 px-2 pt-2">
            <Controller
              name="requestedBy"
              control={control}
              render={({ field }) => (
                <SelectField
                  title={{
                    id: 'react.outboundImport.form.requestedBy.title',
                    defaultMessage: 'Requested By',
                  }}
                  required
                  hasErrors={Boolean(errors.requestedBy?.message)}
                  errorMessage={errors.requestedBy?.message}
                  async
                  loadOptions={debouncePeopleFetch(debounceTime, minSearchLength)}
                  {...field}
                />
              )}
            />
          </div>
          <div className="col-6 px-2 pt-2">
            <Controller
              name="dateRequested"
              control={control}
              render={({ field }) => (
                <DateField
                  title={{
                    id: 'react.outboundImport.form.dateRequested.title',
                    defaultMessage: 'Date Requested',
                  }}
                  placeholder={{
                    id: 'react.default.dateInput.placeholder.label',
                    default: 'Select a date',
                  }}
                  errorMessage={errors.dateRequested?.message}
                  required
                  {...field}
                />
              )}
            />
          </div>
        </div>
      </Subsection>
      <Subsection title={{ label: 'react.outboundImport.form.sendingOptions.label', defaultMessage: 'Sending options' }} collapsable={false}>
        <div className="row">
          <div className="col-3 px-2 pt-2">
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
          <div className="col-3 px-2 pt-2">
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
          <div className="col-3 px-2 pt-2">
            <Controller
              name="dateShipped"
              control={control}
              render={({ field }) => (
                <DateField
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
                />
              )}
            />
          </div>
          <div className="col-3 px-2 pt-2">
            <Controller
              name="expectedDeliveryDate"
              control={control}
              render={({ field }) => (
                <DateField
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
                />
              )}
            />
          </div>
          <div className="col-12 px-2 pt-2">
            <Controller
              name="packingList"
              control={control}
              render={({ field }) => (
                <FileSelect
                  allowedExtensions={['xls']}
                  errorMessage={errors.packingList?.message}
                  dropzoneText={{
                    id: 'react.outboundImport.form.importPackingList.title',
                    defaultMessage: 'Import packing list',
                  }}
                  {...field}
                />
              )}
            />
          </div>
        </div>
      </Subsection>
      <Button
        label="react.outboundImport.form.next.label"
        defaultLabel="Next"
        variant="primary"
        type="submit"
        className="fit-content align-self-end"
        disabled={isValid}
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
};
