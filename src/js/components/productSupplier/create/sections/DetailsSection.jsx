import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import AdditionalDetails, { additionalDetailsFormErrors }
  from 'components/productSupplier/create/subsections/AdditionalDetails';
import BasicDetails, { basicDetailsFormErrors }
  from 'components/productSupplier/create/subsections/BasicDetails';

const DetailsSection = ({ control, errors, getValues }) => (
  <Section title={{ label: 'react.productSupplier.form.section.details', defaultMessage: 'Details' }}>
    <BasicDetails
      control={control}
      errors={errors?.basicDetails}
      getValues={getValues}
    />
    <AdditionalDetails
      control={control}
      errors={errors?.additionalDetails}
    />
  </Section>
);

export default DetailsSection;

DetailsSection.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    basicDetails: basicDetailsFormErrors,
    additionalDetails: additionalDetailsFormErrors,
  }).isRequired,
  getValues: PropTypes.func.isRequired,
};
