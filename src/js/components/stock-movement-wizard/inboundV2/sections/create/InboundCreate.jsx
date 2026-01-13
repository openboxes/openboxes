import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';

import Button from 'components/form-elements/Button';
import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Section from 'components/Layout/v2/Section';
import { DateFormatDateFns } from 'consts/timeFormat';
import useInboundCreateForm from 'hooks/inboundV2/create/useInboundCreateForm';

const InboundCreate = ({ next }) => {
  const {
    form: { control, errors, handleSubmit },
    data: {
      stockLists,
      origin,
      debouncedOriginLocationsFetch,
      debouncedPeopleFetch,
    },
    actions: { onSubmitStockMovementDetails },
  } = useInboundCreateForm({ next });

  return (
    <form onSubmit={handleSubmit(onSubmitStockMovementDetails)}>
      <Section showTitle={false}>
        <div className="row">
          <div className="col-12 px-2 pt-2">
            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextInput
                  {...field}
                  title={{
                    id: 'react.stockMovement.description.label',
                    defaultMessage: 'Description',
                  }}
                  errorMessage={errors.description?.message}
                  required
                  customTooltip
                  ariaLabel={{
                    id: 'react.stockMovement.description.label',
                    defaultMessage: 'Description',
                  }}
                  hasErrors={Boolean(errors.description?.message)}
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
                  {...field}
                  title={{
                    id: 'react.stockMovement.origin.label',
                    defaultMessage: 'Origin',
                  }}
                  placeholder="Select Origin"
                  required
                  hasErrors={Boolean(errors.origin?.message)}
                  errorMessage={errors.origin?.message}
                  async
                  loadOptions={debouncedOriginLocationsFetch}
                  customTooltip
                  ariaLabel={{
                    id: 'react.stockMovement.origin.label',
                    defaultMessage: 'Origin',
                  }}
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
                  {...field}
                  title={{
                    id: 'react.stockMovement.destination.label',
                    defaultMessage: 'Destination',
                  }}
                  customTooltip
                  ariaLabel={{
                    id: 'react.stockMovement.destination.label',
                    defaultMessage: 'Destination',
                  }}
                />
              )}
            />
          </div>
          <div className="col-lg-6 col-md-12 px-2 pt-2">
            <Controller
              name="stocklist"
              disabled={!origin}
              control={control}
              render={({ field }) => (
                <SelectField
                  {...field}
                  title={{
                    id: 'react.stockMovement.stocklist.label',
                    defaultMessage: 'Stocklist',
                  }}
                  options={stockLists}
                  customTooltip
                  ariaLabel={{
                    id: 'react.stockMovement.stocklist.label',
                    defaultMessage: 'Stocklist',
                  }}
                  hasErrors={Boolean(errors.stocklist?.message)}
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
                  {...field}
                  title={{
                    id: 'react.stockMovement.requestedBy.label',
                    defaultMessage: 'Requested By',
                  }}
                  required
                  hasErrors={Boolean(errors.requestedBy?.message)}
                  errorMessage={errors.requestedBy?.message}
                  async
                  loadOptions={debouncedPeopleFetch}
                  customTooltip
                  ariaLabel={{
                    id: 'react.stockMovement.requestedBy.label',
                    defaultMessage: 'Requested By',
                  }}
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
                  showCustomInput={false}
                  ariaLabel={{
                    id: 'react.stockMovement.dateRequested.label',
                    defaultMessage: 'Date Requested',
                  }}
                  hasErrors={Boolean(errors.dateRequested?.message)}
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

export default InboundCreate;

InboundCreate.propTypes = {
  next: PropTypes.func.isRequired,
};
