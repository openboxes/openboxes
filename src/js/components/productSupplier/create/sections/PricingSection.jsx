import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import FixedPrice from 'components/productSupplier/create/subsections/FixedPrice';
import PackageSpecification
  from 'components/productSupplier/create/subsections/PackageSpecification';

const PricingSection = ({ control, errors, setProductPackageQuantity }) => (
  <Section title={{
    label: 'react.productSupplier.form.section.pricing',
    defaultMessage: 'Pricing',
  }}
  >
    <PackageSpecification
      control={control}
      errors={errors}
      setProductPackageQuantity={setProductPackageQuantity}
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
    uom: PropTypes.shape({
      message: PropTypes.string,
    }),
    productPackageQuantity: PropTypes.shape({
      message: PropTypes.string,
    }),
    minOrderQuantity: PropTypes.shape({
      message: PropTypes.string,
    }),
    productPackagePrice: PropTypes.shape({
      message: PropTypes.string,
    }),
    eachPrice: PropTypes.shape({
      message: PropTypes.string,
    }),
  }).isRequired,
  setProductPackageQuantity: PropTypes.func.isRequired,
};
