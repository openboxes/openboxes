import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { useSelector } from 'react-redux';

import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import RoleType from 'consts/roleType';
import { debounceOrganizationsFetch } from 'utils/option-utils';
import { FormErrorPropType } from 'utils/propTypes';

const AdditionalDetails = ({ control, errors }) => {
  const {
    ratingTypeCodes,
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
    ratingTypeCodes: state.productSupplier.ratingTypeCodes,
  }));

  const debounceManufacturersFetch = useCallback(
    debounceOrganizationsFetch(debounceTime, minSearchLength, [RoleType.ROLE_MANUFACTURER]),
    [debounceTime, minSearchLength],
  );

  return (
    <Subsection
      expandedByDefault={false}
      collapsable
      title={{ label: 'react.productSupplier.form.subsection.additionalDetails', defaultMessage: 'Additional Details' }}
    >
      <div className="row">
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="additionalDetails.manufacturer"
            control={control}
            render={({ field }) => (
              <SelectField
                title={{ id: 'react.productSupplier.form.manufacturer.title', defaultMessage: 'Manufacturer' }}
                placeholder="Search for a manufacturer"
                async
                loadOptions={debounceManufacturersFetch}
                hasErrors={Boolean(errors.manufacturer?.message)}
                errorMessage={errors.manufacturer?.message}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="additionalDetails.ratingTypeCode"
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
                hasErrors={Boolean(errors.ratingTypeCode?.message)}
                errorMessage={errors.ratingTypeCode?.message}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="additionalDetails.manufacturerCode"
            control={control}
            render={({ field }) => (
              <TextInput
                title={{ id: 'react.productSupplier.form.manufacturerCode.title', defaultMessage: 'Manufacturer Code' }}
                errorMessage={errors.manufacturerCode?.message}
                {...field}
              />
            )}
          />
        </div>
        <div className="col-lg-4 col-md-6 px-2 pt-2">
          <Controller
            name="additionalDetails.brandName"
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
      </div>
    </Subsection>
  );
};

export default AdditionalDetails;

export const additionalDetailsFormErrors = PropTypes.shape({
  manufacturer: FormErrorPropType,
  ratingTypeCode: FormErrorPropType,
  manufacturerCode: FormErrorPropType,
  brandName: FormErrorPropType,
});

AdditionalDetails.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: additionalDetailsFormErrors,
};

AdditionalDetails.defaultProps = {
  errors: {},
};
