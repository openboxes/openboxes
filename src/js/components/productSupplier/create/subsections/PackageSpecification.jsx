import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';

import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUoM } from 'actions';

const PackageSpecification = ({ control, errors }) => {
  const dispatch = useDispatch();

  const {
    unitsOfMeasure,
  } = useSelector(state => ({
    unitsOfMeasure: state.unitOfMeasure.unitsOfMeasure,
  }));

  useEffect(() => {
    dispatch(fetchUoM());
  }, []);


  return (
    <Subsection
      title={{
        label: 'react.productSupplier.form.subsection.packageSpecification',
        defaultMessage: 'Package Specification',
      }}
      collapsable={false}
    >
      <div className="form-grid-3">
        <Controller
          name="defaultSourcePackage"
          control={control}
          render={({ field }) => (
            <SelectField
              {...field}
              required
              title={{
                id: 'react.productSupplier.form.defaultSourcePackage.title',
                defaultMessage: 'Default Source Package',
              }}
              options={unitsOfMeasure}
              errorMessage={errors.defaultSourcePackage?.message}
            />
          )}
        />
        <Controller
          name="packageSize"
          control={control}
          render={({ field }) => (
            <TextInput
              {...field}
              required
              errorMessage={errors.packageSize?.message}
              title={{
                id: 'react.productSupplier.form.packageSize.title',
                defaultMessage: 'Package Size',
              }}
              tooltip={{
                id: 'react.productSupplier.form.packageSize.tooltip',
                defaultMessage: 'The number of units per package',
              }}
            />
          )}
        />
        <Controller
          name="minimumOrderQuantity"
          control={control}
          render={({ field }) => (
            <TextInput
              {...field}
              errorMessage={errors.minimumOrderQuantity?.message}
              title={{
                id: 'react.productSupplier.form.minimumOrderQuantity.title',
                defaultMessage: 'MOQ',
              }}
              tooltip={{
                id: 'react.productSupplier.form.minimumOrderQuantity.tooltip',
                defaultMessage: 'Minimum Order Quantity - the smallest order the vendor will accept for this product',
              }}
            />
          )}
        />
        <Controller
          name="packagePrice"
          control={control}
          render={({ field }) => (
            <TextInput
              {...field}
              errorMessage={errors.packagePrice?.message}
              title={{
                id: 'react.productSupplier.form.packagePrice.title',
                defaultMessage: 'Package Price',
              }}
              tooltip={{
                id: 'react.productSupplier.form.packagePrice.tooltip',
                defaultMessage: 'The most recent price paid per default package',
              }}
            />
          )}
        />
        <Controller
          name="eachPrice"
          control={control}
          render={({ field }) => (
            <TextInput
              {...field}
              disabled
              errorMessage={errors.eachPrice?.message}
              title={{
                id: 'react.productSupplier.form.eachPrice.title',
                defaultMessage: 'Each Price',
              }}
              tooltip={{
                id: 'react.productSupplier.form.eachPrice.tooltip',
                defaultMessage: 'The most recent price paid per smallest individual unit (package price÷package size)',
              }}
            />
          )}
        />
      </div>
    </Subsection>
  );
};

export default PackageSpecification;

PackageSpecification.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    defaultSourcePackage: PropTypes.shape({
      message: PropTypes.string,
    }),
    packageSize: PropTypes.shape({
      message: PropTypes.string,
    }),
    minimumOrderQuantity: PropTypes.shape({
      message: PropTypes.string,
    }),
    packagePrice: PropTypes.shape({
      message: PropTypes.string,
    }),
    eachPrice: PropTypes.shape({
      message: PropTypes.string,
    }),
  }).isRequired,
};
