import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';

import { fetchQuantityUnitOfMeasure } from 'actions';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';

const PackageSpecification = ({ control, errors }) => {
  const dispatch = useDispatch();

  const {
    quantityUom,
  } = useSelector(state => ({
    quantityUom: state.unitOfMeasure.quantity,
  }));

  useEffect(() => {
    dispatch(fetchQuantityUnitOfMeasure());
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
          name="uomCode"
          control={control}
          render={({ field }) => (
            <SelectField
              {...field}
              required
              title={{
                id: 'react.productSupplier.form.uomCode.title',
                defaultMessage: 'Default Source Package',
              }}
              tooltip={{
                id: 'react.productSupplier.form.uomCode.tooltip',
                defaultMessage: 'The most common package purchased for this product',
              }}
              options={quantityUom}
              errorMessage={errors.uomCode?.message}
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
              type="number"
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
          name="minOrderQuantity"
          control={control}
          render={({ field }) => (
            <TextInput
              {...field}
              errorMessage={errors.minOrderQuantity?.message}
              type="number"
              title={{
                id: 'react.productSupplier.form.minOrderQuantity.title',
                defaultMessage: 'MOQ',
              }}
              tooltip={{
                id: 'react.productSupplier.form.minOrderQuantity.tooltip',
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
              decimal={2}
              errorMessage={errors.packagePrice?.message}
              type="number"
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
              decimal={4}
              type="number"
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
    uomCode: PropTypes.shape({
      message: PropTypes.string,
    }),
    packageSize: PropTypes.shape({
      message: PropTypes.string,
    }),
    minOrderQuantity: PropTypes.shape({
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
