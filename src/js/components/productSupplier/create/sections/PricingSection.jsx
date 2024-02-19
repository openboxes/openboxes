import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import PackageSpecification from 'components/productSupplier/create/subsections/PackageSpecification';
import FixedPrice from 'components/productSupplier/create/subsections/FixedPrice';

const PricingSection = ({ control, errors, mockedRatingTypeCodes }) => (
  <Section title={{
    label: 'react.productSupplier.form.section.pricing',
    defaultMessage: 'Pricing',
  }}
  >
    <PackageSpecification />
    <FixedPrice />
  </Section>
);

export default PricingSection;

PricingSection.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    supplier: PropTypes.shape({
      message: PropTypes.string,
    }),
    name: PropTypes.shape({
      message: PropTypes.string,
    }),
    supplierCode: PropTypes.shape({
      message: PropTypes.string,
    }),
    product: PropTypes.shape({
      message: PropTypes.string,
    }),
  }).isRequired,
  mockedRatingTypeCodes: PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  }).isRequired,
};
