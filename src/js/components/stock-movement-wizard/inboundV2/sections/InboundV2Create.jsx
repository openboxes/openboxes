import React from 'react';

import PropTypes from 'prop-types';
import { Controller, useWatch } from 'react-hook-form';
import { useSelector } from 'react-redux';

import Button from 'components/form-elements/Button';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Section from 'components/Layout/v2/Section';
import StockMovementDirection from 'consts/StockMovementDirection';
import { debounceLocationsFetch, debouncePeopleFetch } from 'utils/option-utils';
import { FormErrorPropType } from 'utils/propTypes';

const InboundV2Create = ({
  control,
  errors,
  isValid,
  stockLists,
}) => {
  const [origin, destination] = useWatch({
    name: ['origin', 'destination'],
    control,
  });

  const {
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));

  const debouncedLocationsFetch = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    null,
    false,
    false,
    true,
    false,
    StockMovementDirection.INBOUND,
  );

  return (
    <Section title="Details">
      <div className="row">
        <div className="col-12 px-2 pt-2">
          <Controller
            name="description"
            control={control}
            render={({ field }) => (
              <TextInput
                title={{
                  id: 'react.stockMovement.description.label',
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
                  id: 'react.stockMovement.origin.label',
                  defaultMessage: 'Origin',
                }}
                placeholder="Select Origin"
                required
                hasErrors={Boolean(errors.origin?.message)}
                errorMessage={errors.origin?.message}
                async
                loadOptions={debouncedLocationsFetch}
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
                  id: 'react.stockMovement.destination.label',
                  defaultMessage: 'Destination',
                }}
                placeholder="Select Destination"
                required
                hasErrors={Boolean(errors.destination?.message)}
                errorMessage={errors.destination?.message}
                async
                loadOptions={debouncedLocationsFetch}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-6 col-md-12 px-2 pt-2">
          <Controller
            name="stockList"
            disabled={!origin || !destination}
            control={control}
            render={({ field }) => (
              <SelectField
                title={{
                  id: 'react.stockMovement.stocklist.label',
                  defaultMessage: 'Stocklist',
                }}
                options={stockLists}
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
                  id: 'react.stockMovement.requestedBy.label',
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
                  id: 'react.stockMovement.dateRequested.label',
                  defaultMessage: 'Date Requested',
                }}
                errorMessage={errors.dateRequested?.message}
                required
                {...field}
              />
            )}
          />
        </div>
      </div>
      <Button
        label="react.default.button.next.label"
        defaultLabel="Next"
        variant="primary"
        type="submit"
        disabled={!isValid}
        className="fit-content align-self-end"
      />
    </Section>
  );
};

export default InboundV2Create;

InboundV2Create.propTypes = {
  errors: PropTypes.shape({
    description: FormErrorPropType,
    origin: FormErrorPropType,
    destination: FormErrorPropType,
    stocklist: FormErrorPropType,
    requestedBy: FormErrorPropType,
    dateRequested: FormErrorPropType,
  }).isRequired,
  control: PropTypes.shape({}).isRequired,
  isValid: PropTypes.bool.isRequired,
  trigger: PropTypes.func.isRequired,
  stockLists: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  })).isRequired,
};
