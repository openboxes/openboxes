import React from 'react';

import PropTypes from 'prop-types';
import { Controller, useWatch } from 'react-hook-form';

import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import useQuantityUnitOfMeasureOptions from 'hooks/options-data/useQuantityUnitOfMeasureOptions';
import { FormErrorPropType } from 'utils/propTypes';

const PackageSpecification = ({ control, errors, setProductPackageQuantity }) => {
  const { quantityUom } = useQuantityUnitOfMeasureOptions();
  const uom = useWatch({ control, name: 'packageSpecification.uom' });

  return (
    <Subsection
      title={{
        label: 'react.productSupplier.form.subsection.packageSpecification',
        defaultMessage: 'Package Specification',
      }}
      collapsable={false}
    >
      <div className="row">
        <div className="col-lg col-md-6 px-2 pt-2">
          <Controller
            name="packageSpecification.uom"
            control={control}
            render={({ field }) => (
              <SelectField
                {...field}
                onChange={(val) => {
                  field?.onChange(val);
                  // preselect value 1 when unit of measure Each is selected
                  setProductPackageQuantity(val);
                }}
                required
                title={{
                  id: 'react.productSupplier.form.uom.title',
                  defaultMessage: 'Default Source Package',
                }}
                tooltip={{
                  id: 'react.productSupplier.form.uom.tooltip',
                  defaultMessage: 'The most common package purchased for this product',
                }}
                options={quantityUom}
                hasErrors={Boolean(errors.uom?.message)}
                errorMessage={errors.uom?.message}
              />
            )}
          />
        </div>
        <div className="col-lg col-md-6 px-2 pt-2">
          <Controller
            name="packageSpecification.productPackageQuantity"
            control={control}
            render={({ field }) => (
              <TextInput
                {...field}
                disabled={uom?.id === 'EA' || field.disabled}
                required
                type="number"
                decimal={0}
                errorMessage={errors.productPackageQuantity?.message}
                title={{
                  id: 'react.productSupplier.form.productPackageQuantity.title',
                  defaultMessage: 'Package Size',
                }}
                tooltip={{
                  id: 'react.productSupplier.form.productPackageQuantity.tooltip',
                  defaultMessage: 'The number of units per package',
                }}
              />
            )}
          />
        </div>
        <div className="col-lg col-md-6 px-2 pt-2">
          <Controller
            name="packageSpecification.minOrderQuantity"
            control={control}
            render={({ field }) => (
              <TextInput
                {...field}
                errorMessage={errors.minOrderQuantity?.message}
                type="number"
                decimal={0}
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
        </div>
        <div className="col-lg col-md-6 px-2 pt-2">
          <Controller
            name="packageSpecification.productPackagePrice"
            control={control}
            render={({ field }) => (
              <TextInput
                {...field}
                decimal={2}
                errorMessage={errors.productPackagePrice?.message}
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
        </div>
        <div className="col-lg col-md-6 px-2 pt-2">
          <Controller
            name="packageSpecification.eachPrice"
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
      </div>
    </Subsection>
  );
};

export default PackageSpecification;

export const packageSpecificationFormErrors = PropTypes.shape({
  uom: FormErrorPropType,
  productPackageQuantity: FormErrorPropType,
  minOrderQuantity: FormErrorPropType,
  productPackagePrice: FormErrorPropType,
  eachPrice: FormErrorPropType,
});

PackageSpecification.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: packageSpecificationFormErrors,
  setProductPackageQuantity: PropTypes.func.isRequired,
};

PackageSpecification.defaultProps = {
  errors: {},
};
