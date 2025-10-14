import React from 'react';

import PropTypes from 'prop-types';
import { Controller, useWatch } from 'react-hook-form';
import { useSelector } from 'react-redux';

import Button from 'components/form-elements/Button';
import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Section from 'components/Layout/v2/Section';
import StockMovementDirection from 'consts/StockMovementDirection';
import { DateFormatDateFns } from 'consts/timeFormat';
import useInboundCreateForm from 'hooks/inboundV2/create/useInboundCreateForm';
import { debounceLocationsFetch, debouncePeopleFetch } from 'utils/option-utils';

const InboundV2Create = ({ next }) => {
  const {
    errors,
    control,
    trigger,
    handleSubmit,
    onSubmitStockMovementDetails,
    stockLists,
    setValue,
  } = useInboundCreateForm({ next });

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
    <form onSubmit={handleSubmit(onSubmitStockMovementDetails)}>
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
                  customTooltip
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
                  customTooltip
                  {...field}
                />
              )}
            />
          </div>
          <div className="col-lg-6 col-md-12 px-2 pt-2">
            <Controller
              name="destination"
              control={control}
              disabled
              render={({ field }) => (
                <SelectField
                  title={{
                    id: 'react.stockMovement.destination.label',
                    defaultMessage: 'Destination',
                  }}
                  customTooltip
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
                  customTooltip
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
                  customTooltip
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
                <DateFieldDateFns
                  {...field}
                  title={{
                    id: 'react.stockMovement.dateRequested.label',
                    defaultMessage: 'Date Requested',
                  }}
                  errorMessage={errors.dateRequested?.message}
                  required
                  customDateFormat={DateFormatDateFns.DD_MMM_YYYY}
                  customTooltip
                  onChange={async (newDate) => {
                    setValue('dateRequested', newDate);
                    await trigger();
                  }}
                />
              )}
            />
          </div>
        </div>
      </Section>
      <div className="d-flex justify-content-end mt-4">
        <Button
          label="react.default.button.next.label"
          defaultLabel="Next"
          variant="primary"
          type="submit"
          disabled={!!Object.keys(errors).length}
        />
      </div>
    </form>
  );
};

export default InboundV2Create;

InboundV2Create.propTypes = {
  next: PropTypes.func.isRequired,
};
