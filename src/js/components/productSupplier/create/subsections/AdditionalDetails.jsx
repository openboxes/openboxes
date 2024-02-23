import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { useSelector } from 'react-redux';

import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import RoleType from 'consts/roleType';
import { debounceOrganizationsFetch } from 'utils/option-utils';

const AdditionalDetails = ({ control, ratingTypeCodes, errors }) => {
  const {
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));
  const debounceManufacturersFetch =
    useCallback(
      debounceOrganizationsFetch(debounceTime, minSearchLength, [RoleType.ROLE_MANUFACTURER]),
      [debounceTime, minSearchLength],
    );

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
              placeholder="Search for a manufacturer"
              async
              loadOptions={debounceManufacturersFetch}
              errorMessage={errors.manufacturer?.message}
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
              placeholder="Select an option"
              tooltip={{
                id: 'react.productSupplier.form.ratingTypeCode.tooltip',
                defaultMessage: 'Product quality rating based on user feedback or sample review',
              }}
              options={ratingTypeCodes}
              errorMessage={errors.ratingTypeCode?.message}
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
              errorMessage={errors.manufacturerCode?.message}
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
              errorMessage={errors.brandName?.message}
              tooltip={{
                id: 'react.productSupplier.form.brandName.tooltip',
                defaultMessage: 'The brand or product line',
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
  ratingTypeCodes: PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  }).isRequired,
  errors: PropTypes.shape({
    manufacturer: PropTypes.shape({
      message: PropTypes.string,
    }),
    ratingTypeCode: PropTypes.shape({
      message: PropTypes.string,
    }),
    manufacturerCode: PropTypes.shape({
      message: PropTypes.string,
    }),
    brandName: PropTypes.shape({
      message: PropTypes.string,
    }),
  }).isRequired,
};
