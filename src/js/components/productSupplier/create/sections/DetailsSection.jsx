import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import AdditionalDetails from 'components/productSupplier/create/subsections/AdditionalDetails';
import BasicDetails from 'components/productSupplier/create/subsections/BasicDetails';

const DetailsSection = ({ control, errors }) => (
  <Section title={{ label: 'react.productSupplier.form.section.details', defaultMessage: 'Details' }}>
    <BasicDetails
      control={control}
      errors={errors}
    />
    <AdditionalDetails
      control={control}
      errors={errors}
    />
  </Section>
);

export default DetailsSection;

DetailsSection.propTypes = {
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
};
