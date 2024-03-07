import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import FixedPrice from 'components/productSupplier/create/subsections/FixedPrice';
import PackageSpecification
  from 'components/productSupplier/create/subsections/PackageSpecification';
import { FormErrorPropType } from 'utils/propTypes';

const PricingSection = ({ control, errors, setProductPackageQuantity }) => (
  <Section title={{
    label: 'react.productSupplier.form.section.pricing',
    defaultMessage: 'Pricing',
  }}
  >
    <PackageSpecification
      control={control}
      errors={errors?.packageSpecification}
      setProductPackageQuantity={setProductPackageQuantity}
    />
    <FixedPrice
      control={control}
      errors={errors?.fixedPrice}
    />
  </Section>
);

export default PricingSection;

PricingSection.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    packageSpecification: PropTypes.shape({
      uom: FormErrorPropType,
      productPackageQuantity: FormErrorPropType,
      minOrderQuantity: FormErrorPropType,
      productPackagePrice: FormErrorPropType,
      eachPrice: FormErrorPropType,
    }),
    fixedPrice: PropTypes.shape({
      contractPricePrice: FormErrorPropType,
      contractPriceValidUntil: FormErrorPropType,
      tieredPricing: FormErrorPropType,
    }),
  }).isRequired,
  setProductPackageQuantity: PropTypes.func.isRequired,
};
