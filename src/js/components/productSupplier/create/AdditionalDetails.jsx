import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { useSelector } from 'react-redux';

import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import { debounceOrganizationsFetch } from 'utils/option-utils';

const AdditionalDetails = ({ control, mockedRatingTypeCodes }) => {
  const {
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));
  return (
    <Subsection
      title={{ label: 'react.productSupplier.form.subsection.additionalDetails', defaultMessage: 'Additional Details' }}
    >
      <div className="form-grid-3">
        <Controller
          name="manufacturer"
          control={control}
          render={({ field }) => (
            <SelectField
              title={{ id: 'react.productSupplier.form.manufacturer.title', defaultMessage: 'Manufacturer' }}
              async
              loadOptions={debounceOrganizationsFetch(debounceTime, minSearchLength)}
              {...field}
            />
          )}
        />
        <Controller
          name="ratingTypeCode"
          control={control}
          render={({ field }) => (
            <SelectField
              title={{ id: 'react.productSupplier.form.ratingTypeCode.title', defaultMessage: 'Rating Type' }}
              tooltip={{
                id: 'react.productSupplier.form.ratingTypeCode.tooltip',
                defaultMessage: 'Product quality rating based on user feedback or sample review',
              }}
              options={mockedRatingTypeCodes}
              {...field}
            />
          )}
        />
        <Controller
          name="manufacturerCode"
          control={control}
          render={({ field }) => (
            <TextInput
              title={{ id: 'react.productSupplier.form.manufacturerCode.title', defaultMessage: 'Manufacturer Code' }}
              {...field}
            />
          )}
        />
        <Controller
          name="brandName"
          control={control}
          render={({ field }) => (
            <TextInput
              title={{ id: 'react.productSupplier.form.brandName.title', defaultMessage: 'Brand Name' }}
              tooltip={{
                id: 'react.productSupplier.form.brandName.tooltip',
                defaultMessage: 'Product quality rating based on user feedback or sample review',
              }}
              {...field}
            />
          )}
        />
      </div>
    </Subsection>
  );
};

export default AdditionalDetails;

AdditionalDetails.propTypes = {
  control: PropTypes.shape({}).isRequired,
  mockedRatingTypeCodes: PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  }).isRequired,
};
