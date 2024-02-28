import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';

import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import { useSelector } from 'react-redux';

const DefaultPreferenceType = ({ control, errors }) => {
  const {
    preferenceTypes,
  } = useSelector((state) => ({
    preferenceTypes: state.productSupplier.preferenceTypes,
  }));

  return (
    <Subsection
      collapsable={false}
      title={{
        label: 'react.productSupplier.subsection.defaultPreferenceType.title',
        defaultMessage: 'Default Preference Type',
      }}
    >
      <div className="form-grid-3">
        <Controller
          name="defaultPreferenceType"
          control={control}
          render={({ field }) => (
            <SelectField
              {...field}
              title={{
                id: 'react.productSupplier.form.defaultPreferenceType.title',
                defaultMessage: 'Default Preference Type',
              }}
              tooltip={{
                id: 'react.productSupplier.form.defaultPreferenceType.tooltip',
                defaultMessage: 'Company-wide purchasing preference for this supplier established through a competitive bid',
              }}
              options={preferenceTypes}
              errorMessage={errors.defaultPreferenceType?.message}
            />
          )}
        />
        <Controller
          name="validFrom"
          control={control}
          render={({ field }) => (
            <DateField
              title={{
                id: 'react.productSupplier.form.validFrom.title',
                defaultMessage: 'Valid From',
              }}
              errorMessage={errors.validFrom?.message}
              {...field}
            />
          )}
        />
        <Controller
          name="validUntil"
          control={control}
          render={({ field }) => (
            <DateField
              title={{
                id: 'react.productSupplier.form.validUntil.title',
                defaultMessage: 'Valid Until',
              }}
              errorMessage={errors.validUntil?.message}
              {...field}
            />
          )}
        />
        <Controller
          name="bidName"
          control={control}
          render={({ field }) => (
            <TextInput
              {...field}
              decimal={2}
              type="number"
              errorMessage={errors.bidName?.message}
              title={{
                id: 'react.productSupplier.form.bidName.title',
                defaultMessage: 'Bid Name',
              }}
              tooltip={{
                id: 'react.productSupplier.form.bidName.tooltip',
                defaultMessage: 'The bid during which the purchasing preference was selected',
              }}
            />
          )}
        />
      </div>
    </Subsection>
  );
};

export default DefaultPreferenceType;

DefaultPreferenceType.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    defaultPreferenceType: PropTypes.shape({
      message: PropTypes.string,
    }),
    validFrom: PropTypes.shape({
      message: PropTypes.string,
    }),
    validUntil: PropTypes.shape({
      message: PropTypes.string,
    }),
    bidName: PropTypes.shape({
      message: PropTypes.string,
    }),
  }).isRequired,
};
