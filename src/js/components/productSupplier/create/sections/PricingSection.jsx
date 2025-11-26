import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import FixedPrice, { fixedPriceFormErrors }
  from 'components/productSupplier/create/subsections/FixedPrice';
import PackageSpecification, { packageSpecificationFormErrors }
  from 'components/productSupplier/create/subsections/PackageSpecification';

const PricingSection = ({
  control,
  errors,
  setProductPackageQuantity,
  triggerValidation,
}) => (
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
      triggerValidation={triggerValidation}
    />
  </Section>
);

export default PricingSection;

PricingSection.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    packageSpecification: packageSpecificationFormErrors,
    fixedPrice: fixedPriceFormErrors,
  }).isRequired,
  setProductPackageQuantity: PropTypes.func.isRequired,
  triggerValidation: PropTypes.func.isRequired,
};
