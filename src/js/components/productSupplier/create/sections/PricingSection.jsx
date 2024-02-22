import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import FixedPrice from 'components/productSupplier/create/subsections/FixedPrice';
import PackageSpecification
  from 'components/productSupplier/create/subsections/PackageSpecification';

const PricingSection = ({ control, errors }) => (
  <Section title={{
    label: 'react.productSupplier.form.section.pricing',
    defaultMessage: 'Pricing',
  }}
  >
    <PackageSpecification
      control={control}
      errors={errors}
    />
    <FixedPrice
      control={control}
      errors={errors}
    />
  </Section>
);

export default PricingSection;

PricingSection.propTypes = {
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
