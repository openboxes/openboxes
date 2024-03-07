import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import AdditionalDetails from 'components/productSupplier/create/subsections/AdditionalDetails';
import BasicDetails from 'components/productSupplier/create/subsections/BasicDetails';
import { FormErrorPropType } from 'utils/propTypes';

const DetailsSection = ({ control, errors }) => (
  <Section title={{ label: 'react.productSupplier.form.section.details', defaultMessage: 'Details' }}>
    <BasicDetails
      control={control}
      errors={errors?.basicDetails}
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
    basicDetails: PropTypes.shape({
      code: FormErrorPropType,
      product: FormErrorPropType,
      legacyCode: FormErrorPropType,
      supplier: FormErrorPropType,
      supplierCode: FormErrorPropType,
      name: FormErrorPropType,
      active: FormErrorPropType,
      dateCreated: FormErrorPropType,
      lastUpdated: FormErrorPropType,
    }),
    additionalDetails: PropTypes.shape({
      manufacturer: FormErrorPropType,
      ratingTypeCode: FormErrorPropType,
      manufacturerCode: FormErrorPropType,
      brandName: FormErrorPropType,
    }),
  }).isRequired,
};
