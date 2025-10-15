import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { useSelector } from 'react-redux';

import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import StockMovementDirection from 'consts/StockMovementDirection';
import { debounceLocationsFetch, debouncePeopleFetch } from 'utils/option-utils';
import { FormErrorPropType } from 'utils/propTypes';

const OutboundImportBasicDetails = ({ control, errors }) => {
  const {
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));

  const loadOutboundLocations = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    null,
    false,
    false,
    true,
    false,
    StockMovementDirection.OUTBOUND,
  );

  return (
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
                title={{
                  id: 'react.outboundImport.form.description.title',
                  defaultMessage: 'Description',
                }}
                errorMessage={errors.description?.message}
                required
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-6 col-md-12 px-2 pt-2">
          <Controller
            name="origin"
            control={control}
            render={({ field }) => (
              <SelectField
                title={{
                  id: 'react.outboundImport.form.origin.title',
                  defaultMessage: 'Origin',
                }}
                placeholder="Select Origin"
                required
                hasErrors={Boolean(errors.origin?.message)}
                errorMessage={errors.origin?.message}
                async
                loadOptions={loadOutboundLocations}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-6 col-md-12 px-2 pt-2">
          <Controller
            name="destination"
            control={control}
            render={({ field }) => (
              <SelectField
                title={{
                  id: 'react.outboundImport.form.destination.title',
                  defaultMessage: 'Destination',
                }}
                placeholder="Select Destination"
                required
                hasErrors={Boolean(errors.destination?.message)}
                errorMessage={errors.destination?.message}
                async
                loadOptions={loadOutboundLocations}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-6 col-md-12 px-2 pt-2">
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
        <div className="col-lg-6 col-md-12 px-2 pt-2">
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
  );
};

export default OutboundImportBasicDetails;

OutboundImportBasicDetails.propTypes = {
  errors: PropTypes.shape({
    description: FormErrorPropType,
    origin: FormErrorPropType,
    destination: FormErrorPropType,
    requestedBy: FormErrorPropType,
    dateRequested: FormErrorPropType,
  }).isRequired,
  control: PropTypes.shape({}).isRequired,
};
